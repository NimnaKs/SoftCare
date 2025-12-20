package me.nimnakse.watermanagement.common.exception;

public class NotFoundException extends RuntimeException {
    private final ErrorCode errorCode;

    public NotFoundException(String message, ErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
