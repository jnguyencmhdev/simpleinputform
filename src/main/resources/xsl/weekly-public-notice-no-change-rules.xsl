<?xml version="1.0" encoding="UTF-8"?>
<!--
  XSL-FO template for the Weekly Public Notice of No Change Rules Report.

  PDF/UA-1 compliance fix (rule 7.18.5:2):
    Every fo:basic-link must carry fox:alt-text so Apache FOP sets the
    /Contents entry on the resulting PDF link annotation, satisfying
    ISO 32000-1:2008 §14.9.3.
-->
<xsl:stylesheet version="1.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    xmlns:fox="http://xmlgraphics.apache.org/fop/extensions">

  <!-- ═══════════════════════════════ ROOT ═══════════════════════════════ -->
  <xsl:template match="/">
    <fo:root xml:lang="en-US">

      <!-- ── Layout master ── -->
      <fo:layout-master-set>
        <fo:simple-page-master master-name="main"
            page-height="11in" page-width="8.5in"
            margin-top="0.75in" margin-bottom="0.75in"
            margin-left="1in"  margin-right="1in">
          <fo:region-body margin-top="0.5in" margin-bottom="0.5in"/>
          <fo:region-before extent="0.5in"/>
          <fo:region-after  extent="0.5in"/>
        </fo:simple-page-master>
      </fo:layout-master-set>

      <!-- Document metadata (FOP uses fo:declarations for XMP metadata in
           FOP 2.9; title is set programmatically via FopFactory user-agent) -->

      <!-- ── Page sequence ── -->
      <fo:page-sequence master-reference="main">

        <!-- Header -->
        <fo:static-content flow-name="xsl-region-before">
          <fo:block font-size="8pt" text-align="center" color="#555555">
            <xsl:value-of select="report/title"/>
          </fo:block>
        </fo:static-content>

        <!-- Footer -->
        <fo:static-content flow-name="xsl-region-after">
          <fo:block font-size="8pt" text-align="center" color="#555555">
            Page <fo:page-number/> of <fo:page-number-citation ref-id="last-page"/>
          </fo:block>
        </fo:static-content>

        <!-- Body -->
        <fo:flow flow-name="xsl-region-body">

          <!-- Report title -->
          <fo:block font-size="16pt" font-weight="bold"
              text-align="center" space-after="12pt"
              role="H1">
            <xsl:value-of select="report/title"/>
          </fo:block>

          <!-- Report date -->
          <fo:block font-size="10pt" text-align="center" space-after="20pt">
            Week of <xsl:value-of select="report/weekDate"/>
          </fo:block>

          <!-- Introduction paragraph -->
          <fo:block font-size="10pt" space-after="16pt">
            The following rules have been reviewed and determined to require no changes
            for the filing period indicated above.
          </fo:block>

          <!-- Rules table -->
          <xsl:apply-templates select="report/rules"/>

          <!-- Navigation / reference links
               ────────────────────────────────────────────────────────────
               PDF/UA-1 rule 7.18.5:2 FIX:
               Every fo:basic-link MUST have fox:alt-text so that Apache FOP
               writes the /Contents entry into the PDF link annotation.
               Without fox:alt-text the /Contents key is absent and veraPDF /
               PDFComplianceManager raises:
                 "7.18.5:2 Links shall contain an alternate description via
                  their Contents key (ISO 32000-1:2008, 14.9.3)"
               ──────────────────────────────────────────────────────────── -->
          <fo:block space-before="24pt" font-size="9pt" color="#1a0dab"
              border-top="0.5pt solid #cccccc" padding-top="8pt">
            <fo:inline>Related resources: </fo:inline>

            <fo:basic-link
                external-destination="url('https://www.federalregister.gov')"
                fox:alt-text="Federal Register website">
              Federal Register
            </fo:basic-link>

            <fo:inline>&#160;|&#160;</fo:inline>

            <fo:basic-link
                external-destination="url('https://www.regulations.gov')"
                fox:alt-text="Regulations.gov website">
              Regulations.gov
            </fo:basic-link>
          </fo:block>

          <!-- End-of-document anchor for page-number-citation -->
          <fo:block id="last-page"/>

        </fo:flow>
      </fo:page-sequence>
    </fo:root>
  </xsl:template>

  <!-- ═══════════════════════════════ RULES ══════════════════════════════ -->
  <xsl:template match="rules">
    <fo:table table-layout="fixed" width="100%"
        border-collapse="separate" space-after="16pt"
        role="Table">
      <fo:table-column column-width="20%"/>
      <fo:table-column column-width="50%"/>
      <fo:table-column column-width="30%"/>

      <!-- Table header -->
      <fo:table-header>
        <fo:table-row background-color="#e8e8e8" role="TR">
          <fo:table-cell padding="4pt" border="0.5pt solid #aaaaaa" role="TH">
            <fo:block font-weight="bold" font-size="9pt">Rule ID</fo:block>
          </fo:table-cell>
          <fo:table-cell padding="4pt" border="0.5pt solid #aaaaaa" role="TH">
            <fo:block font-weight="bold" font-size="9pt">Description</fo:block>
          </fo:table-cell>
          <fo:table-cell padding="4pt" border="0.5pt solid #aaaaaa" role="TH">
            <fo:block font-weight="bold" font-size="9pt">Reference</fo:block>
          </fo:table-cell>
        </fo:table-row>
      </fo:table-header>

      <!-- Table body -->
      <fo:table-body>
        <xsl:choose>
          <xsl:when test="rule">
            <xsl:apply-templates select="rule"/>
          </xsl:when>
          <xsl:otherwise>
            <fo:table-row role="TR">
              <fo:table-cell padding="4pt" border="0.5pt solid #aaaaaa"
                  number-columns-spanned="3" role="TD">
                <fo:block font-size="9pt" font-style="italic">
                  No rules to display for this period.
                </fo:block>
              </fo:table-cell>
            </fo:table-row>
          </xsl:otherwise>
        </xsl:choose>
      </fo:table-body>
    </fo:table>
  </xsl:template>

  <!-- ══════════════════════════════ RULE ROW ════════════════════════════ -->
  <xsl:template match="rule">
    <fo:table-row role="TR">

      <!-- Rule ID cell -->
      <fo:table-cell padding="4pt" border="0.5pt solid #aaaaaa" role="TD">
        <fo:block font-size="9pt">
          <xsl:value-of select="id"/>
        </fo:block>
      </fo:table-cell>

      <!-- Description cell -->
      <fo:table-cell padding="4pt" border="0.5pt solid #aaaaaa" role="TD">
        <fo:block font-size="9pt">
          <xsl:value-of select="description"/>
        </fo:block>
      </fo:table-cell>

      <!-- Reference link cell
           PDF/UA-1 rule 7.18.5:2 FIX:
           fox:alt-text provides the alternate description for the link
           annotation's /Contents entry. -->
      <fo:table-cell padding="4pt" border="0.5pt solid #aaaaaa" role="TD">
        <fo:block font-size="9pt">
          <xsl:choose>
            <xsl:when test="referenceUrl and referenceUrl != ''">
              <fo:basic-link
                  external-destination="url('{referenceUrl}')"
                  color="#1a0dab"
                  fox:alt-text="{concat('Reference for rule ', id)}">
                <xsl:value-of select="referenceLabel"/>
              </fo:basic-link>
            </xsl:when>
            <xsl:otherwise>
              <xsl:value-of select="referenceLabel"/>
            </xsl:otherwise>
          </xsl:choose>
        </fo:block>
      </fo:table-cell>

    </fo:table-row>
  </xsl:template>

</xsl:stylesheet>
