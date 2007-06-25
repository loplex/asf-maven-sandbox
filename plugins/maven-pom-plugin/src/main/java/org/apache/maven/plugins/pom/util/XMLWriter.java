package org.apache.maven.plugins.pom.util;

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

import org.dom4j.Document;
import org.dom4j.io.OutputFormat;

import java.io.IOException;
import java.io.Writer;

/**
 * XMLWriter 
 *
 * @author <a href="mailto:joakim@erdfelt.com">Joakim Erdfelt</a>
 * @version $Id: XMLWriter.java 529613 2007-04-17 14:10:16Z joakime $
 */
public class XMLWriter
{
    public static void write( Document doc, Writer writer )
        throws XMLException
    {
        try
        {
            OutputFormat outputFormat = OutputFormat.createPrettyPrint();
            org.dom4j.io.XMLWriter xmlwriter = new org.dom4j.io.XMLWriter( writer, outputFormat );
            xmlwriter.write( doc );
            xmlwriter.flush();
        }
        catch ( IOException e )
        {
            throw new XMLException( "Unable to write xml contents to writer: " + e.getMessage(), e );
        }
        
    }
}
