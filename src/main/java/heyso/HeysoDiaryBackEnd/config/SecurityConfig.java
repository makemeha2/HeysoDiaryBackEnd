package heyso.HeysoDiaryBackEnd.config;

import heyso.HeysoDiaryBackEnd.auth.jwt.JwtAuthenticationFilter;
import heyso.HeysoDiaryBackEnd.auth.jwt.JwtTokenProvider;
import heyso.HeysoDiaryBackEnd.user.mapper.UserMapper;
import jakarta.servlet.DispatcherType;
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
                                .authorizeHttpRequests(auth -> {
                                        // ✅ 비동기 디스패치/에러 디스패치에서는 인증 요구하지 않음
                                        // - Async 결과를 응답으로 finalize 하는 단계에서 anonymous로 바뀌며 401나는 문제 방지
                                        auth.dispatcherTypeMatchers(DispatcherType.ASYNC, DispatcherType.ERROR)
                                                        .permitAll();

                                        // ✅ CORS preflight
                                        auth.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll();

                                        // ✅ Spring Boot 기본 에러 엔드포인트 (예외가 401로 덮이는 것 방지)
                                        auth.requestMatchers("/error").permitAll();

                                        // ✅ 인증 없이 허용할 엔드포인트
                                        auth.requestMatchers(
                                                        "/api/auth/**",
                                                        "/swagger-ui/**",
                                                        "/v3/api-docs/**").permitAll();

                                        // ✅ 사용자 정의 엔드포인트 규칙
                                        endpointSecurities.forEach(es -> es.configure(auth));

                                        // ✅ 나머지 API는 인증 필요
                                        auth.requestMatchers("/api/**").authenticated();

                                        // ✅ 그 외도 인증 필요 (원하면 permitAll로 바꿔도 됨)
                                        auth.anyRequest().authenticated();
                                })
                                // ✅ JWT 기반 API라면 basic/form 로그인은 보통 끄는 게 깔끔함
                                .httpBasic(httpBasic -> httpBasic.disable())
                                .formLogin(form -> form.disable());

                // ✅ JWT 인증 필터
                http.addFilterBefore(
                                new JwtAuthenticationFilter(jwtTokenProvider, userMapper),
                                UsernamePasswordAuthenticationFilter.class);

                return http.build();
        }
}
