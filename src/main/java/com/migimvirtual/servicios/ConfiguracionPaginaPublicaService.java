package com.migimvirtual.servicios;

import com.migimvirtual.entidades.ConfiguracionPaginaPublica;
import com.migimvirtual.repositorios.ConfiguracionPaginaPublicaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ConfiguracionPaginaPublicaService {

    @Autowired
    private ConfiguracionPaginaPublicaRepository configRepository;

    public String getValor(String clave) {
        return configRepository.findByClave(clave)
                .map(ConfiguracionPaginaPublica::getValor)
                .orElse(null);
    }

    public String getWhatsapp() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_WHATSAPP);
    }

    public String getInstagram() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_INSTAGRAM);
    }

    public String getDireccion() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_DIRECCION);
    }

    public String getDiasHorarios() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_DIAS_HORARIOS);
    }

    public String getTelefono() {
        return getValor(ConfiguracionPaginaPublica.CLAVE_TELEFONO);
    }

    public Map<String, String> getAllConfig() {
        Map<String, String> map = new HashMap<>();
        configRepository.findAll().forEach(c -> map.put(c.getClave(), c.getValor()));
        return map;
    }

    @Transactional
    public void actualizar(String clave, String valor) {
        ConfiguracionPaginaPublica entity = configRepository.findByClave(clave)
                .orElseGet(() -> {
                    ConfiguracionPaginaPublica nuevo = new ConfiguracionPaginaPublica();
                    nuevo.setClave(clave);
                    return nuevo;
                });
        entity.setValor(valor != null ? valor : "");
        configRepository.save(entity);
    }

    /**
     * Atributos comunes para {@code index-publica} y {@code planes-publica}: contacto, redes, ubicación, eslogan.
     */
    public void rellenarModeloPaginaPublica(Model model) {
        String diasHorarios = getDiasHorarios();
        model.addAttribute("diasHorarios", diasHorarios != null ? diasHorarios : "");
        List<String> diasHorariosLineas = new ArrayList<>();
        if (diasHorarios != null && !diasHorarios.isEmpty()) {
            for (String linea : diasHorarios.split("\\r?\\n|\\r")) {
                if (linea != null && !linea.trim().isEmpty()) {
                    diasHorariosLineas.add(linea.trim());
                }
            }
        }
        model.addAttribute("diasHorariosLineas", diasHorariosLineas);

        String direccion = getDireccion();
        if (direccion == null) {
            direccion = "";
        }
        model.addAttribute("direccion", direccion);

        String urlMapaRaw = getValor(ConfiguracionPaginaPublica.CLAVE_URL_MAPA);
        String urlMapa = urlMapaRaw != null && !urlMapaRaw.isBlank() ? urlMapaRaw.trim() : null;
        model.addAttribute("direccionUrl", urlMapa);

        boolean tieneDir = !direccion.isBlank();
        boolean tieneMapa = urlMapa != null;
        model.addAttribute("mostrarUbicacionPie", tieneDir || tieneMapa);
        if (tieneMapa) {
            model.addAttribute("pieUbicacionTexto", tieneDir ? direccion : "Ver ubicación en mapa");
        } else {
            model.addAttribute("pieUbicacionTexto", direccion);
        }

        model.addAttribute("whatsappUrl", buildWhatsappUrl(getWhatsapp()));
        model.addAttribute("instagramUrl", buildInstagramUrl(getInstagram()));
        model.addAttribute("tiktokUrl", buildTiktokUrl(getValor(ConfiguracionPaginaPublica.CLAVE_TIKTOK)));
        model.addAttribute("youtubeUrl", buildYoutubeUrl(getValor(ConfiguracionPaginaPublica.CLAVE_YOUTUBE)));
        model.addAttribute("facebookUrl", buildFacebookUrl(getValor(ConfiguracionPaginaPublica.CLAVE_FACEBOOK)));
        model.addAttribute("linkedinUrl", buildLinkedinUrl(getValor(ConfiguracionPaginaPublica.CLAVE_LINKEDIN)));
        model.addAttribute("twitterUrl", buildTwitterUrl(getValor(ConfiguracionPaginaPublica.CLAVE_TWITTER)));

        String email = getValor(ConfiguracionPaginaPublica.CLAVE_EMAIL_CONTACTO);
        if (email != null && !email.isBlank()) {
            email = email.trim();
            model.addAttribute("emailContacto", email);
            model.addAttribute("mailtoUrl", "mailto:" + email);
        } else {
            model.addAttribute("emailContacto", null);
            model.addAttribute("mailtoUrl", null);
        }

        String eslogan = getValor(ConfiguracionPaginaPublica.CLAVE_ESLOGAN);
        model.addAttribute("eslogan", eslogan != null && !eslogan.isBlank() ? eslogan.trim() : null);
    }

    private static String buildWhatsappUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return "https://api.whatsapp.com/send?phone=" + raw.replaceAll("[^0-9]", "");
    }

    private static String buildInstagramUrl(String raw) {
        if (raw == null || raw.isBlank() || "#".equals(raw.trim())) {
            return null;
        }
        String v = raw.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) {
            return v;
        }
        return "https://www.instagram.com/" + v.replaceFirst("^@+", "") + "/";
    }

    private static String buildTiktokUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) {
            return v;
        }
        String user = v.replaceFirst("^@+", "");
        return "https://www.tiktok.com/@" + user;
    }

    private static String buildYoutubeUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) {
            return v;
        }
        if (v.startsWith("@")) {
            return "https://www.youtube.com/" + v;
        }
        return "https://www.youtube.com/@" + v.replaceFirst("^@+", "");
    }

    private static String buildFacebookUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) {
            return v;
        }
        v = v.replaceFirst("^/+", "");
        return "https://www.facebook.com/" + v;
    }

    private static String buildLinkedinUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) {
            return v;
        }
        return "https://www.linkedin.com/" + v.replaceFirst("^/+", "");
    }

    private static String buildTwitterUrl(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String v = raw.trim();
        if (v.startsWith("http://") || v.startsWith("https://")) {
            return v;
        }
        String user = v.replaceFirst("^@+", "");
        return "https://twitter.com/" + user;
    }

    @Transactional
    public void asegurarConfigInicial() {
        List<String> claves = List.of(
                ConfiguracionPaginaPublica.CLAVE_WHATSAPP,
                ConfiguracionPaginaPublica.CLAVE_INSTAGRAM,
                ConfiguracionPaginaPublica.CLAVE_DIRECCION,
                ConfiguracionPaginaPublica.CLAVE_DIAS_HORARIOS,
                ConfiguracionPaginaPublica.CLAVE_TELEFONO,
                ConfiguracionPaginaPublica.CLAVE_URL_MAPA,
                ConfiguracionPaginaPublica.CLAVE_TIKTOK,
                ConfiguracionPaginaPublica.CLAVE_YOUTUBE,
                ConfiguracionPaginaPublica.CLAVE_FACEBOOK,
                ConfiguracionPaginaPublica.CLAVE_LINKEDIN,
                ConfiguracionPaginaPublica.CLAVE_TWITTER,
                ConfiguracionPaginaPublica.CLAVE_EMAIL_CONTACTO,
                ConfiguracionPaginaPublica.CLAVE_ESLOGAN
        );
        for (String clave : claves) {
            if (configRepository.findByClave(clave).isEmpty()) {
                String valorDefault = switch (clave) {
                    case ConfiguracionPaginaPublica.CLAVE_WHATSAPP -> "5491112345678";
                    case ConfiguracionPaginaPublica.CLAVE_INSTAGRAM -> "#";
                    case ConfiguracionPaginaPublica.CLAVE_DIRECCION -> "";
                    case ConfiguracionPaginaPublica.CLAVE_DIAS_HORARIOS ->
                            "Consultas por WhatsApp — respondemos en 24 a 48 h.\nSeguimiento y rutinas 100 % online.";
                    case ConfiguracionPaginaPublica.CLAVE_TELEFONO -> "";
                    case ConfiguracionPaginaPublica.CLAVE_URL_MAPA -> "";
                    case ConfiguracionPaginaPublica.CLAVE_TIKTOK,
                         ConfiguracionPaginaPublica.CLAVE_YOUTUBE,
                         ConfiguracionPaginaPublica.CLAVE_FACEBOOK,
                         ConfiguracionPaginaPublica.CLAVE_LINKEDIN,
                         ConfiguracionPaginaPublica.CLAVE_TWITTER,
                         ConfiguracionPaginaPublica.CLAVE_EMAIL_CONTACTO -> "";
                    case ConfiguracionPaginaPublica.CLAVE_ESLOGAN -> "Entrenamiento personalizado online";
                    default -> "";
                };
                configRepository.save(new ConfiguracionPaginaPublica(clave, valorDefault));
            }
        }
    }
}
