package com.lms.backend.exception;

public class DomainExceptions {

    // 404 Not Found
    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) { super(message); }
    }

    // 409 Conflict (e.g., no copies left)
    public static class BusinessRuleException extends RuntimeException {
        public BusinessRuleException(String message) { super(message); }
    }

    // 403 Forbidden (e.g., member trying to return someone else's book)
    public static class AccessDeniedException extends RuntimeException {
        public AccessDeniedException(String message) { super(message); }
    }
}