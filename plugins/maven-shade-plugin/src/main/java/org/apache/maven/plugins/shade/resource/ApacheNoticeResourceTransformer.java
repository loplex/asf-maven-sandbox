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

package org.apache.maven.plugins.shade.resource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public class ApacheNoticeResourceTransformer
    implements ResourceTransformer
{
    Set entries = new LinkedHashSet();
    Map organizationEntries = new LinkedHashMap();
    
    String projectName;
    
    String preamble1 = 
          "// ------------------------------------------------------------------\n"
        + "// NOTICE file corresponding to the section 4d of The Apache License,\n"
        + "// Version 2.0, in this case for ";
    
    String preamble2 = "\n// ------------------------------------------------------------------\n";
        
    String preamble3 = "This product includes software developed at\n";
    
    //defaults overridable via config in pom
    String organizationName = "The Apache Software Foundation";
    String organizationURL = "http://www.apache.org/";
    String inceptionYear = "2006";
    
    String copyright;

        
    public boolean canTransformResource( String resource )
    {
        String s = resource.toLowerCase();

        if (s.equals( "meta-inf/notice.txt" ) || s.equals( "meta-inf/notice" ) )
        {
            return true;
        }

        return false;
    }

    public void processResource( InputStream is )
        throws IOException
    {
        if ( entries.isEmpty() ) 
        {
            String year = new SimpleDateFormat( "yyyy" ).format( new Date() );
            if ( !inceptionYear.equals( year ) ) 
            {
                year = inceptionYear + "-" + year;
            }
            
            
            //add headers
            entries.add( preamble1 + projectName + preamble2 );
            //fake second entry, we'll look for a real one later
            entries.add( projectName + "\nCopyright " + year + " " + organizationName + "\n" );
            entries.add( preamble3 + organizationName + " ("+ organizationURL +").\n" );
        }
        
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        
        String line = reader.readLine();
        StringBuffer sb = new StringBuffer();
        Set currentOrg = null;
        int lineCount = 0;
        while ( line != null )
        {
            String trimedLine = line.trim();
            
            if ( !trimedLine.startsWith( "//" ) )
            {
                if ( trimedLine.length() > 0 )
                {
                    if ( trimedLine.startsWith( "- " ) )
                    {
                        //resource-bundle 1.3 mode
                        if ( lineCount == 1 
                            && sb.toString().contains( "This product includes/uses software(s) developed by" ))
                        {
                            currentOrg = (Set) organizationEntries.get( sb.toString().trim() );
                            if ( currentOrg == null )
                            {
                                currentOrg = new TreeSet();
                                organizationEntries.put( sb.toString().trim(), currentOrg );
                            }
                            sb = new StringBuffer();
                        } 
                        else if ( sb.length() > 0 && currentOrg != null )
                        {
                            currentOrg.add( sb.toString() );
                            sb = new StringBuffer();
                        }
                        
                    }
                    sb.append( line ).append( "\n" );
                    lineCount++;
                }
                else
                {
                    String ent = sb.toString();
                    if ( ent.startsWith( projectName ) && ent.contains( "Copyright " ) ) 
                    {
                        copyright = ent;
                    }
                    if ( currentOrg == null )
                    {
                        entries.add( ent );                        
                    }
                    else
                    {
                        currentOrg.add( ent );
                    }
                    sb = new StringBuffer();
                    lineCount = 0;
                    currentOrg = null;
                }
            }
            
            line = reader.readLine();
        }
    }

    public boolean hasTransformedResource()
    {
        return true;
    }

    public void modifyOutputStream( JarOutputStream jos )
        throws IOException
    {
        jos.putNextEntry( new JarEntry( "META-INF/NOTICE" ) );
        
        OutputStreamWriter writer = new OutputStreamWriter( jos );
        
        int count = 0;
        for ( Iterator itr = entries.iterator() ; itr.hasNext() ; )
        {
            ++count;
            String line = (String) itr.next();
            if ( line.equals( copyright ) && count != 2)
            {
                continue;
            }
            
            if ( count == 2 && copyright != null ) 
            {
                writer.append( copyright );
                writer.append( '\n' );
            }
            else
            {
                writer.append( line );
                writer.append( '\n' );
            }
            if (count == 3) 
            {
                //do org stuff
                for (Iterator oit = organizationEntries.entrySet().iterator() ; oit.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) oit.next();
                    writer.append( entry.getKey().toString() ).append( '\n' );
                    Set entrySet = (Set)entry.getValue();
                    for (Iterator eit = entrySet.iterator() ; eit.hasNext() ;)
                    {
                        writer.append( eit.next().toString() );                        
                    }
                    writer.append( '\n' );
                }
            }
        }
        
        writer.flush();
        
        entries.clear();
    }
}
