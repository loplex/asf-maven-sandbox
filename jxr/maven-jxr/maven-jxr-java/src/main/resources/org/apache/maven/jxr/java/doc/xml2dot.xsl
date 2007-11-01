<?xml version="1.0" encoding="ISO-8859-1" ?>
<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
  <xsl:output method="text" omit-xml-declaration="yes"/>

  <xsl:param name="now"/>
  <!--
    show="public"    Show only public classes and members
    show="protected" Show protected/public classes and members (default)
    show="package"   Show package/protected/public classes and members
    show="private"   Show all classes and members
    show=""          Show nothing
  -->
  <xsl:param name="show"/>
  <!--
    javasrcPath      Relative path or URI to the generated Java Xref directory.
  -->
  <xsl:param name="javasrcPath"/>
  <!--
    javadocPath      Relative path or URI to the generated Javadoc directory.
  -->
  <xsl:param name="javadocPath"/>
  <!--
    diagramLabel     The label of the generated class diagram.
  -->
  <xsl:param name="diagramLabel"/>
  <!--
    diagramEncoding  The encoding of the generated class diagram.
  -->
  <xsl:param name="diagramEncoding"/>

  <xsl:template match="javadoc">
    <xsl:text>#
# Class Diagram - Generated by Maven xml2dot on </xsl:text><xsl:value-of select="$now"/><xsl:text>
#
# @see http://www.graphviz.org/doc/libguide/libguide.pdf
#

    digraph G {
        // global settings</xsl:text>
      <xsl:if test="$diagramEncoding != ''">
        <xsl:text>
        charset = "</xsl:text>
        <xsl:value-of select="$diagramEncoding"/>
        <xsl:text>";</xsl:text>
      </xsl:if>
      <xsl:text>
        ranksep = "1.5";
        rankdir = "TB";
        labeljust = "center";
        labelloc = "bottom";
        label = "</xsl:text>
        <xsl:choose>
          <xsl:when test="$diagramLabel != ''">
            <xsl:value-of select="$diagramLabel"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text>Class Diagram</xsl:text>
          </xsl:otherwise>
        </xsl:choose>
        <xsl:text>";

        edge [fontname = "Helvetica", fontsize = "9", labelfontname = "Helvetica", labelfontsize = "9"];
        node [fontname = "Helvetica", fontsize = "9", shape = "record"];&#10;</xsl:text>

    <xsl:text>
        // subgraphs&#10;</xsl:text>

    <xsl:apply-templates/>

    <xsl:text>
        // edges&#10;</xsl:text>

    <xsl:for-each select="package/class">
      <xsl:if test="extends_class and extends_class/classref/@name!='java.lang.Object'">
        <xsl:text>&#10;        // extends edges&#10;</xsl:text>
        <xsl:text>        </xsl:text>
        <xsl:call-template name="fullname">
          <xsl:with-param name="name" select="extends_class/classref/@name"/>
        </xsl:call-template>
        <xsl:text> -> </xsl:text>
        <xsl:call-template name="fullname">
          <xsl:with-param name="name" select="../@name"/>
          <xsl:with-param name="parentname" select="@name"/>
        </xsl:call-template>
        <xsl:text> [dir = "back", arrowtail = "empty"];&#10;</xsl:text>
      </xsl:if>
      <xsl:if test="implements">
        <xsl:text>&#10;        // implements edges&#10;</xsl:text>
        <xsl:text>        </xsl:text>
        <xsl:for-each select="implements/interfaceref">
          <xsl:call-template name="fullname">
            <xsl:with-param name="name" select="@name"/>
          </xsl:call-template>
          <xsl:text> -> </xsl:text>
          <xsl:call-template name="fullname">
            <xsl:with-param name="name" select="../../../@name"/>
            <xsl:with-param name="parentname" select="../../@name"/>
          </xsl:call-template>
          <xsl:text> [dir = "back", arrowtail = "empty", style = "dashed"];&#10;</xsl:text>
        </xsl:for-each>
      </xsl:if>
    </xsl:for-each>

    <xsl:for-each select="package/interface">
      <xsl:if test="extends_class and extends_class/classref/@name!='java.lang.Object'">
        <xsl:value-of select="extends_class/classref/@name"/>
        <xsl:text> ->
        </xsl:text>
        <xsl:value-of select="../@name"/>
        <xsl:text>.</xsl:text>
        <xsl:value-of select="@name"/>
        <xsl:text> [dir = "back", arrowtail = "empty"]; </xsl:text>
      </xsl:if>
      <xsl:if test="implements">
        <xsl:for-each select="implements/interfaceref">
          <xsl:value-of select="@name"/>
          <xsl:text> ->
          </xsl:text>
          <xsl:value-of select="../../../@name"/>
          <xsl:text>.</xsl:text>
          <xsl:value-of select="../../@name"/>
          <xsl:text> [dir = "back", arrowtail = "empty", style = "dashed"]; </xsl:text>
        </xsl:for-each>
      </xsl:if>
    </xsl:for-each>

    <xsl:text>
    }</xsl:text>
  </xsl:template>

  <xsl:template match="package">
    <xsl:text>        subgraph cluster</xsl:text>
    <xsl:call-template name="fullname">
      <xsl:with-param name="name" select="@name"/>
    </xsl:call-template>
    <xsl:text> {
            labeljust = "center";
            labelloc = "top";
            node [style = "filled"];
            z = "1";</xsl:text>
    <xsl:apply-templates/>
    <!-- rank same, min, max, source or sink
             rankdir TB LR (left to right) or TB (top to bottom)
             ranksep .75 separation between ranks, in inches.
             ratio approximate aspect ratio desired, fill or auto
             remincross if true and there are multiple clusters, re-run crossing minimization
           -->
    <!-- rank=same;
             rankdir=TB;
             ranksep=1;
             ratio=fill;
             remincross=true;
           -->
    <xsl:text>
            label = "</xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:text>";
            tooltip = "</xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:if test="$javasrcPath != ''">
      <xsl:text>";
            URL = "</xsl:text>
      <xsl:value-of select="$javasrcPath"/>
      <xsl:call-template name="filepath">
        <xsl:with-param name="name" select="../@name"/>
        <xsl:with-param name="parentname" select="@name"/>
      </xsl:call-template>
      <xsl:text>/package-summary.html</xsl:text>
    </xsl:if>
    <xsl:if test="$javadocPath != ''">
      <xsl:text>";
        URL = "</xsl:text>
      <xsl:value-of select="$javadocPath"/>
      <xsl:call-template name="filepath">
        <xsl:with-param name="name" select="../@name"/>
        <xsl:with-param name="parentname" select="@name"/>
      </xsl:call-template>
      <xsl:text>/package-summary.html</xsl:text>
    </xsl:if>
    <xsl:text>";
            color = "#000000";
            fillcolor = "#dddddd";
            style = "filled";
        }
