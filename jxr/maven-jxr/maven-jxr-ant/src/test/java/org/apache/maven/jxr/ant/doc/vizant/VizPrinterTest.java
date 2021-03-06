package org.apache.maven.jxr.ant.doc.vizant;

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

import org.apache.maven.jxr.ant.doc.vizant.VizAttrStmt;
import org.apache.maven.jxr.ant.doc.vizant.VizPrinter;

import junit.framework.TestCase;

public class VizPrinterTest
    extends TestCase
{
    VizPrinter printer;

    BufferedWriter writer;

    public VizPrinterTest( String name )
    {
        super( name );
    }

    public void setUp()
    {
        printer = new VizPrinter();
        writer = new BufferedWriter();
        printer.setWriter( writer );
    }

    public void testPrintEmpty()
    {
        printer.print();
        assertEquals( "<?xml version=\"1.0\"?>\n" + "<graph name=\"G\">\n" + "\n"
            + "    <attributes type=\"graph\"><attribute name=\"rankdir\" value=\"LR\" /></attributes>\n"
            + "</graph>\n", writer.getString() );
    }

    public void testSetAttributes()
    {
        printer.setGraphid( "build" );

        VizAttrStmt graph = new VizAttrStmt();
        graph.setType( "graph" );
        graph.addAttribute( "label", "test" );
        graph.addAttribute( "label", "test2" );
        printer.addAttributeStatement( graph );

        VizAttrStmt edge = new VizAttrStmt();
        edge.setType( "edge" );
        edge.addAttribute( "a", "3" );
        edge.addAttribute( "b", "2" );
        edge.addAttribute( "c", "1" );
        printer.addAttributeStatement( edge );

        VizAttrStmt node = new VizAttrStmt();
        node.setType("node");
        node.addAttribute("c", "1");
        node.addAttribute("b", "2");
        node.addAttribute("a", "3");
        printer.addAttributeStatement(node);

        printer.print();
        assertEquals(
                      "<?xml version=\"1.0\"?>\n"
                          + "<graph name=\"build\">\n"
                          + "\n"
                          + "    <attributes type=\"graph\"><attribute name=\"rankdir\" value=\"LR\" /><attribute name=\"label\" value=\"test2\" /></attributes>\n"
                          + "    <attributes type=\"node\"><attribute name=\"c\" value=\"1\" /><attribute name=\"b\" value=\"2\" /><attribute name=\"a\" value=\"3\" /></attributes>\n"
                          + "    <attributes type=\"edge\"><attribute name=\"a\" value=\"3\" /><attribute name=\"b\" value=\"2\" /><attribute name=\"c\" value=\"1\" /></attributes>\n"
                          + "</graph>\n", writer.getString() );
    }
}
