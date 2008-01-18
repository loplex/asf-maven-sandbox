package org.apache.maven.doxia.module.xwiki.blocks;

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

import org.apache.maven.doxia.sink.Sink;

import java.util.Iterator;
import java.util.List;

public abstract class AbstractFatherBlock
    implements Block
{
    private List blocks;

    public abstract void before( Sink sink );

    public abstract void after( Sink sink );

    public AbstractFatherBlock( List childBlocks )
    {
        if ( childBlocks == null )
        {
            throw new IllegalArgumentException( "argument can't be null" );
        }

        this.blocks = childBlocks;
    }

    public void traverse( Sink sink )
    {
        before( sink );

        for ( Iterator i = blocks.iterator(); i.hasNext(); )
        {
            Block block = (Block) i.next();

            block.traverse( sink );
        }

        after( sink );
    }

    public List getBlocks()
    {
        return blocks;
    }
}
