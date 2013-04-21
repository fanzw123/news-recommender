            <xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

             <xsl:output method="xml" encoding="utf-8" indent="yes"/>
            
             <xsl:template match="/">
<feedEntries>
  <xsl:for-each select="//outline[@xmlUrl and not(contains(@title,'Job'))]">
<feed>
   <url><xsl:value-of select="./@xmlUrl"/></url>
   <lang>ENGLISH</lang>
   <category>UNKNOWN</category>
  <site>nyt</site>
  <description><xsl:value-of select="./@title"/></description>
  
 
</feed>
</xsl:for-each>
               </feedEntries>
             </xsl:template>
            
            </xsl:stylesheet>
            