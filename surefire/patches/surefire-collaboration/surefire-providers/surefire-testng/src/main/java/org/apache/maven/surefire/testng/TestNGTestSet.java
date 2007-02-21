package org.apache.maven.surefire.testng;

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

import org.apache.maven.surefire.report.ReporterManager;
import org.apache.maven.surefire.testset.AbstractTestSet;
import org.apache.maven.surefire.testset.TestSetFailedException;

/**
 * Main plugin point for running testng tests within the Surefire runtime
 * infrastructure.
 *
 * @author jkuhnert
 */
public class TestNGTestSet
    extends AbstractTestSet
{
    /**
     * Creates a new test testset that will process the class being
     * passed in to determine the testing configuration.
     */
    public TestNGTestSet( Class testClass )
    {
        super( testClass );
    }

    public int getTestCount()
        throws TestSetFailedException
    {
        // TODO: need to get this from TestNG somehow
        return 1;
    }

    public void execute( ReporterManager reportManager, ClassLoader loader )
    {
        throw new UnsupportedOperationException(
            "This should have been called directly from TestNGDirectoryTestSuite" );
    }
}
