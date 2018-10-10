package com.sample.watermark.exception;

import org.xhtmlrenderer.util.XRRuntimeException;

public class CustomXRRuntimeException extends XRRuntimeException {

    public CustomXRRuntimeException(String msg) {
        super(msg);
    }

    public CustomXRRuntimeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
