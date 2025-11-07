package com.github.lemongrab32.dto;

public record RegistrationRequest(String email, String password, String confirmPassword) {
}
