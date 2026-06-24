package org

import "testing"

func TestMoveReqIDs(t *testing.T) {
	cases := []struct {
		name       string
		req        moveReq
		wantID     int64
		wantParent int64
	}{
		{"含父节点", moveReq{ID: "123", NewParentID: "456"}, 123, 456},
		{"父节点为空→0（移到顶层）", moveReq{ID: "123", NewParentID: ""}, 123, 0},
		{"非法 id→0", moveReq{ID: "abc"}, 0, 0},
	}
	for _, c := range cases {
		t.Run(c.name, func(t *testing.T) {
			id, parent := c.req.ids()
			if id != c.wantID || parent != c.wantParent {
				t.Fatalf("ids() = (%d,%d)，want (%d,%d)", id, parent, c.wantID, c.wantParent)
			}
		})
	}
}
