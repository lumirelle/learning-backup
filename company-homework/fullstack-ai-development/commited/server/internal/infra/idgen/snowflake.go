package idgen

import (
	"sync"
	"time"
)

// 自定义轻量 Snowflake：41bit 毫秒时间戳 | 10bit 节点 | 12bit 序列。
const epoch int64 = 1704067200000 // 2024-01-01 00:00:00 UTC

// Snowflake 生成趋势递增的 int64 ID。
type Snowflake struct {
	mu     sync.Mutex
	lastTs int64
	seq    int64
	node   int64
}

// New 按节点号创建生成器（节点号取低 10 位）。
func New(node int64) *Snowflake { return &Snowflake{node: node & 0x3FF} }

// Next 返回下一个 ID。
func (s *Snowflake) Next() int64 {
	s.mu.Lock()
	defer s.mu.Unlock()
	ts := time.Now().UnixMilli()
	if ts == s.lastTs {
		s.seq = (s.seq + 1) & 0xFFF
		if s.seq == 0 { // 当前毫秒序列耗尽，自旋到下一毫秒
			for ts <= s.lastTs {
				ts = time.Now().UnixMilli()
			}
		}
	} else {
		s.seq = 0
	}
	s.lastTs = ts
	return ((ts - epoch) << 22) | (s.node << 12) | s.seq
}

var def = New(1)

// NextID 使用默认生成器返回一个 ID。
func NextID() int64 { return def.Next() }
