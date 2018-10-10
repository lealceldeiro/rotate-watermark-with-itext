package com.sample.watermark.config;

import com.lowagie.text.DocumentException;
import com.sample.watermark.exception.CustomDocumentException;
import com.sample.watermark.exception.CustomXRRuntimeException;
import com.sample.watermark.service.AppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice("com.sample.watermark.controller")
public class SimpleControllerAdvise extends ResponseEntityExceptionHandler {

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final String MESSAGE_HOLDER = "MESSAGE";
    private static final String CAUSE_HOLDER = "CAUSE";
    private static final String STATUS_HOLDER = "STATUS";
    private static final String TIME_HOLDER = "TIME";

    private AppService service;

    public SimpleControllerAdvise(AppService service) {
        this.service = service;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handlexception(Exception ex, WebRequest req) {
        return handleAsJSONOrPDF(ex, req);
    }

    private ResponseEntity<Object> handleAsJSONOrPDF(Exception ex, WebRequest req) {
        logger.error(ex.getMessage(), ex);
        if (req.getHeader(HttpHeaders.ACCEPT).equals(MediaType.APPLICATION_PDF_VALUE)) {
            try {
                return handleExceptionAsPDF(ex);
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }
        return handleExceptionAsJSON(ex, req);
    }

    /**
     * Generates a JSON with the error information. This is the default handler.
     * @param ex Exception thrown.
     * @param req {@link WebRequest}
     * @return A {@link ResponseEntity}
     */
    private ResponseEntity<Object> handleExceptionAsJSON(Exception ex, WebRequest req) {
        Map<String, Object> messages = getExceptionBody(ex);
        return handleExceptionInternal(ex, messages, new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, req);
    }

    /**
     * Generates and return a PDF file with the error information in case the client specified the "Accept" header as
     * "application/pdf".
     * @param ex Exception thrown.
     * @return A {@link ResponseEntity}
     * @throws DocumentException in case the html for rendering the pdf file is erroneous.
     */
    @SuppressWarnings("squid:S1192")
    private ResponseEntity<Object> handleExceptionAsPDF(Exception ex) throws DocumentException, IOException {
        Map<String, Object> messages = getExceptionBody(ex);
        StringBuilder htmlStart = new StringBuilder("" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <title>Error</title>\n" +
                "</head>\n" +
                "<body>" +
                "");
        String htmlEnd = "" +
                "</body>\n" +
                "</html>" +
                "";
        for(Map.Entry<String, Object> entry: messages.entrySet()) {
            htmlStart.append("<div>")
                    .append("<b>").append(entry.getKey()).append(": </b>")
                    .append("<span><code>").append(entry.getValue()).append("</code></span>")
                    .append("</div>")
                    .append("<hr />");
        }
        final StackTraceElement[] stackTrace = ex.getStackTrace();
        if (stackTrace.length > 0) {
            htmlStart.append("<br /><br /><div>STACKTRACE (filtered with only own classes):</div><hr /> ");
            StackTraceElement ste;
            String className;
            int i = 0;
            for (StackTraceElement aStackTrace : stackTrace) {
                ste = aStackTrace;
                className = ste.getClassName();
                if (className.startsWith("com.sample.watermark")) {
                    htmlStart.append("<i><br />(ST ").append(String.valueOf(++i)).append(")</i><br />");
                    htmlStart.append("<b>FILE: </b><code>").append(ste.getFileName()).append("</code><br />");
                    htmlStart.append("<b>CLASS:  </b><code>").append(className).append("</code><br />");
                    htmlStart.append("<b>LINE NUMBER:  </b><code>").append(ste.getLineNumber()).append("</code><br />");
                    htmlStart.append("<b>METHOD:  </b><code>").append(ste.getMethodName()).append("</code><br />");
                }
            }
        }

        htmlStart.append(htmlEnd);
        byte[] response = service.generatePDFFromHtml(htmlStart.toString());
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", "attachment; filename=error-info.pdf");
        return new ResponseEntity<>(response, headers, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Returns the body of the request when there is an exception.
     * @param e Exception thrown.
     * @return A {@link Map} containing detailed information of the occurred exception.
     */
    private Map<String, Object> getExceptionBody(Exception e) {
        Map<String, Object> body = new HashMap<>();
        body.put(MESSAGE_HOLDER, e.getMessage());
        if (e.getCause() != null) {
            body.put(CAUSE_HOLDER, e.getCause().getMessage());
        }
        body.put(STATUS_HOLDER, HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put(TIME_HOLDER, getTimestamp());

        return body;
    }

    /**
     * Returns the current timestamp in string format.
     * @return A String representation of the current timestamp.
     */
    private String getTimestamp(){
        return timeFormat.format(new Timestamp(System.currentTimeMillis()));
    }
}
