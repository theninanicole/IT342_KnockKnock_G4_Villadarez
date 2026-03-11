package com.knockknock.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.*;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http
            .cors().and()
            .csrf().disable()

            .authorizeRequests()

            // Allow preflight CORS requests
            .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()

            // Allow authentication endpoints
            .antMatchers("/api/auth/**").permitAll()

            // Allow H2 console
            .antMatchers("/h2-console/**").permitAll()

            .anyRequest().authenticated();

        // Needed for H2 console
        http.headers().frameOptions().disable();
    }
}