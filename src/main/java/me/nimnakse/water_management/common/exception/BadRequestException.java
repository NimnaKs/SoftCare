package me.nimnakse.water_management.common.exception;

public class BadRequestException extends RuntimeException {
    private final ErrorCode errorCode;

    public BadRequestException(String message) {
        super(message);
        this.errorCode = ErrorCode.VALIDATION_ERROR;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
