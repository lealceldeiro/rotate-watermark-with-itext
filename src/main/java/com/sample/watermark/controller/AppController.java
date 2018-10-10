package com.sample.watermark.controller;


import com.lowagie.text.DocumentException;
import com.sample.watermark.config.WaterMarkArea;
import com.sample.watermark.service.AppService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.net.HttpURLConnection;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_PDF_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.GET;

@Controller
@Api(tags = "PDF with watermark Demo",
        consumes = APPLICATION_JSON_VALUE,
        produces = APPLICATION_PDF_VALUE)
@WaterMarkArea
@AllArgsConstructor(onConstructor = @__(@Autowired))
@ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_OK, message = "Successful response"),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = "Internal server error")})
@RequestMapping("/v1")
public class AppController {

    private AppService appService;

    @RequestMapping(value = "/pdf", method = GET, produces = APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> getPDF() throws DocumentException, IOException {
        byte[] response = appService.generatePDF();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Disposition", "attachment; filename=PDF_FIle.pdf");
        return new ResponseEntity<>(response, headers, HttpStatus.OK);
    }

}
