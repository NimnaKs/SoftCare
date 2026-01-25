package me.nimnakse.water_management.common.exception;

public class NotFoundException extends RuntimeException {
    private final String messageSn;
    private final ErrorCode errorCode;

    public NotFoundException(String messageEn, String messageSn, ErrorCode errorCode) {
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
