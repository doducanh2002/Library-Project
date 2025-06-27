package org.aibles.authenservice.configuration;

import org.aibles.authenservice.entity.AccountRole;
import org.aibles.authenservice.entity.Role;
import org.aibles.authenservice.filter.JwtRequestFilter;
import org.aibles.authenservice.filter.TokenTypeFilter;
import org.aibles.authenservice.repository.AccountRepository;
import org.aibles.authenservice.repository.AccountRoleRepository;
import org.aibles.authenservice.repository.RoleRepository;
import org.aibles.authenservice.facade.AuthService;
import org.aibles.authenservice.utill.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final RoleRepository roleRepository;
    private final JwtUtil jwtUtil;
    private final AuthService authService;

    @Autowired
    public SecurityConfig(AccountRepository accountRepository,
                          AccountRoleRepository accountRoleRepository,
                          RoleRepository roleRepository,
                          JwtUtil jwtUtil,
                          AuthService authService) {
        this.accountRepository = accountRepository;
        this.accountRoleRepository = accountRoleRepository;
        this.roleRepository = roleRepository;
        this.jwtUtil = jwtUtil;
        this.authService = authService;
    }

    @Bean
    public JwtRequestFilter jwtRequestFilter() {
        return new JwtRequestFilter(userDetailsService(), jwtUtil);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/register").permitAll()
                        .requestMatchers("/api/v1/auth/sendotp").permitAll()
                        .requestMatchers("/api/v1/auth/active").permitAll()
                        .requestMatchers("/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/auth/forgot-password").permitAll()
                        .requestMatchers("/api/v1/auth/reset-password").permitAll()
                        .requestMatchers("/api/v1/test/**").hasRole("USER")
                        .requestMatchers("/api/v1/auth/jwk/token").permitAll()
                        .requestMatchers("/api/v1/auth/change-password").hasRole("USER")
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(csrf -> csrf.disable())
//                .formLogin(form -> form.disable())
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/v1/auth/**").permitAll()
//                        .requestMatchers("/login/oauth2/code/**").permitAll()
//                        .anyRequest().authenticated()
//                )
//                .sessionManagement(session -> session
//                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
//                )
//                .oauth2Login(oauth2 -> oauth2
//                        .loginPage("/api/v1/auth/google/callback")
//                        .userInfoEndpoint(userInfo -> userInfo
//                                .oidcUserService(oidcUserService())
//                        )
//                        .successHandler((request, response, authentication) -> {
//                            String email = authentication.getName();
//                            var loginResponse = authService.loginWithGoogle(email);
//                            response.setContentType("application/json");
//                            response.getWriter().write(String.format(
//                                    "{\"status\":\"SUCCESS\",\"timestamp\":%d,\"data\":{\"accessToken\":\"%s\",\"refreshToken\":\"%s\",\"accessTokenExpiration\":%d,\"refreshTokenExpiration\":%d}}",
//                                    System.currentTimeMillis(),
//                                    loginResponse.getData().getAccessToken(),
//                                    loginResponse.getData().getRefreshToken(),
//                                    loginResponse.getData().getAccessTokenExpiration(),
//                                    loginResponse.getData().getRefreshTokenExpiration()
//                            ));
//                        })
//                )
//                .addFilterBefore(tokenTypeFilter(), UsernamePasswordAuthenticationFilter.class)
//                .addFilterBefore(jwtRequestFilter(), UsernamePasswordAuthenticationFilter.class);
//
//        return http.build();
//    }

    @Bean
    public TokenTypeFilter tokenTypeFilter() {
        return new TokenTypeFilter();
    }


    @Bean
    public UserDetailsService userDetailsService() {
        return username -> accountRepository.findByUsername(username)
                .map(account -> {
                    List<String> roleIds = accountRoleRepository.findByAccountId(account.getId())
                            .stream()
                            .map(AccountRole::getRoleId)
                            .collect(Collectors.toList());

                    List<String> roles = roleRepository.findAllById(roleIds)
                            .stream()
                            .map(Role::getName)
                            .collect(Collectors.toList());

                    return org.springframework.security.core.userdetails.User
                            .withUsername(account.getUsername())
                            .password(account.getPassword())
                            .roles(roles.toArray(new String[0]))
                            .build();
                })
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public OidcUserService oidcUserService() {
        return new OidcUserService();
    }
}