# infra/jwt — JWT RS256 密钥对

## 用途

后端 `ats-backend` 在 M1 阶段开始使用 **RS256（非对称）** 签发 / 验证 JWT access token。
本目录存放 keypair，由 `application.yml` 通过 `${ATS_JWT_KEY_DIR}` 引用。

## 文件

| 文件 | 用途 | git 处理 |
|---|---|---|
| `dev-private.pem` | **开发专用**私钥（PKCS#8，2048-bit RSA） | ✅ commit（公开 OK，仅 dev） |
| `dev-public.pem` | **开发专用**公钥 | ✅ commit |
| `*.prod.pem`、`*.local.pem` | 真实环境密钥 | ❌ 已 gitignore，绝不 commit |

> ⚠️ **dev keypair 仅用于开发环境**。任何打到生产 / 演示环境的部署都必须替换为单独生成的密钥，并通过 secret 管理（K8s Secret / Docker Secret / 云厂商 KMS）注入。

## 路径策略

| 环境 | `ATS_JWT_KEY_DIR` | 文件名 |
|---|---|---|
| 本地开发（`bun run be:dev`） | `../infra/jwt`（默认） | `dev-private.pem` / `dev-public.pem` |
| Docker 部署 | `/etc/ats/jwt`（compose 挂载） | `private.pem` / `public.pem`（重命名） |
| 生产 K8s | `/etc/ats/jwt`（Secret volume） | 同上 |

> 后端通过 `ats.jwt.private-key-path` / `ats.jwt.public-key-path` 读取，env 覆盖优先级最高。

## 重新生成（dev keypair 失效或需要轮换时）

```bash
# 在仓库根目录执行
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out infra/jwt/dev-private.pem
openssl rsa -pubout -in infra/jwt/dev-private.pem -out infra/jwt/dev-public.pem
```

> Windows + Git for Windows 用户：openssl 路径通常为 `"C:\Program Files\Git\usr\bin\openssl.exe"`，PowerShell 里调用前加 `&` 即可。

## 生产环境生成（一次性）

```bash
# 生成
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:4096 -out /tmp/private.prod.pem
openssl rsa -pubout -in /tmp/private.prod.pem -out /tmp/public.prod.pem

# 装载到 K8s Secret（举例）
kubectl create secret generic ats-jwt \
  --from-file=private.pem=/tmp/private.prod.pem \
  --from-file=public.pem=/tmp/public.prod.pem

# 立即从本地擦除
shred -u /tmp/private.prod.pem /tmp/public.prod.pem
```

## 密钥泄露应急

如果 dev keypair 被怀疑泄露到生产数据：

1. 立即重新生成：`openssl genpkey ...`（见上）
2. 后端重启 → 所有现存 access token 失效（refresh token 在 DB 标记 revoked）
3. 前端用户被强制重登
4. 审计 `refresh_tokens` 表近 24h 异常 IP / UA
