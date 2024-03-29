<!--
  ~ Copyright 2010 the original author or authors.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~      http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  -->
<xsl:stylesheet
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:xi="http://www.w3.org/2001/XInclude"
        version="1.0">
    <xsl:import href="urn:docbkx:stylesheet/docbook.xsl"/>
    <xsl:import href="htmlCommon.xsl"/>

    <xsl:param name="section.autolabel">0</xsl:param>
    <xsl:param name="chapter.autolabel">0</xsl:param>
    <xsl:param name="appendix.autolabel">0</xsl:param>

    <!-- Inline the stylesheets directly into the generated html -->

    <xsl:template name="output.html.stylesheets">
    </xsl:template>

    <xsl:template name="user.head.content">
        <style type="text/css">
            <xi:include href="css/base.css" parse="text"/>
            <xi:include href="css/style.css" parse="text"/>
            <xi:include href="css/guide.css" parse="text"/>
        </style>
    </xsl:template>
</xsl:stylesheet>