package org.apache.maven.doxia.editor;

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

import org.apache.maven.doxia.Doxia;
import org.apache.maven.doxia.editor.action.manager.ActionManager;
import org.apache.maven.doxia.editor.io.DoxiaDocumentBuilder;
import org.apache.maven.doxia.editor.windows.EditorWindow;
import org.codehaus.plexus.PlexusConstants;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.context.Context;
import org.codehaus.plexus.context.ContextException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class DefaultApplication
    implements Application, Contextualizable, Startable
{
    private Doxia doxia;

    private DoxiaDocumentBuilder doxiaDocumentBuilder;

    private PlexusContainer container;

    public EditorWindow editorWindow;

    public ActionManager getActionManager()
    {
        return (ActionManager) lookup( ActionManager.ROLE );
    }

    public Object lookup( String role )
    {
        try
        {
            return container.lookup( role );
        }
        catch ( ComponentLookupException e )
        {
            throw new RuntimeException( "error while looking up '" + role + "'.", e );
        }
    }

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public EditorWindow getEditorWindow()
    {
        return editorWindow;
    }

    public void setEditorWindow( EditorWindow editorWindow )
    {
        this.editorWindow = editorWindow;
    }

    public Doxia getDoxia()
    {
        return doxia;
    }

    public DoxiaDocumentBuilder getDoxiaDocumentBuilder()
    {
        return doxiaDocumentBuilder;
    }

    // ----------------------------------------------------------------------
    // Component Lifecycle
    // ----------------------------------------------------------------------

    public void contextualize( Context context )
        throws ContextException
    {
        container = (PlexusContainer) context.get( PlexusConstants.PLEXUS_KEY );
    }

    public void start()
        throws StartingException
    {
    }

    public void stop()
        throws StoppingException
    {
    }
}
