package com.jc.jnotes.dao;

public class DaoRuntimeException extends RuntimeException {
    
    public static final int LOCAL = 1;
    public static final int REMOTE = 2;

    private static final long serialVersionUID = 1L;
    private final int type;
    
    public DaoRuntimeException(int type, String errorMessage) {
        super(errorMessage);
        this.type = type;
    }
    
    public DaoRuntimeException(int type, Throwable err) {
        super(err);
        this.type = type;
    }

    public DaoRuntimeException(int type, String errorMessage, Throwable err) {
        super(errorMessage, err);
        this.type = type;
    }

    public int getType() {
        return type;
    }

}
