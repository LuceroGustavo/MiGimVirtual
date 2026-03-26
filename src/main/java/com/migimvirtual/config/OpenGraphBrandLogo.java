package com.migimvirtual.config;

import org.springframework.ui.Model;

/**
 * Imagen para Open Graph / WhatsApp: JPEG liviano (WhatsApp suele no mostrar previews si {@code og:image} pesa cientos de KB;
 * el logo del navbar {@code /img/mgvirtual_logo1.png} se mantiene en alta calidad para la UI).
 */
public final class OpenGraphBrandLogo {

    /** Archivo para compartir: peso bajo (el PNG del navbar ronda 780 KB y WhatsApp a menudo omite la imagen). */
    public static final String PATH = "/img/og-share-migymvirtual.jpg";
    public static final String MIME_TYPE = "image/jpeg";
    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;

    private OpenGraphBrandLogo() {}

    public static void addLogoToModel(Model model, String publicBaseUrl) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        model.addAttribute("ogImageUrl", base + PATH);
        model.addAttribute("ogImageWidth", WIDTH);
        model.addAttribute("ogImageHeight", HEIGHT);
        model.addAttribute("ogImageType", MIME_TYPE);
    }
}
