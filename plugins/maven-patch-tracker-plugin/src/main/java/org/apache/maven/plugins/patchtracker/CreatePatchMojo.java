package org.apache.maven.plugins.patchtracker;

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
import org.apache.maven.plugins.patchtracker.tracking.PatchTracker;
import org.apache.maven.plugins.patchtracker.tracking.PatchTrackerException;
import org.apache.maven.plugins.patchtracker.tracking.PatchTrackerRequest;
import org.apache.maven.plugins.patchtracker.tracking.PatchTrackerResult;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

/**
 * Goal which create a diff/patch file from the current project and create an issue in the project
 * with attaching the created patch file
 *
 * @goal create
 * @aggregator
 */
public class CreatePatchMojo
    extends AbstractPatchMojo
{


    public void execute()
        throws MojoExecutionException
    {
        // TODO do a status before and complains if some files in to be added status ?

        String patchContent = getPatchContent();

        PatchTrackerRequest patchTrackerRequest = buidPatchTrackerRequest( true );

        patchTrackerRequest.setPatchContent( patchContent );

        getLog().debug( patchTrackerRequest.toString() );

        try
        {
            PatchTracker patchTracker = getPatchTracker();
            PatchTrackerResult result = patchTracker.createPatch( patchTrackerRequest );
            getLog().info( "issue created with id:" + result.getPatchId() + ", url:" + result.getPatchUrl() );
        }
        catch ( ComponentLookupException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
        catch ( PatchTrackerException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }


    }


}
