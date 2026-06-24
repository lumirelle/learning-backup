package api

import (
	"net/http/httptest"
	"testing"

	"github.com/rs/zerolog"

	"orghr/internal/infra/db"
)

// 路由可达性回归：所有已声明的写动作端点都必须真正可路由。
// 无 token 时已注册路由经 Auth 中间件返回 401；未注册路由返回 404。
// （历史上排查过一次「路由已注册却 404」的幽灵，实为 stale 进程，此测试守住回归。）
func TestActionRoutesReachable(t *testing.T) {
	r := NewRouter(Deps{
		DB:  db.Open("postgres://x:x@localhost:5433/x?sslmode=disable"),
		Log: zerolog.Nop(), JWTSecret: "x", JWTExpireHours: 1,
	})
	reachable := []struct{ m, p string }{
		{"POST", "/api/v1/contracts/123/renew"},
		{"DELETE", "/api/v1/contracts/123"}, // terminate
		{"POST", "/api/v1/processes/123/approve"},
		{"POST", "/api/v1/processes/123/reject"},
		{"POST", "/api/v1/archives/123/borrow"},
		{"POST", "/api/v1/borrows/123"},
	}
	for _, tc := range reachable {
		w := httptest.NewRecorder()
		r.ServeHTTP(w, httptest.NewRequest(tc.m, tc.p, nil))
		if w.Code == 404 {
			t.Errorf("%s %s unreachable (404); route not wired", tc.m, tc.p)
		}
	}
	// 反向：未注册路径应 404
	w := httptest.NewRecorder()
	r.ServeHTTP(w, httptest.NewRequest("POST", "/api/v1/contracts/123/nope", nil))
	if w.Code != 404 {
		t.Errorf("unexpected: bogus route returned %d, want 404", w.Code)
	}
}
