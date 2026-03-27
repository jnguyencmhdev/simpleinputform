package com.example.demo;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfName;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.annot.PdfAnnotation;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfExportServiceTest {

    private final PdfExportService service = new PdfExportService();

    @Test
    void testUA1() throws Exception {
        List<EntryField> entries = new ArrayList<>();
        EntryField entry = new EntryField();
        entry.setName("Jane Doe");
        entry.setAge(30);
        entry.setTitle("Engineer");
        entry.setHometown("San Francisco");
        entries.add(entry);

        byte[] pdfBytes = service.exportEntriesToPdf(entries);

        List<String> violations = checkUA1LinkCompliance(pdfBytes);
        assertTrue(violations.isEmpty(),
                "PDF is not completely compliant with ua1\n" + String.join("\n", violations));
    }

    private List<String> checkUA1LinkCompliance(byte[] pdfBytes) throws Exception {
        List<String> violations = new ArrayList<>();

        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(new ByteArrayInputStream(pdfBytes)))) {
            for (int i = 1; i <= pdfDoc.getNumberOfPages(); i++) {
                PdfPage page = pdfDoc.getPage(i);
                for (PdfAnnotation annotation : page.getAnnotations()) {
                    if (PdfName.Link.equals(annotation.getSubtype())) {
                        if (annotation.getPdfObject().get(PdfName.Contents) == null) {
                            violations.add("7.18.5:2 Links shall contain an alternate description"
                                    + " via their Contents key as described in"
                                    + " ISO 32000-1:2008, 14.9.3");
                        }
                    }
                }
            }
        }

        return violations;
    }
}
