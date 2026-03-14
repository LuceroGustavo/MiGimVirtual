package com.mattfuncional.controladores;

import com.mattfuncional.dto.PizarraEstadoDTO;
import com.mattfuncional.servicios.PizarraService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/sala")
public class SalaController {

    private static final String COOKIE_PREFIX = "sala_";
    private static final int COOKIE_MAX_AGE_SECONDS = 86400; // 24 horas

    @Autowired
    private PizarraService pizarraService;

    /**
     * Vista HTML para TV (fullscreen con F11).
     * Si la pizarra tiene PIN y el cliente no tiene la cookie de desbloqueo, se muestra la pantalla de ingreso de código.
     */
    @GetMapping("/{token}")
    public String verSala(@PathVariable String token, Model model,
                         HttpServletRequest request) {
        try {
            if (pizarraService.requierePinSala(token) && !tieneCookieDesbloqueo(request, token)) {
                model.addAttribute("token", token);
                return "sala/sala-pin";
            }
            PizarraEstadoDTO estado = pizarraService.construirEstadoParaSala(token);
            model.addAttribute("estado", estado);
            model.addAttribute("token", token);
            return "sala/sala";
        } catch (Exception e) {
            model.addAttribute("error", "Pizarra no encontrada");
            return "sala/sala-error";
        }
    }

    private boolean tieneCookieDesbloqueo(HttpServletRequest request, String token) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return false;
        String nombre = COOKIE_PREFIX + token;
        for (Cookie c : cookies) {
            if (nombre.equals(c.getName()) && "ok".equals(c.getValue())) return true;
        }
        return false;
    }

    /**
     * Verifica el PIN y, si es correcto, establece la cookie para ver la sala.
     */
    @PostMapping("/verificar-pin")
    @ResponseBody
    public ResponseEntity<Void> verificarPin(@RequestBody Map<String, String> body,
                                             HttpServletResponse response) {
        String token = body.get("token");
        String codigo = body.get("codigo");
        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (!pizarraService.verificarPinSala(token, codigo)) {
            return ResponseEntity.status(401).build();
        }
        Cookie cookie = new Cookie(COOKIE_PREFIX + token, "ok");
        cookie.setPath("/sala");
        cookie.setMaxAge(COOKIE_MAX_AGE_SECONDS);
        cookie.setHttpOnly(true);
        response.addCookie(cookie);
        return ResponseEntity.ok().build();
    }

    /**
     * API JSON para polling desde la vista TV.
     */
    @GetMapping(value = "/api/{token}/estado", produces = "application/json")
    @ResponseBody
    public ResponseEntity<PizarraEstadoDTO> getEstado(@PathVariable String token) {
        try {
            PizarraEstadoDTO estado = pizarraService.construirEstadoParaSala(token);
            return ResponseEntity.ok(estado);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
