package com.mycompany.myapp.config;

import static org.springframework.security.config.Customizer.withDefaults;

import com.mycompany.myapp.security.*;
import com.mycompany.myapp.web.filter.SpaWebFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer.FrameOptionsConfig;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;
import tech.jhipster.config.JHipsterProperties;

@Configuration
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfiguration {

    private final JHipsterProperties jHipsterProperties;

    public SecurityConfiguration(JHipsterProperties jHipsterProperties) {
        this.jHipsterProperties = jHipsterProperties;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                if (encodedPassword == null) {
                    return false;
                }
                if (encodedPassword.startsWith("$2")) {
                    return bcrypt.matches(rawPassword, encodedPassword);
                }
                return rawPassword != null && encodedPassword.contentEquals(rawPassword);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, MvcRequestMatcher.Builder mvc) throws Exception {
        http
            .cors(withDefaults())
            .csrf(csrf -> csrf.disable())
            .addFilterAfter(new SpaWebFilter(), BasicAuthenticationFilter.class)
            .headers(headers ->
                headers
                    .contentSecurityPolicy(csp -> csp.policyDirectives(jHipsterProperties.getSecurity().getContentSecurityPolicy()))
                    .frameOptions(FrameOptionsConfig::sameOrigin)
                    .referrerPolicy(referrer -> referrer.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                    .permissionsPolicyHeader(permissions ->
                        permissions.policy(
                            "camera=(), fullscreen=(self), geolocation=(), gyroscope=(), magnetometer=(), microphone=(), midi=(), payment=(), sync-xhr=()"
                        )
                    )
            )
            .authorizeHttpRequests(authz ->
                // prettier-ignore
                authz
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/index.html"),
                                mvc.pattern(HttpMethod.GET, "/*.js"), mvc.pattern(HttpMethod.GET, "/*.txt"),
                                mvc.pattern(HttpMethod.GET, "/*.json"), mvc.pattern(HttpMethod.GET, "/*.map"),
                                mvc.pattern(HttpMethod.GET, "/*.css"))
                        .permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/*.ico"), mvc.pattern(HttpMethod.GET, "/*.png"),
                                mvc.pattern(HttpMethod.GET, "/*.svg"), mvc.pattern(HttpMethod.GET, "/*.webapp"))
                        .permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/app/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/i18n/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/content/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/swagger-ui/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/authenticate")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/auth/login")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/authenticate")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/register")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/auth/register")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/auth/forgot-password")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/activate")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/account/reset-password/init")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/account/reset-password/finish")).permitAll()
                        // Cho phép truy cập công khai: phim, suất chiếu, F&B (read-only)
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/movies/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/phims/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/phim/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/showtimes/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/suat-chieus/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/danh-gias")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/danh-gias/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/dich-vu-fbs/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/fb/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/ghes/showtime/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/dat-ve/seat/hold")).authenticated()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/dat-ve/seat/release")).authenticated()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/booking/create")).authenticated()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/giam-gias/validate/**")).authenticated()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/phong-chieus/**")).permitAll()
                        // Cho phép chatbot công khai
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/chatbot/**")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/payment/vnpay/return")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.GET, "/api/payment/vnpay/ipn")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/payment/vnpay/create-url/**")).authenticated()
                        // Payment callback (công khai)
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/payment/callback")).permitAll()
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/payment/timeout")).permitAll()

                        // Admin API restrictions cho các thao tác Thêm/Sửa/Xóa
                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/phims/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/phims/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/phims/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/phims/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/suat-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/suat-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/suat-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/suat-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/phong-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/phong-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/phong-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/phong-chieus/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/dich-vu-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/dich-vu-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/dich-vu-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/dich-vu-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/chi-tiet-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/chi-tiet-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/chi-tiet-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/chi-tiet-fbs/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/hoa-dons/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/hoa-dons/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/hoa-dons/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/hoa-dons/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/ves/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/ves/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/ves/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/ves/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/ghes/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/ghes/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/ghes/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/ghes/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/danh-gias/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/danh-gias/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/danh-gias/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        .requestMatchers(mvc.pattern(HttpMethod.POST, "/api/giam-gias"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PUT, "/api/giam-gias/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.PATCH, "/api/giam-gias/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern(HttpMethod.DELETE, "/api/giam-gias/**"))
                        .hasAuthority(AuthoritiesConstants.ADMIN)

                        // Admin endpoints
                        .requestMatchers(mvc.pattern("/api/admin/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern("/api/**")).authenticated()
                        .requestMatchers(mvc.pattern("/v3/api-docs/**")).hasAuthority(AuthoritiesConstants.ADMIN)
                        .requestMatchers(mvc.pattern("/management/health")).permitAll()
                        .requestMatchers(mvc.pattern("/management/health/**")).permitAll()
                        .requestMatchers(mvc.pattern("/management/info")).permitAll()
                        .requestMatchers(mvc.pattern("/management/prometheus")).permitAll()
                        .requestMatchers(mvc.pattern("/management/**")).hasAuthority(AuthoritiesConstants.ADMIN)
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .exceptionHandling(exceptions ->
                exceptions
                    .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint())
                    .accessDeniedHandler(new BearerTokenAccessDeniedHandler())
            )
            .oauth2ResourceServer(oauth2 -> oauth2.jwt(withDefaults()));
        return http.build();
    }

    @Bean
    MvcRequestMatcher.Builder mvc(HandlerMappingIntrospector introspector) {
        return new MvcRequestMatcher.Builder(introspector);
    }
}
