package auth

import (
	"context"
	"encoding/json"
	"net/http"
	"net/http/httptest"
	"sync/atomic"
	"testing"

	"orghr/internal/common"
	"orghr/internal/infra/idgen"
	"orghr/internal/testutil"
)

// ---- 签名（纯单测）----

// 期望值由独立实现（Python hmac/hashlib，按文档示例公式）生成，
// 防止「自己实现自己验证」的循环：
//
//	canonical = lower("POST\n/openapi/sso/verifyToken\nproj-Demo01\n1716000000")
//	hmac_sha256("0123456789abcdef0123456789abcdef", canonical)
func TestSignSSOVector(t *testing.T) {
	got := signSSO("0123456789abcdef0123456789abcdef",
		"POST", "/openapi/sso/verifyToken", "proj-Demo01", "1716000000")
	want := "cc4f1aae8e75b861394e016788f922d71525eb25afe1a44fe9de5103b5db79e1"
	if got != want {
		t.Fatalf("signSSO = %s, want %s（大小写折叠或拼接顺序与文档不符）", got, want)
	}
}

// ---- 客户端（mock SSO 服务）----

// mockSSO 起一个假 SSO 后端：verifyToken 校验签名头并返回配置的用户；
// logout 仅计数（验证「停用账号需通知注销」语义）。
type mockSSO struct {
	srv        *httptest.Server
	secret     string
	project    string
	user       *SSOUser // nil = verifyToken 返回 code 0
	logoutHits atomic.Int64
}

func newMockSSO(t *testing.T, user *SSOUser) *mockSSO {
	t.Helper()
	m := &mockSSO{secret: "0123456789abcdef0123456789abcdef", project: "proj1", user: user}
	mux := http.NewServeMux()
	mux.HandleFunc("/openapi/sso/verifyToken", func(w http.ResponseWriter, r *http.Request) {
		ts := r.Header.Get("X-Timestamp")
		want := signSSO(m.secret, r.Method, "/openapi/sso/verifyToken", m.project, ts)
		if ts == "" || r.Header.Get("X-Signature") != want {
			_ = json.NewEncoder(w).Encode(map[string]any{"code": 0, "message": "签名错误"})
			return
		}
		if m.user == nil {
			_ = json.NewEncoder(w).Encode(map[string]any{"code": 0, "message": "登录无效"})
			return
		}
		_ = json.NewEncoder(w).Encode(map[string]any{"code": 1, "data": m.user})
	})
	mux.HandleFunc("/openapi/sso/logout", func(w http.ResponseWriter, _ *http.Request) {
		m.logoutHits.Add(1)
		_ = json.NewEncoder(w).Encode(map[string]any{"code": 1})
	})
	m.srv = httptest.NewServer(mux)
	t.Cleanup(m.srv.Close)
	return m
}

func (m *mockSSO) client() *SSOClient { return NewSSOClient(m.srv.URL, m.project, m.secret) }

func TestSSOClientVerifyToken(t *testing.T) {
	m := newMockSSO(t, &SSOUser{WxUID: "ZhangSan", Name: "张三", Email: "zs@x.com"})
	u, err := m.client().VerifyToken(context.Background(), "tok-1")
	if err != nil {
		t.Fatalf("VerifyToken: %v", err)
	}
	if u.WxUID != "ZhangSan" || u.Name != "张三" {
		t.Fatalf("用户解析不符: %+v", u)
	}

	m.user = nil // SSO 侧返回 code 0
	if _, err := m.client().VerifyToken(context.Background(), "tok-bad"); err == nil {
		t.Fatal("code 0 应返回错误")
	}
}

func TestNewSSOClientDisabled(t *testing.T) {
	if NewSSOClient("https://x", "", "s") != nil || NewSSOClient("https://x", "p", "") != nil {
		t.Fatal("project/secret 缺失时应返回 nil（禁用）")
	}
}

// ---- LoginWithSSO（DB 集成，测试库不可达自动 Skip）----

func ssoTestService(t *testing.T, m *mockSSO, autoProvision bool) *Service {
	t.Helper()
	db := testutil.DB(t)
	testutil.Reset(t, db)
	return NewService(db, "test-secret", 1).WithSSO(m.client(), autoProvision)
}

func mustInsertUser(t *testing.T, s *Service, u *User) {
	t.Helper()
	u.ID = idgen.NextID()
	if u.Roles == nil {
		u.Roles = []string{}
	}
	if _, err := s.db.NewInsert().Model(u).Exec(context.Background()); err != nil {
		t.Fatalf("插入用户失败: %v", err)
	}
}

func appCode(err error) int {
	var ae *common.AppError
	if e, ok := err.(*common.AppError); ok {
		ae = e
	}
	if ae == nil {
		return 0
	}
	return ae.Code
}

