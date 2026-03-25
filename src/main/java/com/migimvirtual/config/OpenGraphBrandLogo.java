package com.migimvirtual.config;

import org.springframework.ui.Model;

/**
 * Logo para Open Graph / WhatsApp: misma ruta estática que el navbar ({@code /img/mgvirtual_logo1.png}).
 */
public final class OpenGraphBrandLogo {

    public static final String PATH = "/img/mgvirtual_logo1.png";
    /** Dimensiones reales de {@code mgvirtual_logo1.png} (Meta/WhatsApp las usan para la tarjeta). */
    public static final int WIDTH = 738;
    public static final int HEIGHT = 738;

    private OpenGraphBrandLogo() {}

    public static void addLogoToModel(Model model, String publicBaseUrl) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        model.addAttribute("ogImageUrl", base + PATH);
        model.addAttribute("ogImageWidth", WIDTH);
        model.addAttribute("ogImageHeight", HEIGHT);
    }
}
