package com.sample.watermark.exception;

import com.lowagie.text.DocumentException;

public class CustomDocumentException extends DocumentException {

    public CustomDocumentException(Exception e) {
        super(e);
    }

    public CustomDocumentException() {
    }

    public CustomDocumentException(String s) {
        super(s);
    }
}
