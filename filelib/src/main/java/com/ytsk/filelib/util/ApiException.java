package com.ytsk.filelib.util;

/**
 * Created by tan on 2017/2/20.
 */

public class ApiException extends RuntimeException {

    public int code;
    public String msg;

    public ApiException(Throwable cause, int code, String msg) {
        super(cause);
        this.code = code;
        this.msg = msg;
    }

    public ApiException(int code, String msg) {
        super(msg);
        this.code = code;
        this.msg = msg;
    }

    public ApiException(Throwable cause) {
        super(cause);
    }
}
