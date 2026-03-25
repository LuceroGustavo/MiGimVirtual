package com.migimvirtual.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * URL base pública (HTTPS, sin puerto interno) para Open Graph, WhatsApp y enlaces absolutos.
 * Tras Nginx, usar {@code server.forward-headers-strategy=framework} para que el request refleje
 * {@code X-Forwarded-Proto} / host. Si no fuera posible, definir {@code MIGIMVIRTUAL_PUBLIC_BASE_URL}.
 */
@Component
public class PublicBaseUrlResolver {

    private static final String PROD_PUBLIC_HOST = "migimvirtual.detodoya.com.ar";
    private static final String PROD_PUBLIC_HTTPS = "https://" + PROD_PUBLIC_HOST;

    @Value("${migimvirtual.public-base-url:}")
    private String configuredPublicBaseUrl;

    public String resolvePublicBaseUrl(HttpServletRequest request) {
        String trimmed = configuredPublicBaseUrl == null ? "" : configuredPublicBaseUrl.trim();
        if (!trimmed.isEmpty()) {
            String base = trimmed.endsWith("/") ?
                    trimmed.substring(0, trimmed.length() - 1) : trimmed;
            return upgradeHttpToHttpsForProdHost(base);
        }
        int port = request.getServerPort();
        String scheme = request.getScheme();
        String host = request.getServerName();
        // Detrás de Nginx, algunos crawlers (Meta) a veces ven scheme=http; el sitio público es siempre HTTPS.
        if (PROD_PUBLIC_HOST.equalsIgnoreCase(host)) {
            return PROD_PUBLIC_HTTPS;
        }
        boolean defaultPort = ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);
        String built = scheme + "://" + host + (defaultPort ? "" : ":" + port);
        return upgradeHttpToHttpsForProdHost(built);
    }

    /**
     * Evita og:url / og:image en http:// si en .env quedó {@code MIGIMVIRTUAL_PUBLIC_BASE_URL=http://...}
     * o el request llega como HTTP detrás del proxy.
     */
    static String upgradeHttpToHttpsForProdHost(String baseUrl) {
        if (baseUrl == null || baseUrl.isEmpty()) {
            return baseUrl;
        }
        String lower = baseUrl.toLowerCase();
        if (lower.startsWith("http://" + PROD_PUBLIC_HOST)) {
            return "https://" + baseUrl.substring("http://".length());
        }
        return baseUrl;
    }
}