func TestLoginWithSSOByWxUID(t *testing.T) {
	m := newMockSSO(t, &SSOUser{WxUID: "ZhangSan", Name: "张三"})
	s := ssoTestService(t, m, false)
	mustInsertUser(t, s, &User{Username: "zhangsan", Name: "张三", PasswordHash: "x", WxUID: "ZhangSan", OrgPath: "/hq", IsActive: true})

	tok, u, err := s.LoginWithSSO(context.Background(), "tok-1")
	if err != nil {
		t.Fatalf("登录失败: %v", err)
	}
	if tok == "" || u.Username != "zhangsan" {
		t.Fatalf("应签发 JWT 并命中已有账号，got user=%+v", u)
	}
}

func TestLoginWithSSOBindByEmail(t *testing.T) {
	m := newMockSSO(t, &SSOUser{WxUID: "LiSi", Name: "李四", Email: "lisi@x.com"})
	s := ssoTestService(t, m, false)
	mustInsertUser(t, s, &User{Username: "lisi", Name: "李四", PasswordHash: "x", Email: "lisi@x.com", OrgPath: "/hq", IsAdmin: true, IsActive: true})

	_, u, err := s.LoginWithSSO(context.Background(), "tok-1")
	if err != nil {
		t.Fatalf("登录失败: %v", err)
	}
	if u.WxUID != "LiSi" {
		t.Fatal("email 唯一命中应回写 wx_uid 完成绑定")
	}
	// 绑定只写 wx_uid，不得动既有字段
	fresh, err := s.Me(context.Background(), u.ID)
	if err != nil || fresh.WxUID != "LiSi" || !fresh.IsAdmin || fresh.PasswordHash != "x" {
		t.Fatalf("绑定后字段异常: %+v (err=%v)", fresh, err)
	}
}

func TestLoginWithSSOEmailAmbiguous(t *testing.T) {
	m := newMockSSO(t, &SSOUser{WxUID: "WangWu", Name: "王五", Email: "dup@x.com"})
	s := ssoTestService(t, m, false)
	mustInsertUser(t, s, &User{Username: "w1", Name: "王五甲", PasswordHash: "x", Email: "dup@x.com", OrgPath: "/hq", IsActive: true})
	mustInsertUser(t, s, &User{Username: "w2", Name: "王五乙", PasswordHash: "x", Email: "dup@x.com", OrgPath: "/hq", IsActive: true})

	_, _, err := s.LoginWithSSO(context.Background(), "tok-1")
	if appCode(err) != 1106 {
		t.Fatalf("邮箱撞多账号应拒绝自动绑定(1106)，got %v", err)
	}
}

func TestLoginWithSSOUnknownRejected(t *testing.T) {
	m := newMockSSO(t, &SSOUser{WxUID: "Nobody", Name: "无名"})
	s := ssoTestService(t, m, false)

	_, _, err := s.LoginWithSSO(context.Background(), "tok-1")
	if appCode(err) != 1104 {
		t.Fatalf("默认（不自动开通）应拒绝无账号登录(1104)，got %v", err)
	}
	// 「本系统无账号」不应注销用户的全局 SSO 会话
	if m.logoutHits.Load() != 0 {
		t.Fatal("无账号拒登不应通知 SSO 注销")
	}
}

func TestLoginWithSSOAutoProvision(t *testing.T) {
	m := newMockSSO(t, &SSOUser{WxUID: "NewGuy", Name: "新人", Email: "new@x.com"})
	s := ssoTestService(t, m, true)

	_, u, err := s.LoginWithSSO(context.Background(), "tok-1")
	if err != nil {
		t.Fatalf("自动开通登录失败: %v", err)
	}
	// 最小权限：无密码、非管理员、org_path 哨兵值（数据范围为空集，非「不限范围」）
	if u.PasswordHash != "" || u.IsAdmin || u.OrgPath != ssoUnassignedPath {
		t.Fatalf("自动开通账号权限过宽: %+v", u)
	}
	// 二次登录命中同一账号，不重复开通
	_, u2, err := s.LoginWithSSO(context.Background(), "tok-2")
	if err != nil || u2.ID != u.ID {
		t.Fatalf("二次登录应命中既有账号: %v", err)
	}
}

func TestLoginWithSSOInactiveNotifiesLogout(t *testing.T) {
	m := newMockSSO(t, &SSOUser{WxUID: "Banned", Name: "封停"})
	s := ssoTestService(t, m, false)
	mustInsertUser(t, s, &User{Username: "banned", Name: "封停", PasswordHash: "x", WxUID: "Banned", OrgPath: "/hq", IsActive: false})

	_, _, err := s.LoginWithSSO(context.Background(), "tok-1")
	if appCode(err) != 1103 {
		t.Fatalf("停用账号应拒登(1103)，got %v", err)
	}
	if m.logoutHits.Load() != 1 {
		t.Fatalf("停用账号拒登应通知 SSO 注销该 token，logout 命中 %d 次", m.logoutHits.Load())
	}
}
