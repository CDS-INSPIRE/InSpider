<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0"
    xmlns:wfs="http://www.opengis.net/wfs" xmlns:gml="http://www.opengis.net/gml">

    <xsl:template match="/wfs:FeatureCollection">
        <xsl:copy>
            <xsl:apply-templates select="*/*">
                <xsl:sort select="child::node()/@gml:id"></xsl:sort>
            </xsl:apply-templates>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*[@gml:id]">
        <xsl:copy>
            <xsl:value-of select="@gml:id"></xsl:value-of>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template match="*"></xsl:template>
</xsl:stylesheet>
