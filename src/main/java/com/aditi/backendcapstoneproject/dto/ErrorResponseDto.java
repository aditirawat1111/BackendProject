package com.aditi.backendcapstoneproject.dto;

public class ErrorResponseDto {
    private String status;
    private String message;

    public String getStatus() {
        return this.status;
    }

    public String getMessage() {
        return this.message;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
