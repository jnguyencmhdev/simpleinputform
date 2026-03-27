package com.example.demo;

import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfString;
import com.itextpdf.kernel.pdf.PdfVersion;
import com.itextpdf.kernel.pdf.PdfViewerPreferences;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.WriterProperties;
import com.itextpdf.kernel.pdf.action.PdfAction;
import com.itextpdf.kernel.pdf.annot.PdfLinkAnnotation;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@Service
public class PdfExportService {

    public byte[] exportEntriesToPdf(List<EntryField> entries) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        WriterProperties writerProperties = new WriterProperties()
                .addUAXmpMetadata()
                .setPdfVersion(PdfVersion.PDF_1_7);

        try (PdfWriter writer = new PdfWriter(baos, writerProperties);
             PdfDocument pdfDoc = new PdfDocument(writer)) {

            pdfDoc.setTagged();
            pdfDoc.getCatalog().setViewerPreferences(
                    new PdfViewerPreferences().setDisplayDocTitle(true));
            pdfDoc.getCatalog().setLang(new PdfString("en-US"));
            pdfDoc.getDocumentInfo().setTitle("Entries Export");

            Document document = new Document(pdfDoc);

            document.add(new Paragraph("Entries Export"));

            for (EntryField entry : entries) {
                document.add(new Paragraph(
                        entry.getName() + " | Age: " + entry.getAge()
                                + " | " + entry.getTitle() + " | " + entry.getHometown()));
            }

            // Add a navigation link back to the entries page
            addLinkAnnotation(pdfDoc.getPage(1),
                    new Rectangle(50, 50, 150, 20),
                    "/entries",
                    "View all entries");

            document.close();
        }

        return baos.toByteArray();
    }

    private void addLinkAnnotation(PdfPage page, Rectangle rect, String uri, String description) {
        PdfLinkAnnotation linkAnnotation = new PdfLinkAnnotation(rect);
        linkAnnotation.setAction(PdfAction.createURI(uri));
        // Required for PDF/UA-1 rule 7.18.5:2: Links shall contain an alternate
        // description via their Contents key (ISO 32000-1:2008, 14.9.3)
        linkAnnotation.setContents(description);
        page.addAnnotation(linkAnnotation);
    }
}
