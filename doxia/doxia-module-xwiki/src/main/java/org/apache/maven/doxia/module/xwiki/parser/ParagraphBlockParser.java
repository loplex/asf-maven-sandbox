package org.apache.maven.doxia.module.xwiki.parser;

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

import org.apache.maven.doxia.module.xwiki.blocks.Block;
import org.apache.maven.doxia.module.xwiki.blocks.ParagraphBlock;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.util.ByLineSource;

public class ParagraphBlockParser
    extends AbstractBlockParser
{
    private BlockParser[] parsers;

    public ParagraphBlockParser( BlockParser[] parsers )
    {
        super();
        this.parsers = parsers;
    }

    public boolean accept( String line, ByLineSource source )
    {
        return true;
    }

    public Block visit( String line, ByLineSource source )
        throws ParseException
    {

        ChildBlocksBuilder builder = new ChildBlocksBuilder( appendUntilEmptyLine( line, source ) );
        builder.setCompatibilityMode( isInCompatibilityMode() );
        return new ParagraphBlock( builder.getBlocks() );
    }

    /**
     * Slurp lines from the source starting with the given line appending them together into a StringBuffer until an
     * empty line is reached, and while the source contains more lines.
     *
     * @param line   the first line
     * @param source the source to read new lines from
     * @return a StringBuffer appended with lines
     * @throws org.apache.maven.doxia.parser.ParseException
     *
     */
    private String appendUntilEmptyLine( String line, ByLineSource source )
        throws ParseException
    {
        StringBuffer text = new StringBuffer();

        do
        {

            if ( line.trim().length() == 0 )
            {
                break;
            }

            boolean accepted = false;
            for ( int i = 0; i < parsers.length; i++ )
            {
                BlockParser parser = parsers[i];
                if ( parser.accept( line, source ) )
                {
                    accepted = true;
                    break;
                }
            }
            if ( accepted )
            {
                // Slightly fragile - if any of the parsers need to do this in order to decide whether to accept a line,
                // then it will barf because of the contract of ByLineSource
                source.ungetLine();
                break;
            }

            if ( text.length() == 0 )
            {
                text.append( line.trim() );
            }
            else
            {
                text.append( " " + line.trim() );
            }

        }
        while ( ( line = source.getNextLine() ) != null );

        return text.toString();
    }

}