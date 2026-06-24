package common

import (
	"net/http"

	"github.com/gin-gonic/gin"
)

// Resp 是统一响应信封，见 docs/04-api-design.md §1.1。
type Resp struct {
	Code      int    `json:"code"`
	Message   string `json:"message"`
	Data      any    `json:"data"`
	RequestID string `json:"request_id,omitempty"`
}

// PageData 是分页 data 结构。
type PageData struct {
	List     any `json:"list"`
	Total    int `json:"total"`
	Page     int `json:"page"`
	PageSize int `json:"page_size"`
}

func reqID(c *gin.Context) string {
	if v, ok := c.Get("request_id"); ok {
		if s, ok := v.(string); ok {
			return s
		}
	}
	return ""
}

// OK 返回成功响应。
func OK(c *gin.Context, data any) {
	c.JSON(http.StatusOK, Resp{Code: 0, Message: "ok", Data: data, RequestID: reqID(c)})
}

// Page 返回分页成功响应。
func Page(c *gin.Context, list any, total, page, size int) {
	OK(c, PageData{List: list, Total: total, Page: page, PageSize: size})
}

// Fail 返回自定义失败响应。
func Fail(c *gin.Context, status, code int, msg string) {
	c.JSON(status, Resp{Code: code, Message: msg, RequestID: reqID(c)})
}

// FailErr 按 AppError 返回失败响应。
func FailErr(c *gin.Context, e *AppError) {
	c.JSON(e.HTTP, Resp{Code: e.Code, Message: e.Message, RequestID: reqID(c)})
}

// FailAny 将任意 error 归一化为失败响应（AppError 用其码，其余按 500）。
func FailAny(c *gin.Context, err error) {
	if ae, ok := err.(*AppError); ok {
		FailErr(c, ae)
		return
	}
	FailErr(c, ErrInternal)
}
