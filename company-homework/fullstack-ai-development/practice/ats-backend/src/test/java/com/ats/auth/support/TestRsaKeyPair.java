package com.ats.auth.support;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * 测试专用：进程内生成 2048 位 RSA keypair，并按 PEM 写到给定目录。
 * 避免测试依赖 infra/jwt/dev-*.pem，确保 CI / 离线环境也能跑。
 */
public final class TestRsaKeyPair {

    public final RSAPrivateKey privateKey;
    public final RSAPublicKey publicKey;

    private TestRsaKeyPair(RSAPrivateKey priv, RSAPublicKey pub) {
        this.privateKey = priv;
        this.publicKey = pub;
    }

    /** 生成 keypair 并写入 {@code dir/privFile} 和 {@code dir/pubFile}（PKCS#8 / X.509 PEM）*/
    public static TestRsaKeyPair generateAndWriteTo(Path dir, String privFile, String pubFile) throws Exception {
        KeyPairGenerator g = KeyPairGenerator.getInstance("RSA");
        g.initialize(2048);
        KeyPair kp = g.generateKeyPair();
        RSAPrivateKey priv = (RSAPrivateKey) kp.getPrivate();
        RSAPublicKey pub = (RSAPublicKey) kp.getPublic();

        Files.createDirectories(dir);
        Files.writeString(dir.resolve(privFile), toPem("PRIVATE KEY", priv.getEncoded()), StandardCharsets.UTF_8);
        Files.writeString(dir.resolve(pubFile), toPem("PUBLIC KEY", pub.getEncoded()), StandardCharsets.UTF_8);

        return new TestRsaKeyPair(priv, pub);
    }

    private static String toPem(String label, byte[] der) {
        String b64 = Base64.getEncoder().encodeToString(der);
        StringBuilder sb = new StringBuilder();
        sb.append("-----BEGIN ").append(label).append("-----\n");
        for (int i = 0; i < b64.length(); i += 64) {
            sb.append(b64, i, Math.min(i + 64, b64.length())).append('\n');
        }
        sb.append("-----END ").append(label).append("-----\n");
        return sb.toString();
    }
}
