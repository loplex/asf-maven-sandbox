package org.codehaus.mojo.tools.antcall;

/*
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
 *
 */

import org.apache.maven.project.MavenProject;
import org.apache.tools.ant.PropertyHelper;
import org.codehaus.plexus.util.introspection.ReflectionValueExtractor;

/**
 * Makes the ${expressions} used in Maven available to Ant as properties.
 *
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 */
public class AntPropertyHelper
    extends PropertyHelper
{
    private final MavenProject mavenProject;
    private final AntCallLogger logger;

    public AntPropertyHelper( MavenProject project, AntCallLogger logger )
    {
        this.mavenProject = project;
        this.logger = logger;
    }

    public synchronized Object getPropertyHook( String ns, String name, boolean user )
    {
        logger.debug( "getProperty(ns=" + ns + ", name=" + name + ", user=" + user + ")" );

        try
        {
            if ( name.startsWith( "project." ) || name.equals( "basedir" ) )
            {
                Object val = ReflectionValueExtractor.evaluate( name.substring( "project.".length() ), mavenProject );

                if ( val != null )
                {
                    return val;
                }
            }
        }
        catch ( Exception e )
        {
            logger.info( "Error evaluating expression '" + name + "'", e );
            e.printStackTrace();
        }

        Object val = super.getPropertyHook( ns, name, user );

        if ( val == null )
        {
            val = System.getProperty( name.toString() );
        }

        return val;
    }
}
