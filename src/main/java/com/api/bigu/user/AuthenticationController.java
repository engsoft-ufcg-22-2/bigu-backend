package com.api.bigu.user;

import com.api.bigu.config.JwtService;
import com.api.bigu.dto.auth.AuthenticationRequest;
import com.api.bigu.dto.auth.EmailRequest;
import com.api.bigu.dto.auth.NewPasswordRequest;
import com.api.bigu.dto.auth.RegisterRequest;
import com.api.bigu.exceptions.UserNotFoundException;
import com.api.bigu.exceptions.WrongPasswordException;
import com.api.bigu.services.AuthenticationService;
import com.api.bigu.util.errors.AuthenticationError;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    @Autowired
    JwtService jwtService;
	
	@Autowired
    AuthenticationService authenticationService;
	
	@Autowired
	UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> register(
            @RequestBody @Valid RegisterRequest registerRequest
    ) {
        try {
            return ResponseEntity.ok(authenticationService.register(registerRequest));
        } catch (IllegalArgumentException | TransactionSystemException e) {
            return AuthenticationError.userUnauthorized(e.getMessage());
        }
    }

    @PostMapping("/authenticate")
    public ResponseEntity<?> authenticate(
            @RequestBody AuthenticationRequest authenticationRequest
    ) {
        return ResponseEntity.ok(authenticationService.authenticate(authenticationRequest));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody EmailRequest emailRequest) throws MessagingException {
        try {
            return ResponseEntity.ok(authenticationService.recover(emailRequest.getEmail()));
        } catch (UserNotFoundException unfe) {
            return UserError.userNotFoundError();
        } catch (MessagingException e) {
            throw new MessagingException("Problemas ao enviar o email.");
        }
    }

    @PutMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestHeader("Authorization") String resetToken, @RequestBody NewPasswordRequest newPasswordRequest) {
        Integer userId = jwtService.extractUserId((jwtService.parse(resetToken)));
        String body = "";
        try {
            User user = userService.findUserById(userId);
            if (jwtService.isTokenValid(jwtService.parse(resetToken), user)) {
                authenticationService.updatePassword(userId, newPasswordRequest);
                body = "Senha alterada.";
            }
            return ResponseEntity.ok(body);
        } catch (UserNotFoundException e) {
            return UserError.userNotFoundError();
        } catch (WrongPasswordException e) {
            return AuthenticationError.wrongPassword();
        }
    }
    
    
    @PutMapping("/edit-password")
    public ResponseEntity<?> editPassword(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String actualPassword, @RequestBody NewPasswordRequest newPasswordRequest) {
        Integer userId = jwtService.extractUserId(jwtService.parse(authorizationHeader));
    	String body = "";
        try {
            User user = userService.findUserById(userId);
            if (jwtService.isTokenValid(jwtService.parse(authorizationHeader), user)){
                authenticationService.updatePassword(userId, actualPassword, newPasswordRequest);
                body = "Senha modificada com sucesso";
            }

    	} catch (UserNotFoundException unfe) {
            return UserError.userNotFoundError();
    	} catch (WrongPasswordException wPE) {
            return AuthenticationError.wrongPassword();
        }
        return ResponseEntity.ok(body);
    }
    

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String authorizationHeader) {
        try {
            authenticationService.addToBlackList(authorizationHeader);
            return ResponseEntity.ok("Logout realizado com sucesso");
        } catch (Exception e) {
            return AuthenticationError.failedLogout(e.getMessage());
        }
    }
}
