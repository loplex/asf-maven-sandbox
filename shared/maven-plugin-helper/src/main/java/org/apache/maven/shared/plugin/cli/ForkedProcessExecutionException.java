package org.apache.maven.shared.plugin.cli;

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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Execution of the forked process failed
 * 
 * @author <a href="mailto:nicolas@apache.org">Nicolas De Loof</a>
 */
public class ForkedProcessExecutionException
    extends MojoExecutionException
{

    /**
     * @param source
     * @param shortMessage
     * @param longMessage
     */
    public ForkedProcessExecutionException( Object source, String shortMessage, String longMessage )
    {
        super( source, shortMessage, longMessage );
    }

    /**
     * @param message
     * @param cause
     */
    public ForkedProcessExecutionException( String message, Exception cause )
    {
        super( message, cause );
    }

    /**
     * @param message
     * @param cause
     */
    public ForkedProcessExecutionException( String message, Throwable cause )
    {
        super( message, cause );
    }

    /**
     * @param message
     */
    public ForkedProcessExecutionException( String message )
    {
        super( message );
    }

}
