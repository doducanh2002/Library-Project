package org.aibles.authenservice.configuration;

import org.aibles.authenservice.filter.AESRequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class CommonConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
//
//    @Value("${spring.key.decrypt}")
//    private String decryptKey;
//
//    @Bean
//    public FilterRegistrationBean<AESRequestFilter> requestFilter() {
//        FilterRegistrationBean<AESRequestFilter> registrationBean = new FilterRegistrationBean<>();
//        registrationBean.setFilter(new AESRequestFilter(decryptKey));
//        registrationBean.addUrlPatterns("/api/v1/*");
//        return registrationBean;
//    }
}