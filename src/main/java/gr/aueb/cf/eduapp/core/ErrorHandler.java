package gr.aueb.cf.eduapp.core;

import gr.aueb.cf.eduapp.core.exceptions.*;
import gr.aueb.cf.eduapp.dto.ResponseMessageDTO;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
@Slf4j
public class ErrorHandler  extends ResponseEntityExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException (ValidationException valExc) {
        log.error("Validation failed. Message={}", valExc.getMessage(), valExc);
        BindingResult bindingResult = valExc.getBindingResult();

        Map<String, String> errors = new HashMap<>();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AppObjectNotFoundException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException (AppObjectNotFoundException nfe) {
        log.warn("Entity not found. Message={}", nfe.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)        // HttpCode 404
                .body(new ResponseMessageDTO(nfe.getCode(), nfe.getMessage()));
    }

    @ExceptionHandler(AppObjectInvalidArgumentException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException (AppObjectInvalidArgumentException iae) {
        log.warn("Invalid argument. Message={}", iae.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)        // HttpCode 400
                .body(new ResponseMessageDTO(iae.getCode(), iae.getMessage()));
    }

    @ExceptionHandler(AppObjectAlreadyExists.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException (AppObjectAlreadyExists aee) {
        log.warn("Entity already exists. Message={}", aee.getMessage());
        return ResponseEntity
                .status(HttpStatus.CONFLICT)        // HttpCode 409
                .body(new ResponseMessageDTO(aee.getCode(), aee.getMessage()));
    }

    @ExceptionHandler(AppObjectNotAuthorizedException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException (AppObjectNotAuthorizedException nae, WebRequest request) {
        log.warn("Authorized failed for URI={}. Message={}", request.getDescription(false), nae.getMessage());      // uri=/api/user/...
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)        // HttpCode 403
                .body(new ResponseMessageDTO(nae.getCode(), nae.getMessage()));
    }

    @ExceptionHandler(AppServerException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException (AppServerException ase) {
        log.warn("Server error with message={}", ase.getMessage());      // uri=/api/user/...
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)        // HttpCode 500
                .body(new ResponseMessageDTO(ase.getCode(), ase.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseMessageDTO> handleConstraintViolationException (IOException ioe) {
        log.error("File upload failed with message={}", ioe.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)        // HttpCode 500
                .body(new ResponseMessageDTO("FILE_UPLOAD_FAILED ", ioe.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseMessageDTO> handleBadCredentialsException (BadCredentialsException bce, HttpServletRequest request) {
        log.warn("Login attempt failed for IP={}",request.getRemoteAddr());
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)        // HttpCode 401
                .body(new ResponseMessageDTO("UNAUTHORIZED ", bce.getMessage()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseMessageDTO> handleBadCredentialsException (AuthenticationException ae, HttpServletRequest request) {
        log.warn("Failed login for IP={}", request.getRemoteAddr());

        String errorCode = switch (ae.getClass().getSimpleName()) {
            case "DisabledException" -> "ACCOUNT_DISABLED";
            case "LockedException" -> "ACCOUNT_LOCKED";
            case "AccountExpiredException" -> "ACCOUNT_EXPIRED";
            case "CredentialsExpiredException" -> "CREDENTIALS_EXPIRED";
            default -> "ACCOUNT_ERROR";
        };

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)        // HttpCode 403
                .body(new ResponseMessageDTO(errorCode, ae.getMessage()));
    }



}