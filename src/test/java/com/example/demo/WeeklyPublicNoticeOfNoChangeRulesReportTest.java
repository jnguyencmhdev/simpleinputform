package com.example.demo;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WeeklyPublicNoticeOfNoChangeRulesReportTest {

    private final WeeklyPublicNoticeOfNoChangeRulesReport weeklyPublicNoticeOfNoChangeRulesReport =
            new WeeklyPublicNoticeOfNoChangeRulesReport();

    @Test
    public void testWeeklyRuleFilingSummaryReport() throws Exception {
        LocalDateTime fixedDate = LocalDateTime.of(2026, 3, 24, 10, 0);

        try (PDDocument result = weeklyPublicNoticeOfNoChangeRulesReport.render(fixedDate);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            assertNotNull(result);
            result.save(baos);
            byte[] pdfBytes = baos.toByteArray();
            assertTrue(pdfBytes.length > 0);
            PDFComplianceManager.INSTANCE.assertComplianceForRule(pdfBytes, "7.21.4.1", 1);
            PDFComplianceManager.INSTANCE.assertComplianceForRule(pdfBytes, "7.21.4.2", 2);
            PDFComplianceManager.INSTANCE.assertTotalCompliance(pdfBytes);
        }
    }
}
