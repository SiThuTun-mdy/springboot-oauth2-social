package com.example.demo;

import java.util.Collections;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.bind.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class OauthApplication extends WebSecurityConfigurerAdapter {
  
//  @Autowired
//  AuthenticationFailureHandler handler;

  public static void main(String[] args) {
    SpringApplication.run(OauthApplication.class, args);
  }

  @GetMapping("/user")
  public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
    return Collections.singletonMap("name", principal.getAttribute("name"));
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    // @formatter:off
      http
          .authorizeRequests(a -> a
              .antMatchers("/", "/error", "/webjars/**").permitAll()
              .anyRequest().authenticated()
          )
          .csrf(c -> c
              .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
          )
          .exceptionHandling(e -> e
              .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
          )
          .oauth2Login(o -> o
            .failureHandler((request, response, exception) -> {
                request.getSession().setAttribute("error.message", exception.getMessage());
                //handler.onAuthenticationFailure(request, response, exception);
            }));
      // @formatter:on
  }

  @GetMapping("/error")
  public String error(HttpServletRequest request) {
    String message = (String) request.getSession().getAttribute("error.message");
    request.getSession().removeAttribute("error.message");
    return message;
  }

}
