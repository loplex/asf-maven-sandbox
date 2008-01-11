package org.apache.maven.doxia.module.xwiki.parser;

import org.apache.maven.doxia.module.confluence.parser.AbstractFatherBlock;
import org.apache.maven.doxia.sink.Sink;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class MacroBlock
    extends AbstractFatherBlock
{
    private String name;

    private Map parameters;

    private String content;
    
    public MacroBlock( String name, Map parameters, String content, List childBlocks )
    {
        super( childBlocks );
        this.name = name;
        this.parameters = parameters;
        this.content = content;
    }

    public void before( Sink sink )
    {

    }

    public void after( Sink sink )
    {

    }

    public String getName()
    {
        return this.name;
    }

    public Map getParameters()
    {
        Map newParameters = new HashMap(this.parameters);
        return newParameters;
    }

    public String getContent()
    {
        return this.content;
    }
}