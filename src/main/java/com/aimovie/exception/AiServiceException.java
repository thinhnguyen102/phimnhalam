package com.aimovie.exception;

public class AiServiceException extends RuntimeException {
    
    private final String errorCode;
    private final String serviceName;
    
    public AiServiceException(String message) {
        super(message);
        this.errorCode = "AI_SERVICE_ERROR";
        this.serviceName = "AI_ACTOR_RECOGNITION";
    }
    
    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "AI_SERVICE_ERROR";
        this.serviceName = "AI_ACTOR_RECOGNITION";
    }
    
    public AiServiceException(String errorCode, String serviceName, String message) {
        super(message);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
    }
    
    public AiServiceException(String errorCode, String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.serviceName = serviceName;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public String getServiceName() {
        return serviceName;
    }
}
