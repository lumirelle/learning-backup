// Command ldapsync 连接 LDAP，预览或同步组织架构与人员。
//
// 用法（需在能直连 LDAP:389 的网络内，如公司内网；LDAP_* 环境变量见 .env.example）：
//
//	go run ./cmd/ldapsync preview            # 只读：打印重建出的部门树 + 人员清单（不写库）
//	go run ./cmd/ldapsync sync --dry-run     # 计算同步计划但回滚（不写库）
//	go run ./cmd/ldapsync sync               # 实际同步进库（幂等）
//	go run ./cmd/ldapsync purge-demo         # 预演：列出将清理的演示/业务数据（不写库）
//	go run ./cmd/ldapsync purge-demo --confirm  # 实际清理演示数据（保留登录账号）
package main

import (
	"context"
	"encoding/json"
	"fmt"
	"os"

	"orghr/internal/infra/config"
	"orghr/internal/infra/db"
	"orghr/internal/ldapsync"
)

func main() {
	cfg := config.Load()
	if !cfg.LDAP.Enabled() {
		fmt.Fprintln(os.Stderr, "LDAP 未配置：请在 .env / 环境变量设置 LDAP_URL / LDAP_BIND_DN / LDAP_BIND_PW 等")
		os.Exit(1)
	}

	cmd := "preview"
	if len(os.Args) > 1 {
		cmd = os.Args[1]
	}
	dryRun := false
	confirm := false
	for _, a := range os.Args[2:] {
		switch a {
		case "--dry-run", "-n":
			dryRun = true
		case "--confirm":
			confirm = true
		}
	}

	// purge-demo 不读 LDAP，单独处理（默认预演，需 --confirm 才落库）。
	if cmd == "purge-demo" {
		database := db.Open(cfg.DatabaseURL)
		defer func() { _ = database.Close() }()
		rep, err := ldapsync.PurgeDemo(context.Background(), database, !confirm)
		must(err)
		if !confirm {
			fmt.Fprintln(os.Stderr, "（预演模式：未写库；确认后加 --confirm 实际清理）")
		}
		printJSON(rep)
		return
	}

	switch cmd {
	case "preview":
		cli, err := ldapsync.Dial(cfg.LDAP)
		must(err)
		defer cli.Close()
		snap, err := cli.Read()
		must(err)
		fmt.Fprintf(os.Stderr, "读取完成：%d 分组 / %d 用户\n", len(snap.Groups), len(snap.Users))
		printJSON(ldapsync.BuildPreview(snap))
	case "sync":
		database := db.Open(cfg.DatabaseURL)
		defer func() { _ = database.Close() }()
		rep, err := ldapsync.RunSync(context.Background(), database, cfg.LDAP, "cli", 0, "cli", dryRun)
		must(err)
		printJSON(rep)
	default:
		fmt.Fprintf(os.Stderr, "未知命令 %q（可用：preview / sync / purge-demo）\n", cmd)
		os.Exit(2)
	}
}

func printJSON(v any) {
	b, _ := json.MarshalIndent(v, "", "  ")
	fmt.Println(string(b))
}

func must(err error) {
	if err != nil {
		fmt.Fprintln(os.Stderr, "错误:", err)
		os.Exit(1)
	}
}
