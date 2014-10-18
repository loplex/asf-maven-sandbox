package org.apache.maven.dist.tools;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * @author Karl Heinz Marbaise
 */
@Mojo( name = "list-plugins-prerequisites", requiresProject = false )
public class ListPluginsPrerequisitesMojo
    extends AbstractMavenReport
{

    /**
     * Site renderer.
     */
    @Component
    protected Renderer siteRenderer;

    /**
     * Reporting directory.
     */
    @Parameter( defaultValue = "${project.reporting.outputDirectory}", required = true )
    protected File outputDirectory;

    /**
     * Maven project.
     */
    @Parameter( defaultValue = "${project}", readonly = true, required = true )
    protected MavenProject project;

    @Override
    public String getName( Locale locale )
    {
        return "Dist Tool> List Plugins Prerequisites";
    }

    @Override
    public String getDescription( Locale locale )
    {
        return "Maven and JDK version prerequisites for plugins";
    }

    @Override
    protected void executeReport( Locale locale )
        throws MavenReportException
    {
        if ( !outputDirectory.exists() )
        {
            outputDirectory.mkdirs();
        }

        GetPrerequisites prerequisites = new GetPrerequisites();

        Sink sink = getSink();

        sink.head();
        sink.title();
        sink.text( "Display Plugins Prerequisites" );
        sink.title_();
        sink.head_();
        sink.body();

        Map<ArtifactVersion, List<PluginPrerequisites>> groupedPrequisites = prerequisites.getGroupedPrequisites();

        sink.table();

        ArrayList<ArtifactVersion> sortedVersion = new ArrayList<ArtifactVersion>();
        sortedVersion.addAll( groupedPrequisites.keySet() );
        
        Collections.<ArtifactVersion>sort( sortedVersion );

        for ( ArtifactVersion mavenVersion : sortedVersion)
        {
            List<PluginPrerequisites> pluginsPrerequisites = groupedPrequisites.get( mavenVersion );

            sink.tableRow();
            sink.tableHeaderCell();
            sink.rawText( "Maven Version Prerequisite " + mavenVersion + " (" + pluginsPrerequisites.size() + " / "
                + prerequisites.pluginNames.length + ")" );
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.rawText( "Maven Version");
            sink.tableHeaderCell_();

            sink.tableHeaderCell();
            sink.rawText( "JDK Version");
            sink.tableHeaderCell_();

            sink.tableRow_();

            for ( PluginPrerequisites pluginPrerequisites : pluginsPrerequisites )
            {
                sink.tableRow();
                sink.tableCell();
                sink.link( prerequisites.getPluginInfoUrl( pluginPrerequisites.getPluginName() ) );
                sink.text( pluginPrerequisites.getPluginName() );
                sink.link_();
                sink.text( " " );
                sink.text( pluginPrerequisites.getPluginVersion() );
                sink.tableCell_();
                sink.tableCell();
                sink.text( pluginPrerequisites.getMavenVersion().toString() );
                sink.tableCell_();

                sink.tableCell();
                sink.text( pluginPrerequisites.getJdkVersion() );
                sink.tableCell_();
                sink.tableRow_();
            }

        }

        sink.table_();
        sink.body_();
    }

    @Override
    public String getOutputName()
    {
        return "dist-tool-prerequisites";
    }

    @Override
    protected Renderer getSiteRenderer()
    {
        return siteRenderer;
    }

    @Override
    protected String getOutputDirectory()
    {
        return outputDirectory.getAbsolutePath();
    }

    @Override
    protected MavenProject getProject()
    {
        return project;
    }

}