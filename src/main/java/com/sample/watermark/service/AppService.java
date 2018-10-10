package com.sample.watermark.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.sample.watermark.exception.CustomDocumentException;
import com.sample.watermark.exception.CustomXRRuntimeException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.htmlcleaner.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.util.XRRuntimeException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class AppService {


    public byte[] generatePDF() throws DocumentException, IOException {
        // sample html for getting a PDF
        String html = "" +
                "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <title>Sample PDF with watermark</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div>\n" +
                "        This is a sample PDF with a watermark in it\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>" +
                "";

        return generatePDFFromHtml(html);
    }

    /**
     * Generates a PDF from an HTML
     * @param html HTML to be converted to PDF
     * @return byte[] (byte array) containing the PDF information
     * @throws com.sample.watermark.exception.CustomDocumentException when there is an error due to malformed html
     * @see XRRuntimeException
     */
    public byte[] generatePDFFromHtml(String html) throws DocumentException, IOException {
        HtmlCleaner cleaner = new HtmlCleaner();
        TagNode rootTagNode = cleaner.clean(html);
        CleanerProperties cleanerProperties = cleaner.getProperties();
        cleanerProperties.setHtmlVersion(5);
        XmlSerializer xmlSerializer = new PrettyXmlSerializer(cleanerProperties);
        String cleanedHtml = xmlSerializer.getAsString(rootTagNode);

        ITextRenderer renderer = new ITextRenderer();
        try {
            renderer.setDocumentFromString(cleanedHtml);
        } catch (XRRuntimeException e) {
            throw new CustomXRRuntimeException(e.getMessage(), e);
        }
        renderer.layout();
        ByteArrayOutputStream fos = new ByteArrayOutputStream(html.length());
        try {
            renderer.createPDF(fos);
        } catch (DocumentException e) {
            throw new CustomDocumentException(e);
        }
        byte[] bytes = fos.toByteArray();
        return getDocumentWithWaterMark(bytes);
    }

    /**
     * Returns the same document with the watermark stamped on it. Read more at:
     * https://developers.itextpdf.com/examples/stamping-content-existing-pdfs/watermark-examples#1040-transparentwatermark2.java
     * and https://stackoverflow.com/a/29587148/5640649
     * @param documentBytes Byte array of the pdf which is going to be exported
     * @return byte[] with the same byte array provided but now with the watermark stamped on it.
     * @throws IOException If any IO exception occurs while adding the watermark
     * @throws DocumentException If any DocumentException exception occurs while adding the watermark
     */
    private byte[] getDocumentWithWaterMark(byte[] documentBytes) throws IOException, DocumentException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        // pdf
        PdfReader reader = new PdfReader(documentBytes);
        int n = reader.getNumberOfPages();
        PdfStamper stamper = new PdfStamper(reader, outputStream);
        // text watermark
        Font font = new Font(Font.HELVETICA, 60);
        Phrase phrase = new Phrase("SuperEasy You Done", font);
        // transparency
        PdfGState gs1 = new PdfGState();
        gs1.setFillOpacity(0.06f);
        // properties
        PdfContentByte over;
        Rectangle pagesize;
        float x, y;
        // loop over every page (in case more than one page)
        for (int i = 1; i <= n; i++) {
            pagesize = reader.getPageSizeWithRotation(i);
            x = (pagesize.getLeft() + pagesize.getRight()) / 2;
            y = (pagesize.getTop() + pagesize.getBottom()) / 2;
            over = stamper.getOverContent(i);
            over.saveState();
            over.setGState(gs1);
            // add text
            ColumnText.showTextAligned(over, Element.ALIGN_CENTER, phrase, x, y, 0);
            over.restoreState();
        }
        stamper.close();
        reader.close();

        return outputStream.toByteArray();
    }


}
