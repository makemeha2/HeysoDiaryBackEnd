package heyso.HeysoDiaryBackEnd.config;

import heyso.HeysoDiaryBackEnd.auth.jwt.JwtAuthenticationFilter;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtTokenProvider;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

        private final JwtTokenProvider jwtTokenProvider;
        private final UserMapper userMapper;
        private final List<EndpointSecurity> endpointSecurities;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

                http
                                .cors(Customizer.withDefaults())
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                                .permitAll());
                http
                                .authorizeHttpRequests(auth -> {
                                        auth
                                                        .requestMatchers(
                                                                        "/api/auth/**",
                                                                        "/swagger-ui/**",
                                                                        "/v3/api-docs/**")
                                                        .permitAll();

                                        endpointSecurities.forEach(es -> es.configure(auth));

                                        auth.requestMatchers("/api/**").authenticated();

                                        auth.anyRequest().authenticated();
                                })
                                .httpBasic(Customizer.withDefaults())
                                .formLogin(form -> form.disable());

                http.addFilterBefore(
                                new JwtAuthenticationFilter(jwtTokenProvider, userMapper),
                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
