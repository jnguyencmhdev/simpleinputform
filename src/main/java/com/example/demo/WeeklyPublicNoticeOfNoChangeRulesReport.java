package com.example.demo;

import org.apache.fop.apps.Fop;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FopFactory;
import org.apache.fop.apps.MimeConstants;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionURI;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotation;
import org.apache.pdfbox.pdmodel.interactive.annotation.PDAnnotationLink;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Renders the Weekly Public Notice of No Change Rules report as a PDF.
 *
 * <p>The XSL-FO template applies the PDF/UA-1 fix for rule 7.18.5:2 by
 * setting {@code fox:alt-text} on every {@code fo:basic-link}, which causes
 * Apache FOP to write the {@code /Contents} entry on each link annotation.</p>
 */
@Service
public class WeeklyPublicNoticeOfNoChangeRulesReport {

    private static final String XSL_PATH = "xsl/weekly-public-notice-no-change-rules.xsl";
    private static final DateTimeFormatter WEEK_FMT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /**
     * Render the report for the week that contains {@code reportDate}.
     *
     * @param reportDate the reference date for the report week
     * @return the rendered {@link PDDocument}; caller must close it
     */
    public PDDocument render(LocalDateTime reportDate) throws Exception {
        // Build the XML data document for this report
        Document dataDoc = buildDataDocument(reportDate);

        // Transform XSL-FO data document → PDF bytes via Apache FOP
        byte[] pdfBytes = renderToPdf(dataDoc);

        // Load the PDF bytes into PDDocument
        PDDocument document = Loader.loadPDF(pdfBytes);

        // PDF/UA-1 rule 7.18.5:2 post-processing fix:
        // Apache FOP 2.9 propagates fox:alt-text into the tagged structure tree but
        // does NOT write it into the link annotation's /Contents entry.  We therefore
        // set /Contents on every link annotation that still lacks one, using the
        // action URI as the accessible description.
        ensureLinkAnnotationContents(document);

        return document;
    }

    // ── private helpers ───────────────────────────────────────────────────

    /**
     * Ensure that every link annotation in {@code document} has a non-blank
     * {@code /Contents} entry (PDF/UA-1 rule 7.18.5:2).
     *
     * <p>Apache FOP 2.9 populates the tagged PDF structure tree from
     * {@code fox:alt-text} but does not copy it into the annotation dictionary's
     * {@code /Contents} key.  This method closes that gap by writing the link
     * action URI into {@code /Contents} for any annotation that still lacks one.</p>
     */
    private void ensureLinkAnnotationContents(PDDocument document) throws IOException {
        for (PDPage page : document.getPages()) {
            for (PDAnnotation annotation : page.getAnnotations()) {
                if (annotation instanceof PDAnnotationLink link
                        && (link.getContents() == null || link.getContents().isBlank())) {
                    String description = "Link";
                    if (link.getAction() instanceof PDActionURI uriAction) {
                        description = uriAction.getURI();
                    }
                    link.setContents(description);
                }
            }
        }
    }

    private Document buildDataDocument(LocalDateTime reportDate) throws Exception {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = db.newDocument();

        Element reportEl = doc.createElement("report");
        doc.appendChild(reportEl);

        // Title
        Element titleEl = doc.createElement("title");
        titleEl.setTextContent("Weekly Public Notice of No Change Rules");
        reportEl.appendChild(titleEl);

        // Week date
        Element weekEl = doc.createElement("weekDate");
        weekEl.setTextContent(reportDate.format(WEEK_FMT));
        reportEl.appendChild(weekEl);

        // Rules list
        Element rulesEl = doc.createElement("rules");
        reportEl.appendChild(rulesEl);

        addRule(doc, rulesEl,
                "7.21.4.1",
                "Annotation structure element tagging requirement",
                "https://www.iso.org/standard/64599.html",
                "ISO 14289-1 §7.21.4.1");

        addRule(doc, rulesEl,
                "7.21.4.2",
                "Annotation tag type identification requirement",
                "https://www.iso.org/standard/64599.html",
                "ISO 14289-1 §7.21.4.2");

        return doc;
    }

    private void addRule(Document doc, Element parent,
                         String id, String description,
                         String refUrl, String refLabel) {
        Element rule = doc.createElement("rule");

        Element idEl = doc.createElement("id");
        idEl.setTextContent(id);
        rule.appendChild(idEl);

        Element descEl = doc.createElement("description");
        descEl.setTextContent(description);
        rule.appendChild(descEl);

        Element urlEl = doc.createElement("referenceUrl");
        urlEl.setTextContent(refUrl);
        rule.appendChild(urlEl);

        Element labelEl = doc.createElement("referenceLabel");
        labelEl.setTextContent(refLabel);
        rule.appendChild(labelEl);

        parent.appendChild(rule);
    }

    private byte[] renderToPdf(Document dataDoc) throws Exception {
        // Load XSL from classpath
        ClassPathResource xslResource = new ClassPathResource(XSL_PATH);

        // Set up FOP factory
        FopFactory fopFactory = FopFactory.newInstance(
                getClass().getResource("/").toURI());

        // Create FOUserAgent BEFORE newFop so accessibility is applied at init time.
        // Calling setAccessibility(true) after newFop causes a NullPointerException
        // because the PDF logical structure handler is not wired up.
        FOUserAgent foUserAgent = fopFactory.newFOUserAgent();
        foUserAgent.setAccessibility(true);
        foUserAgent.setProducer("Weekly Public Notice Report Generator");
        foUserAgent.setCreator("Weekly Public Notice Report Generator");
        foUserAgent.setTitle("Weekly Public Notice of No Change Rules");

        ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
        Fop fop = fopFactory.newFop(MimeConstants.MIME_PDF, foUserAgent, pdfOut);

        TransformerFactory tf = TransformerFactory.newInstance();
        Source xslSource = new StreamSource(xslResource.getInputStream());
        Transformer transformer = tf.newTransformer(xslSource);

        Source xmlSource = new DOMSource(dataDoc);
        Result result = new SAXResult(fop.getDefaultHandler());
        transformer.transform(xmlSource, result);

        return pdfOut.toByteArray();
    }
}
