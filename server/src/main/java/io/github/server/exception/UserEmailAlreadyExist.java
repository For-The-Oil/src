package io.github.server.exception;

public class UserEmailAlreadyExist extends RuntimeException {
    public UserEmailAlreadyExist(String message) {
        super(message);
    }
}
