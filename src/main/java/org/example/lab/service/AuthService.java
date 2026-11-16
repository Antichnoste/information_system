package org.example.lab.service;
import org.example.lab.exception.GlobalExceptionHandler;
import jakarta.ws.rs.WebApplicationException;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.example.lab.dto.AuthRequest;
import org.example.lab.model.AppUser;
import org.example.lab.repository.AppUserRepository;
import org.example.lab.security.Passwords;

@ApplicationScoped
public class AuthService {
    @Inject
    private AppUserRepository appUserRepository;

    @Transactional
    public String register(AuthRequest input) {
        if (appUserRepository.findByUsername(input.getUsername()) != null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(409, "Этот логин уже занят"));
        }

        AppUser user = new AppUser();
        user.setUsername(input.getUsername());
        user.setPasswordHash(Passwords.hash(input.getPassword()));
        appUserRepository.save(user);
        return user.getUsername();
    }

    public String authenticate(AuthRequest input) {
        if (input == null || input.getUsername() == null || input.getPassword() == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Укажите логин и пароль"));
        }

        AppUser user = appUserRepository.findByUsername(input.getUsername());
        if (user == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(401, "Неверный логин или пароль"));
        }

        if (!Passwords.matches(input.getPassword(), user.getPasswordHash())) {
            throw new WebApplicationException(GlobalExceptionHandler.response(401, "Неверный логин или пароль"));
        }

        return user.getUsername();
    }
}
