package idgen

import "testing"

// 生成大量 ID，校验唯一且趋势递增。
func TestSnowflakeUniqueAndMonotonic(t *testing.T) {
	const n = 100000
	s := New(1)
	seen := make(map[int64]struct{}, n)
	var prev int64
	for i := 0; i < n; i++ {
		id := s.Next()
		if id <= 0 {
			t.Fatalf("id should be positive, got %d", id)
		}
		if _, dup := seen[id]; dup {
			t.Fatalf("duplicate id at %d: %d", i, id)
		}
		seen[id] = struct{}{}
		if id < prev {
			t.Fatalf("id not monotonic: %d < %d", id, prev)
		}
		prev = id
	}
}

// 不同节点生成的 ID 不应碰撞。
func TestSnowflakeDistinctNodes(t *testing.T) {
	a, b := New(1), New(2)
	if a.Next() == b.Next() {
		t.Fatal("ids from distinct nodes collided")
	}
}
