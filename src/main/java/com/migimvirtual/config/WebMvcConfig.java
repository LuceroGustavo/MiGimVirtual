package com.migimvirtual.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Asegura que GET /login muestre siempre la plantilla personalizada (login.html)
 * y no la página por defecto de Spring Security ("Please sign in").
 * También evita que el navegador cachee el panel del profesor para que siempre se vean datos actuales.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/login").setViewName("login");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
                String path = request.getRequestURI();
                if (path != null && path.startsWith("/profesor")) {
                    if ("/profesor/dashboard".equals(path) || path.matches("/profesor/\\d+")) {
                        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                        response.setHeader("Pragma", "no-cache");
                        response.setDateHeader("Expires", 0);
                    }
                }
                return true;
            }
        }).addPathPatterns("/profesor/dashboard", "/profesor/*");
    }
}
