package com.jc.jnotes.service;

public class ControllerServiceException extends Exception {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public ControllerServiceException(String errorMessage) {
        super(errorMessage);
    }
    
    public ControllerServiceException(String errorMessage, Throwable err) {
        super(errorMessage, err);
    }
    
}
