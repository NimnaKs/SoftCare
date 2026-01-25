package me.nimnakse.water_management.common.exception;

public class BadRequestException extends RuntimeException {
    private final String messageSn;
    private final ErrorCode errorCode;

    public BadRequestException(String messageEn, String messageSn) {
        super(messageEn);
        this.messageSn = messageSn;
        this.errorCode = ErrorCode.VALIDATION_ERROR;
    }

    public BadRequestException(String messageEn, String messageSn, ErrorCode errorCode) {
        super(messageEn);
        this.messageSn = messageSn;
        this.errorCode = errorCode;
    }

    public String getMessageSn() {
        return messageSn;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
