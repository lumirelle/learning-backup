package com.ats.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.net.URI;
import java.util.List;

/**
 * CORS：本机开发 + 局域网（手机 / 同 WiFi 其他设备）访问前端时的跨域。
 * <p>
 * Spring {@code allowedOriginPatterns} 对 IP 通配支持有限，因此对 RFC1918 私网段
 * 在 {@link #checkOrigin} 中额外放行（仍要求 http/https + 合法端口）。
 */
@Configuration
public class CorsConfig {

    private static final List<String> BASE_ORIGIN_PATTERNS = List.of(
            "http://localhost:*",
            "http://127.0.0.1:*",
            "https://localhost:*",
            "https://127.0.0.1:*"
    );

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration cfg = new CorsConfiguration() {
            @Override
            @Nullable
            public String checkOrigin(@Nullable String requestOrigin) {
                if (requestOrigin == null) {
                    return null;
                }
                String fromPatterns = super.checkOrigin(requestOrigin);
                if (fromPatterns != null) {
                    return fromPatterns;
                }
                if (LanOriginSupport.isPrivateNetworkOrigin(requestOrigin)) {
                    return requestOrigin;
                }
                return null;
            }
        };
        cfg.setAllowedOriginPatterns(BASE_ORIGIN_PATTERNS);
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setExposedHeaders(List.of("Authorization"));
        cfg.setAllowCredentials(true);
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return new CorsFilter(src);
    }

    /** 判断 Origin 是否为局域网 / 本机私网地址（用于 dev 同 WiFi 调试）。 */
    static final class LanOriginSupport {

        private LanOriginSupport() {
        }

        static boolean isPrivateNetworkOrigin(String origin) {
            try {
                URI uri = URI.create(origin);
                String scheme = uri.getScheme();
                if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                    return false;
                }
                String host = uri.getHost();
                if (host == null || host.isEmpty()) {
                    return false;
                }
                if ("localhost".equalsIgnoreCase(host)) {
                    return true;
                }
                return isPrivateIpv4(host);
            } catch (IllegalArgumentException ex) {
                return false;
            }
        }

        private static boolean isPrivateIpv4(String host) {
            int[] o = parseIpv4(host);
            if (o == null) {
                return false;
            }
            if (o[0] == 10) {
                return true;
            }
            if (o[0] == 192 && o[1] == 168) {
                return true;
            }
            return o[0] == 172 && o[1] >= 16 && o[1] <= 31;
        }

        @Nullable
        private static int[] parseIpv4(String host) {
            String[] parts = host.split("\\.");
            if (parts.length != 4) {
                return null;
            }
            int[] octets = new int[4];
            for (int i = 0; i < 4; i++) {
                try {
                    int v = Integer.parseInt(parts[i]);
                    if (v < 0 || v > 255) {
                        return null;
                    }
                    octets[i] = v;
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
            return octets;
        }
    }
}
