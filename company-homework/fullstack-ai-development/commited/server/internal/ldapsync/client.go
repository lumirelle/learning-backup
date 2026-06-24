// Package ldapsync 把 LDAP 目录（组织架构的镜像）读进本系统：
// ou=group 重建为部门树，ou=user 映射为人员，LDAP 为唯一事实源。
//
// 本文件只负责「连接 + 读取 + 解析成内存快照」，
// 不写库；落库在 sync.go。读取全程只读，连接用完即关。
package ldapsync

import (
	"crypto/tls"
	"fmt"
	"sort"
	"strconv"
	"strings"

	"github.com/go-ldap/ldap/v3"

	"orghr/internal/infra/config"
)

// 自建分组的 gidNumber 区间。
// 真实目录里部门的 gidNumber 可能为 0/缺失或区间与文档不符，故判定采用「排除法」：
// 只有 gid 明确落在自建区间才视为自建分组，其余（含 gid 缺失）一律当部门，
// 以「完全使用 LDAP 组织架构」为准，避免真实部门被误判成自建分组而漏掉整棵树。
const (
	customGidMin = 20000
	customGidMax = 30000
)

// Group 是 ou=group 下的一个条目（部门或自建分组）。
type Group struct {
	DN        string   `json:"dn"`
	EntryUUID string   `json:"entry_uuid"`
	CN        string   `json:"cn"`         // 部门型：完整部门路径（A/B/C，已去首级）
	GIDNumber int      `json:"gid_number"` // 区分部门(≥30000)/自建分组
	Members   []string `json:"-"`          // uniqueMember 的用户 DN 列表
}

// IsDept 判断是否参与组织树重建：除非 gidNumber 明确落在自建分组区间，否则都算部门。
func (g Group) IsDept() bool { return g.GIDNumber < customGidMin || g.GIDNumber >= customGidMax }

// User 是 ou=user 下的一个条目。
type User struct {
	DN          string   `json:"dn"`
	EntryUUID   string   `json:"entry_uuid"`
	UID         string   `json:"uid"`          // = username
	WxUID       string   `json:"wx_uid"`       // = SSO User.wxUid，人员稳定关联键
	DisplayName string   `json:"display_name"` // = 中文名
	SN          string   `json:"sn"`
	GivenName   string   `json:"given_name"`
	Mail        string   `json:"mail"`
	UIDNumber   int      `json:"uid_number"`
	MemberOf    []string `json:"member_of"` // 所属分组/部门 DN
}

// Snapshot 是一次目录读取的完整结果（未落库）。
type Snapshot struct {
	Groups []Group `json:"groups"`
	Users  []User  `json:"users"`
}

// Client 封装一条到 LDAP 目录的连接。
type Client struct {
	conn *ldap.Conn
	cfg  config.LDAPConfig
}

// Dial 建立连接并以管理员 DN 绑定。调用方负责 Close。
func Dial(cfg config.LDAPConfig) (*Client, error) {
	conn, err := ldap.DialURL(cfg.URL)
	if err != nil {
		return nil, fmt.Errorf("ldap 连接失败: %w", err)
	}
	if cfg.StartTLS {
		if err := conn.StartTLS(&tls.Config{InsecureSkipVerify: true}); err != nil {
			_ = conn.Close()
			return nil, fmt.Errorf("ldap StartTLS 失败: %w", err)
		}
	}
	if err := conn.Bind(cfg.BindDN, cfg.BindPW); err != nil {
		_ = conn.Close()
		return nil, fmt.Errorf("ldap 绑定失败（检查 BindDN/密码/IP 白名单）: %w", err)
	}
	return &Client{conn: conn, cfg: cfg}, nil
}

// Close 关闭连接。
func (c *Client) Close() {
	if c.conn != nil {
		_ = c.conn.Close()
	}
}

// search 执行一次分页搜索（PagingControl，避免大目录截断）。
func (c *Client) search(baseDN, filter string, attrs []string) ([]*ldap.Entry, error) {
	if baseDN == "" {
		baseDN = c.cfg.BaseDN
	}
	req := ldap.NewSearchRequest(
		baseDN, ldap.ScopeWholeSubtree, ldap.NeverDerefAliases,
		0, 0, false, filter, attrs, nil,
	)
	res, err := c.conn.SearchWithPaging(req, 500)
	if err != nil {
		return nil, fmt.Errorf("ldap 搜索失败 base=%s filter=%s: %w", baseDN, filter, err)
	}
	return res.Entries, nil
}

// Read 读取全部分组与用户，组装为快照（只读，不写库）。
func (c *Client) Read() (*Snapshot, error) {
	groupDN := c.cfg.GroupDN
	if groupDN == "" {
		groupDN = "ou=group," + c.cfg.BaseDN
	}
	userDN := c.cfg.UserDN
	if userDN == "" {
		userDN = "ou=user," + c.cfg.BaseDN
	}

	gEntries, err := c.search(groupDN, "(objectClass=groupOfUniqueNames)",
		[]string{"entryUUID", "cn", "gidNumber", "uniqueMember"})
	if err != nil {
		return nil, err
	}
	uEntries, err := c.search(userDN, "(objectClass=posixAccount)",
		[]string{"entryUUID", "uid", "wxUid", "displayName", "sn", "givenName", "mail", "uidNumber", "memberOf"})
	if err != nil {
		return nil, err
	}

	snap := &Snapshot{}
	for _, e := range gEntries {
		snap.Groups = append(snap.Groups, Group{
			DN:        e.DN,
			EntryUUID: e.GetAttributeValue("entryUUID"),
			CN:        e.GetAttributeValue("cn"),
			GIDNumber: atoi(e.GetAttributeValue("gidNumber")),
			Members:   e.GetAttributeValues("uniqueMember"),
		})
	}
	for _, e := range uEntries {
		snap.Users = append(snap.Users, User{
			DN:          e.DN,
			EntryUUID:   e.GetAttributeValue("entryUUID"),
			UID:         e.GetAttributeValue("uid"),
			WxUID:       e.GetAttributeValue("wxUid"),
			DisplayName: e.GetAttributeValue("displayName"),
			SN:          e.GetAttributeValue("sn"),
			GivenName:   e.GetAttributeValue("givenName"),
			Mail:        e.GetAttributeValue("mail"),
			UIDNumber:   atoi(e.GetAttributeValue("uidNumber")),
			MemberOf:    e.GetAttributeValues("memberOf"),
		})
	}
	// 稳定排序，便于幂等同步与 diff 可读。
	sort.Slice(snap.Groups, func(i, j int) bool { return snap.Groups[i].CN < snap.Groups[j].CN })
	sort.Slice(snap.Users, func(i, j int) bool { return snap.Users[i].UID < snap.Users[j].UID })
	return snap, nil
}

func atoi(s string) int {
	n, _ := strconv.Atoi(strings.TrimSpace(s))
	return n
}
