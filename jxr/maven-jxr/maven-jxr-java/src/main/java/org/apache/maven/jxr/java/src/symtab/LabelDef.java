package org.apache.maven.jxr.java.src.symtab;

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

/**
 * A label that appears in the source file.
 *
 * @version $Id$
 */
class LabelDef
    extends Definition
{

    private static final long serialVersionUID = -2054703202457275137L;

    // ==========================================================================
    // ==  Methods
    // ==========================================================================

    /**
     * Constructor to create a new label symbol
     *
     * @param name
     * @param occ
     * @param parentScope
     */
    LabelDef( String name, // name of the label
              Occurrence occ, // where it was defined
              ScopedDef parentScope )
    { // scope containing the def
        super( name, occ, parentScope );
    }

    /**
     * @see org.apache.maven.jxr.java.src.symtab.Definition#generateTags(org.apache.maven.jxr.java.src.symtab.HTMLTagContainer)
     */
    public void generateTags( HTMLTagContainer tagList )
    {

        // state that this is a label
        // System.out.println(getQualifiedName() + " (Label) " + getDef());
        // list all references to this label
        // listReferences(System.out);
    }

    /**
     * @see org.apache.maven.jxr.java.src.symtab.Definition#getOccurrenceTag(org.apache.maven.jxr.java.src.symtab.Occurrence)
     */
    public HTMLTag getOccurrenceTag( Occurrence occ )
    {
        return null;
    }
}
