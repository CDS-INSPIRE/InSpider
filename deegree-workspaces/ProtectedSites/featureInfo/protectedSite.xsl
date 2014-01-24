<xsl:stylesheet  exclude-result-prefixes="gml base ps gmd gco" xmlns:gn="urn:x-inspire:specification:gmlas:GeographicalNames:3.0" xmlns:gco="http://www.isotc211.org/2005/gco" xmlns:gmd="http://www.isotc211.org/2005/gmd" xmlns:base="urn:x-inspire:specification:gmlas:BaseTypes:3.2" xmlns:ps="urn:x-inspire:specification:gmlas:ProtectedSites:3.0" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:import href="html.xsl"/>

    <xsl:template mode="label" match="ps:siteProtectionClassification">
        <xsl:text>Site protection classification</xsl:text>
        <xsl:call-template name="display-index"/>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:inspireID/base:Identifier/base:localId">
        <xsl:text>Inspire id local id</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:inspireID/base:Identifier/base:namespace">
        <xsl:text>Inspire id namespace</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:legalFoundationDate">
        <xsl:text>Legal foundation date</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:legalFoundationDocument/gmd:CI_Citation/gmd:title/gco:CharacterString">
        <xsl:text>Legal foundation document title</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:legalFoundationDocument/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:date/gco:DateTime">
        <xsl:text>Legal foundation document date</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:legalFoundationDocument/gmd:CI_Citation/gmd:date/gmd:CI_Date/gmd:dateType/gmd:CI_DateTypeCode">
        <xsl:text>Legal foundation document date type</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteDesignation">
        <xsl:text>Site designation</xsl:text>
        <xsl:call-template name="display-index"/>        
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteDesignation/ps:DesignationType/ps:designationScheme">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteDesignation"/>
        <xsl:text> schema</xsl:text>        
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteDesignation/ps:DesignationType/ps:designation">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteDesignation"/>                
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteDesignation/ps:DesignationType/ps:percentageUnderDesignation	">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteDesignation"/>
        <xsl:text> percentage under designation</xsl:text>        
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName">
        <xsl:text>Site name</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName/gn:GeographicalName/gn:language">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteName"/>
        <xsl:text> language</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName/gn:GeographicalName/gn:nativeness">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteName"/>
        <xsl:text> nativeness</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName/gn:GeographicalName/gn:nameStatus">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteName"/>
        <xsl:text> status</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName/gn:GeographicalName/gn:pronunciation">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteName"/>
        <xsl:text> pronunciation</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName/gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:script">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteName"/>
        <xsl:text> script</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName/gn:GeographicalName/gn:sourceOfName">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteName"/>
        <xsl:text> source</xsl:text>
    </xsl:template>
    
    <xsl:template mode="label" match="ps:siteName/gn:GeographicalName/gn:spelling/gn:SpellingOfName/gn:text">
        <xsl:apply-templates mode="label" select="ancestor::ps:siteName"/>        
    </xsl:template>
    
</xsl:stylesheet>