package com.example.configuration;

import com.example.service.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Configuration
public class SecurityConfiguration {

    @Bean
    public SecurityWebFilterChain webFilterChain(ServerHttpSecurity httpSecurity){
        // @formatter: off
        return httpSecurity
                .csrf().disable()
                .authorizeExchange()
                .pathMatchers(HttpMethod.POST, "/anime/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.PUT, "/anime/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.DELETE, "/anime/**").hasRole("ADMIN")
                .pathMatchers(HttpMethod.GET, "/anime/**").hasRole("USER")
                .pathMatchers("/swagger-ui.html", "swagger-ui/**", "/v3/api-docs/**", "/webjars/**")
                .permitAll()
                .anyExchange().authenticated()
                .and().formLogin()
                .and().httpBasic()
                .and().build();
        // @formatter: on
    }


    @Bean
    ReactiveAuthenticationManager authenticationManager(UserService service){
        return new UserDetailsRepositoryReactiveAuthenticationManager(service);
    }


//    @Bean
//    public MapReactiveUserDetailsService userDetailsService(){
//        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
//        UserDetails user = User.withUsername("nayeem")
//                .password(passwordEncoder.encode("ahmed"))
//                .roles("USER")
//                .build();
//
//        UserDetails admin = User.withUsername("mehedi")
//                .password(passwordEncoder.encode("hasan"))
//                .roles("ADMIN")
//                .build();
//
//        return new MapReactiveUserDetailsService(user, admin);
//    }
}
