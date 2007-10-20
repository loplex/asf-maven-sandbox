package org.apache.maven.plugin.jxr;

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

import org.apache.maven.plugin.jxr.JavasrcReport;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;

/**
 * <code>Unit tests</code> of JXR plugin
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 */
public class JavasrcReportTest
    extends AbstractMojoTestCase
{
    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp()
        throws Exception
    {
        // required for mojo lookups to work
        super.setUp();
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    protected void tearDown()
        throws Exception
    {
        // nop
    }

    /**
     * Method to test javasrc generation
     *
     * @throws Exception
     */
    public void testJavasrc()
        throws Exception
    {
        File testPom = new File( getBasedir(), "src/test/resources/unit/dummy-project-test/pom.xml" );
        JavasrcReport mojo = (JavasrcReport) lookupMojo( "javasrc", testPom );
        mojo.execute();

        File outputDir = new File( getBasedir(), "target/test/unit/dummy-project-test/target/site" );
        assertTrue( new File( outputDir, "index.html" ).exists() );
        assertTrue( new File( outputDir, "/maven/App_java.html" ).exists() );
    }
}
