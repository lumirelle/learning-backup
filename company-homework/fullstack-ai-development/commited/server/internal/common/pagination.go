package common

import (
	"strconv"

	"github.com/gin-gonic/gin"
)

// ParsePage 解析分页参数，page 从 1 起，page_size 上限 100。
func ParsePage(c *gin.Context) (page, size int) {
	page, _ = strconv.Atoi(c.DefaultQuery("page", "1"))
	size, _ = strconv.Atoi(c.DefaultQuery("page_size", "20"))
	if page < 1 {
		page = 1
	}
	if size < 1 {
		size = 20
	}
	if size > 100 {
		size = 100
	}
	return page, size
}
