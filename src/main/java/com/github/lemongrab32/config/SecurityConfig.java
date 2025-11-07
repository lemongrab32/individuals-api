package com.github.lemongrab32.config;

import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
		return http
			.csrf(ServerHttpSecurity.CsrfSpec::disable)
			.authorizeExchange(exchange -> exchange
				.pathMatchers( "/actuator/**")
				.permitAll()
				.anyExchange().authenticated())
			.oauth2ResourceServer(oauth -> oauth
				.jwt(Customizer.withDefaults()))
			.build();
	}

	@Bean
	public JwtDecoder jwtDecoder(OAuth2ResourceServerProperties resourceServerProperties) {
		return JwtDecoders.fromOidcIssuerLocation(resourceServerProperties.getJwt().getIssuerUri());
	}

}
