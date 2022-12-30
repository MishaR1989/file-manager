package com.ramankevich.fileimportmanager.exceptions;

public class CustomException extends RuntimeException {

    private final int errorCode;
    private final String errorMessage;

    public CustomException(int errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
