package common

// AppError 携带 HTTP 状态码与业务错误码，错误码表见 docs/04-api-design.md §3。
type AppError struct {
	HTTP    int
	Code    int
	Message string
}

func (e *AppError) Error() string { return e.Message }

// NewError 构造业务错误。
func NewError(http, code int, msg string) *AppError {
	return &AppError{HTTP: http, Code: code, Message: msg}
}

// 通用错误（与文档错误码区间一致）。
var (
	ErrBadRequest   = NewError(400, 1001, "参数错误")
	ErrValidation   = NewError(400, 1002, "校验失败")
	ErrNotFound     = NewError(404, 1003, "资源不存在")
	ErrConflict     = NewError(409, 1004, "资源冲突")
	ErrUnauthorized = NewError(401, 1101, "未登录")
	ErrTokenInvalid = NewError(401, 1102, "token 失效")
	ErrForbidden    = NewError(403, 1103, "无权限")
	ErrInternal     = NewError(500, 5000, "内部错误")
)
