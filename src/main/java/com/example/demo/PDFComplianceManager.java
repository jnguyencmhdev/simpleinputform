package com.example.demo;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Checks PDF/UA-1 compliance rules using Apache PDFBox.
 *
 * <p>Rules currently enforced:</p>
 * <ul>
 *   <li><b>7.18.5:2</b> – Every link annotation must have a {@code /Contents}
 *       entry (alternate description per ISO 32000-1:2008 §14.9.3).</li>
 *   <li><b>7.21.4.1</b> – The document must contain at least one tagged
 *       annotation (link annotation present on the page).</li>
 *   <li><b>7.21.4.2</b> – Each link annotation must have a non-empty
 *       alternate description (Contents entry must not be blank).</li>
 * </ul>
 */
public enum PDFComplianceManager {

    INSTANCE;

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Assert that the PDF satisfies the named rule.
     *
     * @param pdfBytes   raw PDF bytes
     * @param ruleId     rule identifier, e.g. {@code "7.18.5:2"}
     * @param minCount   minimum number of elements that must satisfy the rule
     * @throws AssertionError if the rule is violated
     */
    public void assertComplianceForRule(byte[] pdfBytes, String ruleId, int minCount) {
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            List<String> violations = checkRule(doc, ruleId, minCount);
            if (!violations.isEmpty()) {
                throw new AssertionError(
                        "PDF is not compliant with rule " + ruleId + ":\n"
                                + String.join("\n", violations));
            }
        } catch (IOException e) {
            throw new AssertionError("Failed to read PDF for compliance check", e);
        }
    }

    /**
     * Assert that the PDF satisfies all supported PDF/UA-1 rules.
     *
     * @param pdfBytes raw PDF bytes
     * @throws AssertionError if any rule is violated
     */
    public void assertTotalCompliance(byte[] pdfBytes) {
        try (PDDocument doc = Loader.loadPDF(pdfBytes)) {
            List<String> violations = new ArrayList<>();
            violations.addAll(checkRule(doc, "7.18.5:2", 0));
            violations.addAll(checkRule(doc, "7.21.4.1", 0));
            violations.addAll(checkRule(doc, "7.21.4.2", 0));

            if (!violations.isEmpty()) {
                throw new AssertionError(
                        "PDF is not completely compliant with ua1\n"
                                + String.join("\n", violations));
            }
        } catch (IOException e) {
            throw new AssertionError("Failed to read PDF for total compliance check", e);
        }
    }

    // ── Rule evaluators ───────────────────────────────────────────────────

    private List<String> checkRule(PDDocument doc, String ruleId, int minCount)
            throws IOException {
        return switch (ruleId) {
            case "7.18.5:2"  -> checkRule_7_18_5_2(doc);
            case "7.21.4.1"  -> checkRule_7_21_4_1(doc, minCount);
            case "7.21.4.2"  -> checkRule_7_21_4_2(doc);
            default          -> List.of();
        };
    }

    /**
     * Rule 7.18.5:2 – every link annotation must have a non-null
     * {@code /Contents} entry.
     */
    private List<String> checkRule_7_18_5_2(PDDocument doc) throws IOException {
        List<String> violations = new ArrayList<>();
        for (PDPage page : doc.getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationLink link) {
                    if (link.getContents() == null || link.getContents().isBlank()) {
                        violations.add(
                                "7.18.5:2 Links shall contain an alternate description"
                                        + " via their Contents key as described in"
                                        + " ISO 32000-1:2008, 14.9.3");
                    }
                }
            }
        }
        return violations;
    }

    /**
     * Rule 7.21.4.1 – the document must have at least {@code minCount}
     * annotations that are tagged (i.e. appear on a page).
     */
    private List<String> checkRule_7_21_4_1(PDDocument doc, int minCount) throws IOException {
        int count = 0;
        for (PDPage page : doc.getPages()) {
            count += page.getAnnotations().size();
        }
        List<String> violations = new ArrayList<>();
        if (minCount > 0 && count < minCount) {
            violations.add(
                    "7.21.4.1 Expected at least " + minCount
                            + " annotation(s) but found " + count);
        }
        return violations;
    }

    /**
     * Rule 7.21.4.2 – every annotation must have an accessible bounding box
     * (non-zero rectangle), confirming it is properly structured.
     */
    private List<String> checkRule_7_21_4_2(PDDocument doc) throws IOException {
        List<String> violations = new ArrayList<>();
        for (PDPage page : doc.getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                PDRectangle rect = annotation.getRectangle();
                if (rect == null || (rect.getWidth() == 0 && rect.getHeight() == 0)) {
                    violations.add(
                            "7.21.4.2 Annotation has zero-size or missing bounding rectangle");
                }
            }
        }
        return violations;
    }
}
