package org.apache.maven.doxia.book;

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

import org.apache.maven.doxia.book.context.BookContext;
import org.apache.maven.doxia.book.model.BookModel;
import org.apache.maven.doxia.book.services.indexer.BookIndexer;
import org.apache.maven.doxia.book.services.io.BookIo;
import org.apache.maven.doxia.book.services.renderer.BookRenderer;
import org.apache.maven.doxia.book.services.validation.BookValidator;
import org.apache.maven.doxia.book.services.validation.ValidationResult;
import org.codehaus.plexus.logging.AbstractLogEnabled;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 * @plexus.component
 */
public class DefaultBookDoxia
    extends AbstractLogEnabled
    implements BookDoxia
{
    /**
     * @plexus.requirement
     */
    private BookIo bookIo;

    /**
     * @plexus.requirement
     */
    private BookValidator bookValidator;

    /**
     * @plexus.requirement
     */
    private BookIndexer bookIndexer;

    /**
     * @plexus.requirement role="org.apache.maven.doxia.book.services.renderer.BookRenderer"
     */
    private Map bookRenderers;

    // ----------------------------------------------------------------------
    // BookDoxia Implementation
    // ----------------------------------------------------------------------

    public BookModel loadBook( File bookDescriptor )
        throws BookDoxiaException
    {
        return bookIo.readBook( bookDescriptor );
    }

    public void renderBook( BookModel book, String bookRendererId, List files, File outputDirectory )
        throws BookDoxiaException
    {
        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        ValidationResult validationResult = bookValidator.validateBook( book );

        if ( !validationResult.isAllOk() )
        {
            throw new InvalidBookDescriptorException( validationResult );
        }

        // ----------------------------------------------------------------------
        // Create and initialize the context
        // ----------------------------------------------------------------------

        BookContext context = new BookContext();

        context.setBook( book );

        context.setOutputDirectory( outputDirectory );

        // -----------------------------------------------------------------------
        //
        // -----------------------------------------------------------------------

        bookIo.loadFiles( context, files );

        // ----------------------------------------------------------------------
        // Generate indexes
        // ----------------------------------------------------------------------

        bookIndexer.indexBook( book, context );

        // ----------------------------------------------------------------------
        // Render the book
        // ----------------------------------------------------------------------

        System.out.println( "Book-renderers available: " + bookRenderers.keySet() );
        
        BookRenderer bookRenderer = (BookRenderer) bookRenderers.get( bookRendererId );

        if ( bookRenderer == null )
        {
            throw new BookDoxiaException( "No such book renderer '" + bookRendererId + "'." );
        }

        bookRenderer.renderBook( context );
    }
}
