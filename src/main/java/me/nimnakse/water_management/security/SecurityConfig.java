package me.nimnakse.water_management.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
        private final CustomUserDetailsService userDetailsService;
        private final JwtAuthenticationFilter jwtAuthenticationFilter;

        public SecurityConfig(CustomUserDetailsService userDetailsService,
                        JwtAuthenticationFilter jwtAuthenticationFilter) {
                this.userDetailsService = userDetailsService;
                this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                HttpMethod.OPTIONS, "/**")
                                                .permitAll()
                                                .requestMatchers(
                                                                "/auth/**",
                                                                "/health/**",
                                                                "/v3/api-docs/**",
                                                                "/swagger-ui/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.GET,
                                                                "/revenue-main-categories/**",
                                                                "/revenue-accounts/**",
                                                                "/fixed-asset-master-categories/**",
                                                                "/fixed-asset-templates/**",
                                                                "/inventory-master-categories/**",
                                                                "/inventory-templates/**",
                                                                "/expense-main-categories/**",
                                                                "/expense-accounts/**",
                                                                "/liability-main-categories/**",
                                                                "/liability-accounts/**",
                                                                "/org-units/**")
                                                .hasAnyAuthority("APP_SCOPE_ADMIN_PORTAL", "APP_SCOPE_BRANCH_APP")
                                                .requestMatchers(
                                                                HttpMethod.POST,
                                                                "/inventory-templates/import",
                                                                "/fixed-asset-templates/import")
                                                .hasAnyAuthority("APP_SCOPE_ADMIN_PORTAL", "APP_SCOPE_BRANCH_APP")
                                                .requestMatchers(
                                                                HttpMethod.GET,
                                                                "/fixed-asset-templates/**",
                                                                "/inventory-templates/**")
                                                .hasAnyAuthority("APP_SCOPE_ADMIN_PORTAL", "APP_SCOPE_BRANCH_APP")
                                                .requestMatchers(
                                                                "/members/**",
                                                                "/employees/**",
                                                                "/address-lines/**",
                                                                "/connections/**",
                                                                "/inventory-consumptions/**",
                                                                "/fixed-asset-consumptions/**",
                                                                "/fixed-asset-bin-cards/**",
                                                                "/inventory-consumptions/**",
                                                                "/inventory-bin-cards/**",
                                                                "/monetary-transactions/**",
                                                                "/purchase-vouchers/**",
                                                                "/purchase-voucher-drafts/**",
                                                                "/societies/**",
                                                                "/gn-divisions/**",
                                                                "/valves/**",
                                                                "/billing-zones/**",
                                                                "/clusters/**",
                                                                "/address-lines/**",
                                                                "/tariffs",
                                                                "/tariffs/**")
                                                .hasAuthority("APP_SCOPE_BRANCH_APP")
                                                .requestMatchers(
                                                                "/roles/**",
                                                                "/users/**",
                                                                "/water-projects/**",
                                                                "/org-units/**",
                                                                "/organizations/**",
                                                                "/revenue-main-categories/**",
                                                                "/revenue-accounts/**",
                                                                "/fixed-asset-master-categories/**",
                                                                "/fixed-asset-templates/**",
                                                                "/fixed-asset-consumptions/**",
                                                                "/fixed-asset-bin-cards/**",
                                                                "/inventory-master-categories/**",
                                                                "/inventory-templates/**",
                                                                "/inventory-consumptions/**",
                                                                "/inventory-bin-cards/**",
                                                                "/expense-main-categories/**",
                                                                "/expense-accounts/**",
                                                                "/liability-main-categories/**",
                                                                "/liability-accounts/**")
                                                .hasAuthority("APP_SCOPE_ADMIN_PORTAL")
                                                .anyRequest().authenticated())
                                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager() {
                DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
                provider.setPasswordEncoder(passwordEncoder());
                return new ProviderManager(provider);
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
}
