package middleware

import (
	"strconv"
	"testing"

	"github.com/golang-jwt/jwt/v5"
)

// 回归测试：JWT uid 必须以字符串编码并精确还原。
// Snowflake int64(~3.2e17) 超出 JSON number(float64, 2^53) 安全范围，
// 若用数字会丢精度，导致相邻用户 id collapse、鉴权身份错乱。
func TestPrincipalFromClaimsUIDPrecision(t *testing.T) {
	ids := []int64{322943883082207232, 322943883082207234, 322943883082207235}
	seen := map[int64]bool{}
	for _, id := range ids {
		claims := jwt.MapClaims{
			"uid":      strconv.FormatInt(id, 10),
			"username": "u",
			"is_admin": false,
			"roles":    []any{"employee"},
		}
		p := principalFromClaims(claims)
		if p.UserID != id {
			t.Fatalf("uid lost precision: got %d want %d", p.UserID, id)
		}
		if seen[p.UserID] {
			t.Fatalf("distinct ids collapsed to same value: %d", p.UserID)
		}
		seen[p.UserID] = true
	}
	if len(seen) != len(ids) {
		t.Fatalf("expected %d distinct ids, got %d", len(ids), len(seen))
	}
}
