package org.apache.maven.scm.provider.svn.svnjava;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.svn.AbstractSvnScmProvider;
import org.apache.maven.scm.provider.svn.command.SvnCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.add.SvnAddCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.changelog.SvnChangeLogCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.checkin.SvnCheckInCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.checkout.SvnCheckOutCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.diff.SvnDiffCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.remove.SvnRemoveCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.status.SvnStatusCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.tag.SvnTagCommand;
import org.apache.maven.scm.provider.svn.svnjava.command.update.SvnUpdateCommand;
import org.apache.maven.scm.provider.svn.svnjava.repository.SvnJavaScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.codehaus.plexus.util.StringUtils;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.internal.io.svn.SVNRepositoryFactoryImpl;

import java.io.File;
import java.net.URI;
import java.util.Collections;

/**
 * @author <a href="mailto:dh-maven@famhq.com">David Hawkins</a>
 * @version $Id$
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider" role-hint="javasvn"
 */
public class SvnJavaScmProvider
    extends AbstractSvnScmProvider
{
    public static final String COMMAND_LINE = "JavaSVN Library";

    private static boolean initialized = false;

    /** {@inheritDoc} */
    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        initialize();

        try
        {
            File f;
            if ( scmSpecificUrl.trim().startsWith( "file" ) )
            {
                try
                {
                    f = new File( new URI( scmSpecificUrl ) );
                }
                catch ( Exception e )
                {
                    // nop
                    f = new File( scmSpecificUrl );
                }
            }
            else
            {
                f = new File( scmSpecificUrl );
            }

            SVNURL url;
            if ( f.exists() )
            {
                url = SVNURL.fromFile( f );
            }
            else
            {
                url = SVNURL.parseURIEncoded( scmSpecificUrl );
            }

            // The existing svn provider pattern is to strip the username (if any)
            // from the url.
            String strUrl = url.toString();
            if ( url.getUserInfo() != null )
            {
                strUrl = StringUtils.replace( strUrl, url.getUserInfo() + "@", "" );
            }

            return new SvnJavaScmProviderRepository( url, strUrl );
        }
        catch ( SVNException e )
        {
            throw new ScmRepositoryException( "The scm url is invalid: " + e.getMessage(),
                                              Collections.singletonList( e.getMessage() ) );
        }
    }

    /** {@inheritDoc} */
    protected SvnCommand getAddCommand()
    {
        return new SvnAddCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getChangeLogCommand()
    {
        return new SvnChangeLogCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getCheckInCommand()
    {
        return new SvnCheckInCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getCheckOutCommand()
    {
        return new SvnCheckOutCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getDiffCommand()
    {
        return new SvnDiffCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getRemoveCommand()
    {
        return new SvnRemoveCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getStatusCommand()
    {
        return new SvnStatusCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getTagCommand()
    {
        return new SvnTagCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getUpdateCommand()
    {
        return new SvnUpdateCommand();
    }

    /** {@inheritDoc} */
    protected SvnCommand getBranchCommand()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    protected SvnCommand getExportCommand()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    protected SvnCommand getInfoCommand()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    protected SvnCommand getListCommand()
    {
        // TODO Auto-generated method stub
        return null;
    }

    /** {@inheritDoc} */
    protected String getRepositoryURL( File path )
        throws ScmException
    {
        try
        {
            return SVNURL.fromFile( path ).getURIEncodedPath();
        }
        catch ( SVNException e )
        {
            throw new IllegalArgumentException( e.getMessage() );
        }
    }

    /**
     * Initializes the library to work with a repository either via svn:// (and
     * svn+ssh://) or via http:// (and https://)
     */
    private static void initialize()
    {
        if ( initialized )
        {
            return;
        }

        /*
         * for DAV (over http and https)
         */
        DAVRepositoryFactory.setup();

        /*
        * for svn (over svn and svn+ssh)
        */
        SVNRepositoryFactoryImpl.setup();

        /*
         * for file
         * TODO activate it when a new release of javasvn will be available, so we'll can run TCK tests
         */
        // FSRepositoryFactory.setup();
        initialized = true;
    }
}
