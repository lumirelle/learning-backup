package ldapsync

import "testing"

// 用文档示例（docs/sso/ldap.md）构造快照，验证部门树重建与人员部门解析。
func sampleSnapshot() *Snapshot {
	return &Snapshot{
		Groups: []Group{
			// 部门型（gid≥30000），cn 为完整路径，缺中间层 "南京小东/业务3部(iOS)"
			{DN: "cn=南京小东/业务3部(iOS)/技术组,ou=group,dc=p,dc=x,dc=com", EntryUUID: "g1", CN: "南京小东/业务3部(iOS)/技术组", GIDNumber: 30012},
			{DN: "cn=南京小东/行政部,ou=group,dc=p,dc=x,dc=com", EntryUUID: "g2", CN: "南京小东/行政部", GIDNumber: 30020},
			// 自建分组（gid<30000）：不进部门树
			{DN: "cn=自定义分组1,ou=group,dc=p,dc=x,dc=com", EntryUUID: "g3", CN: "自定义分组1", GIDNumber: 20005},
		},
		Users: []User{
			{DN: "uid=chen,ou=user", UID: "chen", WxUID: "chen", DisplayName: "陈嘉生", GivenName: "嘉生", Mail: "c@x.com",
				MemberOf: []string{"cn=南京小东/业务3部(iOS)/技术组,ou=group,dc=p,dc=x,dc=com", "cn=自定义分组1,ou=group,dc=p,dc=x,dc=com"}},
			{DN: "uid=li,ou=user", UID: "li", WxUID: "li", DisplayName: "李四", MemberOf: nil}, // 无部门
		},
	}
}

// 真实目录里用户条目可能不返回 memberOf，部门关系只在分组的 uniqueMember 上。
func memberViaGroupSnapshot() *Snapshot {
	return &Snapshot{
		Groups: []Group{
			{DN: "cn=总裁办/财务部,ou=group", EntryUUID: "g1", CN: "总裁办/财务部", GIDNumber: 0,
				Members: []string{"uid=wang, ou=user"}}, // 注意逗号后带空格，考验 DN 规范化
		},
		Users: []User{
			{DN: "uid=wang,ou=user", UID: "wang", WxUID: "wang", DisplayName: "王五", MemberOf: nil},
		},
	}
}

func TestBuildPreviewMemberViaUniqueMember(t *testing.T) {
	pv := BuildPreview(memberViaGroupSnapshot())
	if pv.DeptCount != 2 { // 总裁办 + 总裁办/财务部
		t.Fatalf("应有 2 部门（含补全的总裁办），实际 %d", pv.DeptCount)
	}
	if len(pv.Users) != 1 || len(pv.Users[0].DeptPaths) != 1 || pv.Users[0].DeptPaths[0] != "总裁办/财务部" {
		t.Fatalf("王五 应经 uniqueMember 解析到财务部（用户无 memberOf）: %+v", pv.Users)
	}
}

// gidNumber 为 0/缺失（真实目录的情况）时，部门不应被误判成自建分组。
func TestBuildPreviewTreatsMissingGidAsDept(t *testing.T) {
	snap := &Snapshot{Groups: []Group{
		{DN: "cn=产研交付中心,ou=group", EntryUUID: "g1", CN: "产研交付中心", GIDNumber: 0},
		{DN: "cn=自建分组X,ou=group", EntryUUID: "g2", CN: "自建分组X", GIDNumber: 20005},
	}}
	pv := BuildPreview(snap)
	if pv.DeptCount != 1 || pv.Tree[0].Path != "产研交付中心" {
		t.Fatalf("gid=0 的应当作部门进树: %+v", pv.Tree)
	}
	if len(pv.CustomGroups) != 1 || pv.CustomGroups[0] != "自建分组X" {
		t.Fatalf("gid 在自建区间的才算自建分组: %+v", pv.CustomGroups)
	}
}

func TestBuildPreviewTree(t *testing.T) {
	pv := BuildPreview(sampleSnapshot())

	// 根应只有「南京小东」一个，其下两个子节点（业务3部(iOS)、行政部）
	if len(pv.Tree) != 1 || pv.Tree[0].Path != "南京小东" {
		t.Fatalf("根节点异常: %+v", pv.Tree)
	}
	root := pv.Tree[0]
	if !root.Synthetic {
		t.Error("南京小东 在 LDAP 无实体分组，应为补全(synthetic)节点")
	}
	if len(root.Children) != 2 {
		t.Fatalf("南京小东 应有 2 个子部门，实际 %d", len(root.Children))
	}

	// 中间补全节点「南京小东/业务3部(iOS)」synthetic，其下技术组为实体
	var biz *DeptNode
	for _, c := range root.Children {
		if c.Path == "南京小东/业务3部(iOS)" {
			biz = c
		}
	}
	if biz == nil || !biz.Synthetic {
		t.Fatalf("业务3部(iOS) 应为补全节点: %+v", biz)
	}
	if len(biz.Children) != 1 || biz.Children[0].EntryUUID != "g1" || biz.Children[0].Synthetic {
		t.Fatalf("技术组应为实体叶子(entryUUID=g1): %+v", biz.Children)
	}

	// dept_count = 南京小东 + 业务3部(iOS) + 技术组 + 行政部 = 4
	if pv.DeptCount != 4 {
		t.Errorf("部门数应为 4，实际 %d", pv.DeptCount)
	}
	// 自建分组不进树，单列
	if len(pv.CustomGroups) != 1 || pv.CustomGroups[0] != "自定义分组1" {
		t.Errorf("自建分组应单列: %+v", pv.CustomGroups)
	}
}

func TestBuildPreviewUsers(t *testing.T) {
	pv := BuildPreview(sampleSnapshot())
	var chen, li *PreviewUser
	for i := range pv.Users {
		switch pv.Users[i].UID {
		case "chen":
			chen = &pv.Users[i]
		case "li":
			li = &pv.Users[i]
		}
	}
	if chen == nil || len(chen.DeptPaths) != 1 || chen.DeptPaths[0] != "南京小东/业务3部(iOS)/技术组" {
		t.Fatalf("陈 应解析到技术组部门（自建分组不算部门）: %+v", chen)
	}
	// 无 memberOf 的用户应产生 warning
	if li == nil || len(li.DeptPaths) != 0 {
		t.Fatalf("李四 应无部门: %+v", li)
	}
	found := false
	for _, w := range pv.Warnings {
		if w == "用户无可解析部门（memberOf 未命中部门型分组）: li" {
			found = true
		}
	}
	if !found {
		t.Errorf("应对无部门用户产生 warning: %+v", pv.Warnings)
	}
}
