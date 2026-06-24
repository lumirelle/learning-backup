package ldapsync

import (
	"sort"
	"strings"
)

// DeptNode 是从 LDAP 分组路径重建出的部门树节点（内存态，未落库）。
type DeptNode struct {
	Path      string      `json:"path"`       // 完整部门路径 A/B/C
	Name      string      `json:"name"`       // 末级名 C
	EntryUUID string      `json:"entry_uuid"` // 命中 LDAP 分组条目时带上（中间补全节点为空）
	GIDNumber int         `json:"gid_number"`
	Synthetic bool        `json:"synthetic"` // true = 路径中间补全的节点，LDAP 无对应条目
	Children  []*DeptNode `json:"children,omitempty"`
}

// PreviewUser 是预览里的人员条目，附带解析出的部门路径。
type PreviewUser struct {
	UID         string   `json:"uid"`
	WxUID       string   `json:"wx_uid"`
	DisplayName string   `json:"display_name"`
	Mail        string   `json:"mail"`
	DeptPaths   []string `json:"dept_paths"` // 由 memberOf 解析出的部门路径（可能多个）
}

// Preview 是 BuildPreview 的结果：可视化的部门树 + 人员清单 + 计数。
type Preview struct {
	Tree         []*DeptNode   `json:"tree"`
	DeptCount    int           `json:"dept_count"`
	CustomGroups []string      `json:"custom_groups"` // 自建分组（非部门型）的 cn
	Users        []PreviewUser `json:"users"`
	UserCount    int           `json:"user_count"`
	Warnings     []string      `json:"warnings,omitempty"`
}

const pathSep = "/"

// normalizeDN 规范化 DN 以便相等比较：小写 + 去掉逗号分隔符两侧空格
// （不同来源的 memberOf / 条目 DN 可能在逗号后带或不带空格）。
func normalizeDN(dn string) string {
	parts := strings.Split(strings.ToLower(dn), ",")
	for i := range parts {
		parts[i] = strings.TrimSpace(parts[i])
	}
	return strings.Join(parts, ",")
}

// BuildPreview 把快照解析为部门树与人员清单（纯计算，无副作用）。
// 部门树由「部门型分组」的 cn 路径重建，按 `/` 拆分并补全缺失的中间层。
func BuildPreview(snap *Snapshot) *Preview {
	p := &Preview{}

	dnToPath := make(map[string]string)        // 部门分组 DN → 路径（供 memberOf 反查）
	userDNToPaths := make(map[string][]string) // 用户 DN → 部门路径（由分组 uniqueMember 反查）
	roots := make(map[string]*DeptNode)
	index := make(map[string]*DeptNode) // path → node

	ensure := func(path string) *DeptNode {
		if n, ok := index[path]; ok {
			return n
		}
		segs := strings.Split(path, pathSep)
		n := &DeptNode{Path: path, Name: segs[len(segs)-1], Synthetic: true}
		index[path] = n
		if len(segs) == 1 {
			roots[path] = n
		} else {
			parent := ensureChain(strings.Join(segs[:len(segs)-1], pathSep), index, roots)
			parent.Children = append(parent.Children, n)
		}
		return n
	}

	for _, g := range snap.Groups {
		if !g.IsDept() {
			if g.CN != "" {
				p.CustomGroups = append(p.CustomGroups, g.CN)
			}
			continue
		}
		path := strings.Trim(g.CN, pathSep)
		if path == "" {
			p.Warnings = append(p.Warnings, "部门分组 cn 为空，已跳过: "+g.DN)
			continue
		}
		dnToPath[normalizeDN(g.DN)] = path
		// 由分组的 uniqueMember 反查成员（不依赖用户条目是否返回 memberOf）。
		for _, m := range g.Members {
			mk := normalizeDN(m)
			userDNToPaths[mk] = append(userDNToPaths[mk], path)
		}
		n := ensure(path)
		// 实体分组覆盖中间补全节点的占位信息。
		n.Synthetic = false
		n.EntryUUID = g.EntryUUID
		n.GIDNumber = g.GIDNumber
	}

	for _, u := range snap.Users {
		// 不用 LDAP givenName 当英文名：真实目录里 givenName = name 去掉首字符，
		// 对中文名是「去姓」、对英文名会丢首字母（如 Muhammad→uhammad），不可靠。
		pu := PreviewUser{UID: u.UID, WxUID: u.WxUID, DisplayName: u.DisplayName, Mail: u.Mail}
		seen := map[string]bool{}
		add := func(path string) {
			if !seen[path] {
				pu.DeptPaths = append(pu.DeptPaths, path)
				seen[path] = true
			}
		}
		// 主：分组 uniqueMember 反查；补：用户 memberOf（若目录返回）。
		for _, path := range userDNToPaths[normalizeDN(u.DN)] {
			add(path)
		}
		for _, dn := range u.MemberOf {
			if path, ok := dnToPath[normalizeDN(dn)]; ok {
				add(path)
			}
		}
		sort.Strings(pu.DeptPaths)
		if len(pu.DeptPaths) == 0 {
			p.Warnings = append(p.Warnings, "用户无可解析部门（memberOf 未命中部门型分组）: "+u.UID)
		}
		p.Users = append(p.Users, pu)
	}

	p.Tree = sortNodes(roots)
	p.DeptCount = len(index)
	p.UserCount = len(p.Users)
	sort.Strings(p.CustomGroups)
	return p
}

// ensureChain 在 ensure 内部递归补全父链（与 ensure 同逻辑，避免闭包自引用）。
func ensureChain(path string, index map[string]*DeptNode, roots map[string]*DeptNode) *DeptNode {
	if n, ok := index[path]; ok {
		return n
	}
	segs := strings.Split(path, pathSep)
	n := &DeptNode{Path: path, Name: segs[len(segs)-1], Synthetic: true}
	index[path] = n
	if len(segs) == 1 {
		roots[path] = n
	} else {
		parent := ensureChain(strings.Join(segs[:len(segs)-1], pathSep), index, roots)
		parent.Children = append(parent.Children, n)
	}
	return n
}

func sortNodes(roots map[string]*DeptNode) []*DeptNode {
	var out []*DeptNode
	for _, n := range roots {
		sortChildren(n)
		out = append(out, n)
	}
	sort.Slice(out, func(i, j int) bool { return out[i].Path < out[j].Path })
	return out
}

func sortChildren(n *DeptNode) {
	sort.Slice(n.Children, func(i, j int) bool { return n.Children[i].Path < n.Children[j].Path })
	for _, c := range n.Children {
		sortChildren(c)
	}
}
