package org.apache.maven.reducto.analysis;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;

/**
 * The default JAR analysis result.
 */
public class DefaultJarAnalysisResult
    extends AbstractLogEnabled
    implements JarAnalysisResult
{
    public static final String ROLE = DefaultJarAnalysisResult.class.getName();

    private File jar;

    private boolean isSealed;

    private String groupId;

    private String artifactId;

    private String version;

    private String md5Checksum;

    private String vendor;

    public DefaultJarAnalysisResult( File jar )
    {
        this.jar = jar;
    }

    public File getJar()
    {
        return jar;
    }

    // Accessors

    public String getGroupId()
    {
        return groupId;
    }

    public void setGroupId( String groupId )
    {
        this.groupId = groupId;
    }

    public String getArtifactId()
    {
        return artifactId;
    }

    public void setArtifactId( String artifactId )
    {
        this.artifactId = artifactId;
    }

    public String getVersion()
    {
        return version;
    }

    public void setVersion( String version )
    {
        this.version = version;
    }

    public String getMd5Checksum()
    {
        return md5Checksum;
    }

    public void setMd5Checksum( String md5Checksum )
    {
        this.md5Checksum = md5Checksum;
    }

    public boolean isResolved()
    {
        return ( groupId != null ) && ( artifactId != null ) && ( version != null );
    }
}
