package com.seeyou.dieselpoint.norm;

/**
 * @author Septian Yudhistira
 * @version 1.0
 * @since 2024-11-03
 */

public class DBException extends RuntimeException {
    public DBException() {
    }

    public DBException(String msg) {
        super(msg);
    }

    public DBException(Throwable t) {
        super(t);
    }

    public DBException(String msg, Throwable t) {
        super(msg, t);
    }
}

