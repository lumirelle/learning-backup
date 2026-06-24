package common_test

import (
	"net/http/httptest"
	"testing"

	"github.com/gin-gonic/gin"

	"orghr/internal/common"
)

func init() { gin.SetMode(gin.TestMode) }

func ctxWithQuery(rawQuery string) *gin.Context {
	w := httptest.NewRecorder()
	c, _ := gin.CreateTestContext(w)
	c.Request = httptest.NewRequest("GET", "/?"+rawQuery, nil)
	return c
}

func TestParsePage(t *testing.T) {
	cases := []struct {
		name         string
		query        string
		wantPage     int
		wantPageSize int
	}{
		{"默认值", "", 1, 20},
		{"正常值", "page=3&page_size=50", 3, 50},
		{"page<1 归一", "page=0", 1, 20},
		{"负页码归一", "page=-5", 1, 20},
		{"size<1 归一默认", "page_size=0", 1, 20},
		{"size 超上限封顶 100", "page_size=9999", 1, 100},
		{"非数字回退默认", "page=abc&page_size=xyz", 1, 20},
	}
	for _, tc := range cases {
		t.Run(tc.name, func(t *testing.T) {
			page, size := common.ParsePage(ctxWithQuery(tc.query))
			if page != tc.wantPage || size != tc.wantPageSize {
				t.Fatalf("ParsePage(%q) = (%d,%d)，want (%d,%d)", tc.query, page, size, tc.wantPage, tc.wantPageSize)
			}
		})
	}
}

func TestSubtreeExpr(t *testing.T) {
	// 空 scope → 恒真、无参数。
	expr, args := common.SubtreeExpr("org_path", "")
	if expr != "TRUE" || args != nil {
		t.Fatalf("空 scope 应返回 TRUE/nil，实际 %q %v", expr, args)
	}

	// 非空 scope → 严格 `/` 边界（自身 OR 后代），两个参数。
	expr, args = common.SubtreeExpr("org_path", "/hq/fin")
	if expr != "(org_path = ? OR org_path LIKE ?)" {
		t.Fatalf("表达式不符: %q", expr)
	}
	if len(args) != 2 || args[0] != "/hq/fin" || args[1] != "/hq/fin/%" {
		t.Fatalf("参数应为 [/hq/fin, /hq/fin/%%]，实际 %v", args)
	}
}

func TestAppErrorImplementsError(t *testing.T) {
	e := common.NewError(409, 1201, "冲突")
	if e.Error() != "冲突" || e.HTTP != 409 || e.Code != 1201 {
		t.Fatalf("AppError 字段异常: %+v", e)
	}
	// FailAny 应识别 AppError；这里只校验类型断言路径不 panic。
	var err error = e
	if ae, ok := err.(*common.AppError); !ok || ae.Code != 1201 {
		t.Fatalf("类型断言失败")
	}
}
