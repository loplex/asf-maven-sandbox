package org.apache.maven.jxr.java.src.util;

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
 * Class SortableString
 *
 * @version $Id$
 */
public class SortableString
    implements JSComparable
{

    /**
     * Method getString
     *
     * @return
     */
    public String getString()
    {
        return _s;
    }

    /**
     * Constructor SortableString
     *
     * @param s
     */
    public SortableString( String s )
    {
        _s = s;
    }

    /**
     * @see org.apache.maven.jxr.java.src.util.JSComparable#compareTo(java.lang.Object)
     */
    public int compareTo( Object o )
    {
        return _s.compareTo( ( (SortableString) o ).getString() );
    }

    /** Field _s */
    private String _s;
}
