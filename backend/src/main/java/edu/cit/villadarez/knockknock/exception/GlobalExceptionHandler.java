package edu.cit.villadarez.knockknock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
		return ResponseEntity
				.status(HttpStatus.BAD_REQUEST)
				.body(Map.of("error", ex.getMessage()));
	}

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<Map<String, String>> handleResponseStatusException(ResponseStatusException ex) {
		String message = ex.getReason() != null ? ex.getReason() : "Request failed";
		return ResponseEntity
				.status(ex.getStatusCode())
				.body(Map.of("error", message));
	}

	@ExceptionHandler(OAuthProviderConflictException.class)
	public ResponseEntity<Map<String, Object>> handleOAuthProviderConflictException(OAuthProviderConflictException ex) {
		return ResponseEntity
				.status(HttpStatus.CONFLICT)
				.body(Map.of(
						"success", false,
						"error", Map.of(
								"code", "AUTH-004",
								"message", ex.getMessage()
						)
				));
	}
}

