package com.github.lemongrab32.client;

import com.github.lemongrab32.dto.LoginRequest;
import com.github.lemongrab32.exception.InvalidCredentialsException;
import com.github.lemongrab32.exception.UnauthorizedException;
import jakarta.ws.rs.NotAuthorizedException;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.AccessTokenResponse;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
@Slf4j
public class KeycloakAdminClient {

	private final Keycloak keycloak;
	private final String realm;
	private final String host;
	private final String clientId;
	private final String clientSecret;

	public KeycloakAdminClient(
		@Value("keycloak.host") String host,
		@Value("keycloak.realm") String realm,
		@Value("keycloak.admin.username") String adminUsername,
		@Value("keycloak.admin.username") String adminPassword,
		@Value("spring.security.oauth2.client.registration.gateway.client-id") String clientId,
		@Value("spring.security.oauth2.client.registration.gateway.client-secret") String clientSecret)
	{
		this.keycloak =
			Keycloak.getInstance(host, realm, adminUsername, adminPassword, clientId);
		this.realm = realm;
		this.host = host;
		this.clientId = clientId;
		this.clientSecret = clientSecret;
	}

	public void registerUser(String email, String password) {
		final String op = "[registerUser] - ";

		log.info("{}Registering user with email {}", op, email);

		UsersResource users = keycloak.realm(realm).users();

		UserRepresentation user = new UserRepresentation();
		CredentialRepresentation credential = createPasswordCredentials(password);
		user.setEmail(email);
		user.setUsername(email);
		user.setCredentials(Collections.singletonList(credential));

		try (Response response = users.create(user)) {
			if (response.getStatus() == 201) {
				log.info("{}User {} registered successfully", op, email);
			} else {
				log.error("{}User {} registration failed with status {}", op, email, response.getStatus());
			}
		} catch (Exception e) {
			log.error("{}User {} registration failed. Error message: {}", op, email, e.getMessage());
		}
	}

	public AccessTokenResponse login(LoginRequest request) {
		try (Keycloak keycloak = keycloakCredentialBuilder(request)) {
			return keycloak.tokenManager().getAccessToken();
		} catch (NotAuthorizedException e) {
			log.error("The user {} is not authorized", request.email());
			throw new UnauthorizedException("The user is not authorized");
		} catch (BadCredentialsException e) {
			log.error("Bad credentials for user {}", request.email());
			throw new InvalidCredentialsException("Bad credentials for user");
		}
	}

	private Keycloak keycloakCredentialBuilder(LoginRequest request) {
		return KeycloakBuilder.builder()
			.realm(realm)
			.serverUrl(host)
			.clientId(clientId)
			.clientSecret(clientSecret)
			.username(request.email())
			.password(request.password())
			.build();
	}

	private CredentialRepresentation createPasswordCredentials(String password) {
		CredentialRepresentation credential = new CredentialRepresentation();

		credential.setTemporary(false);
		credential.setType(CredentialRepresentation.PASSWORD);
		credential.setValue(password);

		return credential;
	}
}
