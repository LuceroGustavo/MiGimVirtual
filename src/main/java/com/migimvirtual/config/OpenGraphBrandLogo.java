package com.migimvirtual.config;

import org.springframework.ui.Model;

/**
 * Open Graph / WhatsApp: landing y planes usan JPEG liviano; la hoja de rutina usa {@link #RUTINA_SHARE_PATH}.
 */
public final class OpenGraphBrandLogo {

    /** Archivo para compartir (home/planes): peso bajo (el PNG del navbar ronda 780 KB y WhatsApp a menudo omite la imagen). */
    public static final String PATH = "/img/og-share-migymvirtual.jpg";
    public static final String MIME_TYPE = "image/jpeg";
    public static final int WIDTH = 512;
    public static final int HEIGHT = 512;

    /**
     * Imagen solo para preview al compartir enlace de rutina (hoja pública o vista profesor con misma plantilla).
     * Debe coincidir con dimensiones reales del PNG (Meta/WhatsApp).
     */
    public static final String RUTINA_SHARE_PATH = "/img/envio_rutina.png";
    public static final String RUTINA_SHARE_MIME_TYPE = "image/png";
    public static final int RUTINA_SHARE_WIDTH = 600;
    public static final int RUTINA_SHARE_HEIGHT = 367;

    private OpenGraphBrandLogo() {}

    public static void addLogoToModel(Model model, String publicBaseUrl) {
        applyOgImage(model, publicBaseUrl, PATH, WIDTH, HEIGHT, MIME_TYPE);
    }

    /** {@code og:image} para {@code rutinas/verRutina.html} (WhatsApp / redes al compartir la rutina). */
    public static void addRutinaShareImageToModel(Model model, String publicBaseUrl) {
        applyOgImage(model, publicBaseUrl, RUTINA_SHARE_PATH, RUTINA_SHARE_WIDTH, RUTINA_SHARE_HEIGHT, RUTINA_SHARE_MIME_TYPE);
    }

    private static void applyOgImage(Model model, String publicBaseUrl, String path, int width, int height, String mimeType) {
        String base = publicBaseUrl == null ? "" : publicBaseUrl.trim();
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        model.addAttribute("ogImageUrl", base + path);
        model.addAttribute("ogImageWidth", width);
        model.addAttribute("ogImageHeight", height);
        model.addAttribute("ogImageType", mimeType);
    }
}
