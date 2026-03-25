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

    @Value("${migimvirtual.public-base-url:}")
    private String configuredPublicBaseUrl;

    public String resolvePublicBaseUrl(HttpServletRequest request) {
        String trimmed = configuredPublicBaseUrl == null ? "" : configuredPublicBaseUrl.trim();
        if (!trimmed.isEmpty()) {
            return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
        }
        int port = request.getServerPort();
        String scheme = request.getScheme();
        String host = request.getServerName();
        // Detrás de Nginx, algunos crawlers (Meta) a veces ven scheme=http; el sitio público es siempre HTTPS.
        if ("migimvirtual.detodoya.com.ar".equalsIgnoreCase(host)) {
            return "https://migimvirtual.detodoya.com.ar";
        }
        boolean defaultPort = ("http".equals(scheme) && port == 80)
                || ("https".equals(scheme) && port == 443);
        return scheme + "://" + host + (defaultPort ? "" : ":" + port);
    }
}
