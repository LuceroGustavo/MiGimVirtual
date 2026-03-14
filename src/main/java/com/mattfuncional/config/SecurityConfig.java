package com.mattfuncional.config;

import com.mattfuncional.repositorios.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomAuthenticationSuccessHandler successHandler;
    private final PasswordEncoder passwordEncoder;
    private final UsuarioRepository usuarioRepository;

    public SecurityConfig(CustomAuthenticationSuccessHandler successHandler,
                         PasswordEncoder passwordEncoder,
                         UsuarioRepository usuarioRepository) {
        this.successHandler = successHandler;
        this.passwordEncoder = passwordEncoder;
        this.usuarioRepository = usuarioRepository;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {
            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                // Solo usuarios con login (ADMIN, AYUDANTE, DEVELOPER). Alumnos nunca autentican.
                return usuarioRepository.findFirstByCorreoAndRolIn(username, java.util.List.of("ADMIN", "AYUDANTE", "DEVELOPER"))
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + username));
            }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/css/**", "/js/**", "/img/**", "/", "/error", "/demo", "/publica", "/planes", "/public/**").permitAll()
                        .requestMatchers("/rutinas/hoja/**").permitAll()
                        .requestMatchers("/sala/**").permitAll()
                        .requestMatchers("/profesor/usuarios-sistema/**", "/profesor/pagina-publica/**").hasAnyRole("ADMIN", "DEVELOPER")
                        .requestMatchers("/profesor/**", "/series/**", "/rutinas/**", "/exercise/**", "/ejercicios/**", "/calendario/**")
                        .hasAnyRole("ADMIN", "AYUDANTE", "DEVELOPER")
                        .anyRequest().authenticated())
                .formLogin(formLogin -> formLogin
                        .loginPage("/login")
                        .loginProcessingUrl("/logincheck")
                        .successHandler(successHandler)
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                .headers(headers -> headers
                        .frameOptions(frame -> frame.sameOrigin()))
                .csrf(csrf -> csrf.disable());

        http.authenticationProvider(authenticationProvider());
        
        http.sessionManagement(session -> session
                .sessionCreationPolicy(org.springframework.security.config.http.SessionCreationPolicy.IF_REQUIRED));

        return http.build();
    }
}