</xsl:text>
  </xsl:template>

  <xsl:template match="class">
    <xsl:text>&#10;        </xsl:text>
    <xsl:call-template name="fullname">
      <xsl:with-param name="name" select="../@name"/>
      <xsl:with-param name="parentname" select="@name"/>
    </xsl:call-template>
    <xsl:text> [ </xsl:text>
    <xsl:choose>
      <xsl:when test="@extensiblity='abstract'">
        <xsl:text>
            color = "#848684",
            fillcolor = "#ced7ce",
            fontname = "Helvetica-Italic"</xsl:text>
      </xsl:when>
      <xsl:otherwise>
        <xsl:text>
            color = "#9c0031",
            fillcolor = "#ffffce",</xsl:text>
      </xsl:otherwise>
    </xsl:choose>
    <!-- need to replace newline chars with \n -->
    <!--comment="<xsl:value-of select="doc" />" ,-->
    <xsl:text>
            tooltip = "</xsl:text>
    <xsl:call-template name="name">
      <xsl:with-param name="name" select="../@name"/>
      <xsl:with-param name="parentname" select="@name"/>
    </xsl:call-template>
    <xsl:if test="$javasrcPath != ''">
      <xsl:text>",
            URL = "</xsl:text>
      <xsl:value-of select="$javasrcPath"/>
      <xsl:call-template name="filepath">
        <xsl:with-param name="name" select="../@name"/>
        <xsl:with-param name="parentname" select="@name"/>
      </xsl:call-template>
      <xsl:text>_java.html</xsl:text>
    </xsl:if>
    <xsl:text>",
            style = "filled",
            label = "{</xsl:text>
    <xsl:value-of select="@name"/>
    <xsl:if test="@extensibility='abstract'">
      <xsl:text> \{ abstract \} </xsl:text>
    </xsl:if>
    <xsl:text>|</xsl:text>
    <!-- attributes -->
    <xsl:for-each select="field">
      <xsl:if test="$show = 'private'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:when test="@access='package'">
            <xsl:text> ~ </xsl:text>
          </xsl:when>
          <xsl:when test="@access='private'">
            <xsl:text> - </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected' or @access='package' or @access='private'">
          <xsl:call-template name="printAttribute"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'package'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:when test="@access='package'">
            <xsl:text> ~ </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected' or @access='package'">
          <xsl:call-template name="printAttribute"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'protected'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected'">
          <xsl:call-template name="printAttribute"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'public' and (@access='public')">
        <xsl:text> + </xsl:text>

        <xsl:call-template name="printAttribute"/>
      </xsl:if>
    </xsl:for-each>

    <xsl:text>|</xsl:text>

    <!-- constructor -->
    <xsl:for-each select="constructor">
      <xsl:if test="$show = 'private'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='private'">
            <xsl:text> - </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:when test="@access='package'">
            <xsl:text> ~ </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected' or @access='package' or @access='private'">
          <xsl:call-template name="printConstructor"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'package'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:when test="@access='package'">
            <xsl:text> ~ </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected' or @access='package'">
          <xsl:call-template name="printConstructor"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'protected'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected'">
          <xsl:call-template name="printConstructor"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'public' and @access='public'">
        <xsl:text> + </xsl:text>

        <xsl:call-template name="printConstructor"/>
      </xsl:if>
    </xsl:for-each>

    <!-- methods -->
    <xsl:for-each select="method">
      <xsl:if test="$show = 'private'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='private'">
            <xsl:text> - </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:when test="@access='package'">
            <xsl:text> ~ </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected' or @access='package' or @access='private'">
          <xsl:call-template name="printMethod"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'package'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:when test="@access='package'">
            <xsl:text> ~ </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected' or @access='package'">
          <xsl:call-template name="printMethod"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'protected'">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
          </xsl:when>
          <xsl:when test="@access='protected'">
            <xsl:text> # </xsl:text>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>

        <xsl:if test="@access='public' or @access='protected'">
          <xsl:call-template name="printMethod"/>
        </xsl:if>
      </xsl:if>

      <xsl:if test="$show = 'public' and @access='public'">
        <xsl:text> + </xsl:text>

        <xsl:call-template name="printMethod"/>
      </xsl:if>

    </xsl:for-each>

    <xsl:text>}"
            ];</xsl:text>
  </xsl:template>

  <xsl:template match="interface">
    <xsl:text>
        </xsl:text>
    <xsl:call-template name="fullname">
      <xsl:with-param name="name" select="../@name"/>
      <xsl:with-param name="parentname" select="@name"/>
    </xsl:call-template>
    <xsl:text> [
            color = "#9c0031",
            fillcolor = "#deffff",
            label = "{\&lt;\&lt;interface\&gt;\&gt;\n</xsl:text>
    <xsl:value-of select="@name"/>

    <xsl:text>|</xsl:text>
    <!-- operations -->
    <xsl:if test="$show = 'private' or $show = 'package' or $show = 'protected' or $show = 'public'">
      <xsl:for-each select="method">
        <xsl:choose>
          <xsl:when test="@access='public'">
            <xsl:text> + </xsl:text>
            <xsl:call-template name="printMethod"/>
          </xsl:when>
          <xsl:otherwise>
            <xsl:text/>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:for-each>
    </xsl:if>
    <xsl:text>\l}"
            ];</xsl:text>
  </xsl:template>

  <xsl:template match="doc"/>

  <xsl:template match="extends"/>

  <xsl:template match="field"/>

  <xsl:template match="constructor"/>

  <xsl:template match="method"/>

  <xsl:template name="printAttribute">
    <xsl:if test="@static='true'">
      <xsl:text> $ </xsl:text>
    </xsl:if>

    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="@name"/>
      <xsl:with-param name="marker" select="'.'"/>
    </xsl:call-template>
    <xsl:text> : </xsl:text>
    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="primitive/@type"/>
      <xsl:with-param name="marker" select="'.'"/>
    </xsl:call-template>
    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="classref/@name"/>
      <xsl:with-param name="marker" select="'.'"/>
    </xsl:call-template>

    <xsl:if test="@final='true'">
      <xsl:text> \{ final \} </xsl:text>
    </xsl:if>

    <xsl:text>\l</xsl:text>
  </xsl:template>

  <xsl:template name="printConstructor">
    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="@name"/>
      <xsl:with-param name="marker" select="'.'"/>
    </xsl:call-template>
    <xsl:text>(</xsl:text>
    <xsl:for-each select="parameter">
      <xsl:value-of select="primitive/@type"/>
      <xsl:call-template name="substring-after-last">
        <xsl:with-param name="input" select="classref/@name"/>
        <xsl:with-param name="marker" select="'.'"/>
      </xsl:call-template>
      <xsl:if test="not(position()=last())">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>)</xsl:text>

    <xsl:text>\l</xsl:text>
  </xsl:template>

  <xsl:template name="printMethod">
    <xsl:if test="@static='true'">
      <xsl:text> $ </xsl:text>
    </xsl:if>

    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="@name"/>
      <xsl:with-param name="marker" select="'.'"/>
    </xsl:call-template>
    <xsl:text>(</xsl:text>
    <xsl:for-each select="parameter">
      <xsl:value-of select="primitive/@type"/>
      <xsl:call-template name="substring-after-last">
        <xsl:with-param name="input" select="classref/@name"/>
        <xsl:with-param name="marker" select="'.'"/>
      </xsl:call-template>
      <xsl:if test="not(position()=last())">
        <xsl:text>, </xsl:text>
      </xsl:if>
    </xsl:for-each>
    <xsl:text>) : </xsl:text>
    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="returns/primitive/@type"/>
      <xsl:with-param name="marker" select="'.'"/>
    </xsl:call-template>
    <xsl:call-template name="substring-after-last">
      <xsl:with-param name="input" select="returns/classref/@name"/>
      <xsl:with-param name="marker" select="'.'"/>
    </xsl:call-template>

    <xsl:if test="@extensibility='abstract' or @native='true' or @synchronized='true'">
      <xsl:text> \{</xsl:text>
    </xsl:if>
    <xsl:if test="@extensibility='abstract'">
      <xsl:text> abstract </xsl:text>
    </xsl:if>
    <xsl:if test="@native='true'">
      <xsl:text> native </xsl:text>
    </xsl:if>
    <xsl:if test="@synchronized='true'">
      <xsl:text> synchronized </xsl:text>
    </xsl:if>
    <xsl:if test="@extensibility='abstract' or @native='true' or @synchronized='true'">
      <xsl:text>\} </xsl:text>
    </xsl:if>

    <xsl:text>\l</xsl:text>
  </xsl:template>

  <!-- Utilities templates -->
  <xsl:template name="name">
    <xsl:param name="name"/>
    <xsl:param name="parentname"/>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$name"/>
      <xsl:with-param name="replace" select="'.'"/>
      <xsl:with-param name="with" select="'.'"/>
    </xsl:call-template>
    <xsl:if test="$parentname!=''">.</xsl:if>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$parentname"/>
      <xsl:with-param name="replace" select="'.'"/>
      <xsl:with-param name="with" select="'.'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="fullname">
    <xsl:param name="name"/>
    <xsl:param name="parentname"/>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$name"/>
      <xsl:with-param name="replace" select="'.'"/>
      <xsl:with-param name="with" select="'_'"/>
    </xsl:call-template>
    <xsl:if test="$parentname!=''">_</xsl:if>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$parentname"/>
      <xsl:with-param name="replace" select="'.'"/>
      <xsl:with-param name="with" select="'_'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="filepath">
    <xsl:param name="name"/>
    <xsl:param name="parentname"/>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$name"/>
      <xsl:with-param name="replace" select="'.'"/>
      <xsl:with-param name="with" select="'/'"/>
    </xsl:call-template>
    <xsl:if test="$parentname!=''">/</xsl:if>
    <xsl:call-template name="replace-string">
      <xsl:with-param name="text" select="$parentname"/>
      <xsl:with-param name="replace" select="'.'"/>
      <xsl:with-param name="with" select="'/'"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="replace-string">
    <xsl:param name="text"/>
    <xsl:param name="replace"/>
    <xsl:param name="with"/>
    <xsl:choose>
      <xsl:when test="contains($text,$replace)">
        <xsl:value-of select="substring-before($text,$replace)"/>
        <xsl:value-of select="$with"/>
        <xsl:call-template name="replace-string">
          <xsl:with-param name="text" select="substring-after($text,$replace)"/>
          <xsl:with-param name="replace" select="$replace"/>
          <xsl:with-param name="with" select="$with"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$text"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="substring-after-last">
    <xsl:param name="input"/>
    <xsl:param name="marker"/>
    <xsl:choose>
      <xsl:when test="contains($input,$marker)">
        <xsl:call-template name="substring-after-last">
          <xsl:with-param name="input" select="substring-after($input,$marker)"/>
          <xsl:with-param name="marker" select="$marker"/>
        </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
        <xsl:value-of select="$input"/>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
</xsl:stylesheet>
