<xsl:stylesheet  exclude-result-prefixes="gml xsi"  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:gml="http://www.opengis.net/gml/3.2" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    
    <xsl:output method="html"/>
    
    <xsl:template match="/">
        <xsl:text disable-output-escaping='yes'>&lt;!DOCTYPE html>&#xa;</xsl:text>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="*"/>
    
    <xsl:template match="text() | @*"/>
    
    <xsl:template match="/gml:FeatureCollection">
        <html>
            <head>
                <style>
                    body {font-family: sans-serif;}
                    th {text-decoration:underline;}
                    .gmlId {font-weight:bold; background-color: burlyWood;}
                    .even {background-color: beige;}
                    .odd  {background-color: lightCyan;}
                    .meta {color: gray; font-style:italic;}
                </style>
            </head>
            <body>
                <xsl:apply-templates select="gml:featureMember"/>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template name="display-index">
        <xsl:param name="currentNode" select="."/>
        <xsl:param name="currentCount" select="number(1)"/>
        
        <xsl:choose>
            <xsl:when test="name($currentNode/preceding-sibling::*[1]) = name()">
                <xsl:call-template name="display-index">
                    <xsl:with-param name="currentNode" select="$currentNode/preceding-sibling::*[1]"/>
                    <xsl:with-param name="currentCount" select="$currentCount + 1"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$currentCount > 1 or name($currentNode/following-sibling::*[1]) = name()">
                    <xsl:text>[</xsl:text>
                    <xsl:value-of select="$currentCount"/>
                    <xsl:text>]</xsl:text>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template name="display-name">        
        <xsl:element name="span">
            <xsl:if test="namespace-uri()">
                <xsl:attribute name="title">
                    <xsl:text>{</xsl:text>
                    <xsl:value-of select="namespace-uri()"/>
                    <xsl:text>}</xsl:text>
                    <xsl:value-of select="local-name()"/>
                </xsl:attribute>
            </xsl:if>
            <xsl:value-of select="name()"/>
            <xsl:call-template name="display-index"/>
        </xsl:element>
    </xsl:template>

    <xsl:template match="gml:featureMember">
        <table>
            <tr>
                <th>Field</th>
                <th>Value</th>
            </tr>
            <tr class="gmlId">
                <td>GML_ID</td>
                <td><xsl:value-of select="*/@gml:id"/></td>
            </tr>
            <xsl:for-each select="*/descendant::*[not(self::gml:*)][count(child::*) = 0]">
                <xsl:variable name="content" select="text() | attribute::*"/>
                <xsl:if test="$content">
                    <xsl:variable name="isEven" select="position() mod 2 = 0"/>
                    <xsl:element name="tr">
                        <xsl:attribute name="class">
                            <xsl:choose>
                                <xsl:when test="$isEven">
                                    <xsl:text>even</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:text>odd</xsl:text>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:attribute>                        
                        <xsl:element name="td">
                            <xsl:variable name="count" select="count($content)"/>
                            <xsl:if test="$count > 1">
                                <xsl:attribute name="rowspan">
                                    <xsl:value-of select="$count"/>
                                </xsl:attribute>
                            </xsl:if>
                            <xsl:apply-templates mode="label" select="."/>
                        </xsl:element>
                        <td><xsl:apply-templates mode="content" select="$content[1]"/></td>                    
                    </xsl:element>
                    <xsl:for-each select="$content">
                        <xsl:if test="position() > 1">
                            <xsl:element name="tr">
                                <xsl:attribute name="class">
                                    <xsl:choose>
                                        <xsl:when test="$isEven">
                                            <xsl:text>even</xsl:text>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:text>odd</xsl:text>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:attribute>
                                <td>
                                    <xsl:apply-templates mode="content" select="."/>
                                </td>
                            </xsl:element>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:if>
            </xsl:for-each>            
        </table>        
    </xsl:template>
    
    <xsl:template mode="content" match="text() | @*"/>
    
    <xsl:template mode="content" match="text()">
        <xsl:apply-templates mode="attributeLabel"/>
         <xsl:value-of select="."/>
    </xsl:template>
    
    <xsl:template mode="content" match="@*">
        <xsl:apply-templates mode="attributeLabel" select="."/>
        <xsl:value-of select="."/>
    </xsl:template>
    
    <xsl:template mode="content" match="@xsi:nil">
        <span class="meta">(empty)</span>
    </xsl:template>
    
    <xsl:template mode="label" match="*">
        <xsl:for-each select="ancestor::*[parent::*[ancestor::gml:featureMember]]">
            <xsl:call-template name="display-name"/>
            <xsl:text>/</xsl:text>
        </xsl:for-each>
        <xsl:call-template name="display-name"/> 
    </xsl:template>
    
    <xsl:template mode="attributeLabel" match="@*">        
            <xsl:text>@</xsl:text>
            <xsl:call-template name="display-name"/>
            <xsl:text>: </xsl:text>
    </xsl:template>
    
    <xsl:template mode="attributeLabel" match="@codeListValue">
        <xsl:text>waarde: </xsl:text>
    </xsl:template>
    
    <xsl:template mode="attributeLabel" match="@codeList">
        <xsl:text>code lijst: </xsl:text>
    </xsl:template>
</xsl:stylesheet>