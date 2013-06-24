package org.apache.commons.plugins.gpg;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.gpg.GpgSigner;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Copy of org.apache.maven.plugin.gpg.AbstractGpgMojo moved to o.a.c package.
 * Unfortunately, AbstractGpgMojo does not use annotations, so it cannot be extended externally.
 * Also, we need access to the method newSigner() which is package protected.
 * The hope is that this functionality will eventually be added to the gpg plugin.
 */
public abstract class AbstractGpgMojo
    extends AbstractMojo
{

    /**
     * The directory from which gpg will load keyrings. If not specified, gpg will use the value configured for its
     * installation, e.g. <code>~/.gnupg</code> or <code>%APPDATA%/gnupg</code>.
     *
     * @since 1.0
     */
    @Parameter( property = "gpg.homedir" )
    private File homedir;

    /**
     * The passphrase to use when signing.
     */
    @Parameter( property = "gpg.passphrase" )
    private String passphrase;

    /**
     * The "name" of the key to sign with. Passed to gpg as <code>--local-user</code>.
     */
    @Parameter( property = "gpg.keyname" )
    private String keyname;

    /**
     * Passes <code>--use-agent</code> or <code>--no-use-agent</code> to gpg. If using an agent, the passphrase is
     * optional as the agent will provide it.
     * For gpg2, specify true as --no-use-agent was removed in gpg2 and doesn't ask for a passphrase anymore.
     */
    @Parameter( property = "gpg.useagent", defaultValue = "false" )
    private boolean useAgent;

    /**
     */
    @Parameter( defaultValue = "${settings.interactiveMode}", readonly = true )
    private boolean interactive;

    /**
     * The path to the GnuPG executable to use for artifact signing. Defaults to either "gpg" or "gpg.exe" depending on
     * the operating system.
     *
     * @since 1.1
     */
    @Parameter( property = "gpg.executable" )
    private String executable;

    /**
     * Whether to add the default keyrings from gpg's home directory to the list of used keyrings.
     *
     * @since 1.2
     */
    @Parameter( property = "gpg.defaultKeyring", defaultValue = "true" )
    private boolean defaultKeyring;

    /**
     * The path to a secret keyring to add to the list of keyrings. By default, only the {@code secring.gpg} from gpg's
     * home directory is considered. Use this option (in combination with {@link #publicKeyring} and
     * {@link #defaultKeyring} if required) to use a different secret key. <em>Note:</em> Relative paths are resolved
     * against gpg's home directory, not the project base directory.
     *
     * @since 1.2
     */
    @Parameter( property = "gpg.secretKeyring" )
    private String secretKeyring;

    /**
     * The path to a public keyring to add to the list of keyrings. By default, only the {@code pubring.gpg} from gpg's
     * home directory is considered. Use this option (and {@link #defaultKeyring} if required) to use a different public
     * key. <em>Note:</em> Relative paths are resolved against gpg's home directory, not the project base directory.
     *
     * @since 1.2
     */
    @Parameter( property = "gpg.publicKeyring" )
    private String publicKeyring;

    GpgSigner newSigner( MavenProject project )
        throws MojoExecutionException, MojoFailureException
    {
        GpgSigner signer = new GpgSigner();

        signer.setExecutable( executable );
        signer.setInteractive( interactive );
        signer.setKeyName( keyname );
        signer.setUseAgent( useAgent );
        signer.setHomeDirectory( homedir );
        signer.setDefaultKeyring( defaultKeyring );
        signer.setSecretKeyring( secretKeyring );
        signer.setPublicKeyring( publicKeyring );

        signer.setPassPhrase( passphrase );
        if ( null == passphrase && !useAgent )
        {
            if ( !interactive )
            {
                throw new MojoFailureException( "Cannot obtain passphrase in batch mode" );
            }
            try
            {
                signer.setPassPhrase( signer.getPassphrase( project ) );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Exception reading passphrase", e );
            }
        }

        return signer;
    }

}
