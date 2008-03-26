package org.maven.maven.plugin.aapt;

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

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.android.CommandExecutor;
import org.apache.maven.android.ExecutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.Artifact;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Shane Isbell
 * @goal compile
 * @phase compile
 * @description
 */
public class AaptCompilerMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;

    /**
     * @parameter expression="${settings.localRepository}"
     * @required
     */
    private File localRepository;

    /**
     * @component
     */
    private ArtifactFactory artifactFactory;

    /**
     * @parameter default-value = "m3-rc20a"
     */
    private String androidVersion;

    public void execute() throws MojoExecutionException, MojoFailureException {

        CommandExecutor executor = CommandExecutor.Factory.createDefaultCommmandExecutor();
        executor.setLogger(this.getLog());

        File resourceDirectory = new File(project.getBasedir(), "res");

        Artifact artifact = artifactFactory.createArtifact("android", "android", androidVersion, "jar", "jar");
        ArtifactRepositoryLayout defaultLayout = new DefaultRepositoryLayout();
        System.out.println(defaultLayout.pathOf(artifact));
        File androidJar = new File(localRepository, defaultLayout.pathOf(artifact));

        List<String> commands = new ArrayList<String>();
        commands.add("compile");
        commands.add("-m");
        commands.add("-J");
        commands.add(project.getBuild().getSourceDirectory());

        commands.add("-M");
        commands.add(project.getBasedir().getAbsolutePath() + File.separatorChar + "AndroidManifest.xml");
        if (resourceDirectory.exists()) {
            commands.add("-S");
            commands.add(resourceDirectory.getAbsolutePath());
        }
        commands.add("-I");
        commands.add(androidJar.getAbsolutePath());
        getLog().info("aapt " + commands.toString());
        try {
            executor.executeCommand("aapt", commands, project.getBasedir(), false);
        } catch (ExecutionException e) {
            throw new MojoExecutionException("", e);
        }
    }
}
