package org.apache.maven.doxia.ide.eclipse.twiki.ui.editor;

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

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.text.html.HTML.Tag;

import org.apache.maven.doxia.ide.eclipse.common.ui.ColorManager;
import org.apache.maven.doxia.ide.eclipse.common.ui.CommonPlugin;
import org.apache.maven.doxia.ide.eclipse.common.ui.actions.AbstractBoldAction;
import org.apache.maven.doxia.ide.eclipse.common.ui.actions.AbstractItalicAction;
import org.apache.maven.doxia.ide.eclipse.common.ui.actions.AbstractLinkAction;
import org.apache.maven.doxia.ide.eclipse.common.ui.actions.AbstractMonospacedAction;
import org.apache.maven.doxia.ide.eclipse.common.ui.actions.AbstractTableAction;
import org.apache.maven.doxia.ide.eclipse.common.ui.actions.IActionConstants;
import org.apache.maven.doxia.ide.eclipse.common.ui.contentassist.AbstractContentAssistProcessor;
import org.apache.maven.doxia.ide.eclipse.common.ui.dialogs.AddLinkDialog.Link;
import org.apache.maven.doxia.ide.eclipse.common.ui.dialogs.AddTableDialog.Table;
import org.apache.maven.doxia.ide.eclipse.common.ui.editors.AbstractTextMultiPageEditorPart;
import org.apache.maven.doxia.ide.eclipse.common.ui.editors.text.AbstractTextEditor;
import org.apache.maven.doxia.ide.eclipse.common.ui.editors.text.AbstractTextSourceViewerConfiguration;
import org.apache.maven.doxia.ide.eclipse.common.ui.rules.AbstractTextScanner;
import org.apache.maven.doxia.ide.eclipse.twiki.ui.TWikiPlugin;
import org.apache.maven.doxia.ide.eclipse.twiki.ui.editor.TWikiDocumentProvider.TWikiPartitionScanner;
import org.apache.maven.doxia.markup.Markup;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlink;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.hyperlink.URLHyperlink;
import org.eclipse.jface.text.hyperlink.URLHyperlinkDetector;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.texteditor.IDocumentProvider;

/**
 * TWiki editor.
 *
 * @author <a href="mailto:vincent.siveton@gmail.com">Vincent Siveton</a>
 * @version $Id$
 * @since 1.0
 * @see <a href="http://twiki.org/cgi-bin/view/TWiki04x01/TextFormattingRules">
 *      http://twiki.org/cgi-bin/view/TWiki04x01/TextFormattingRules</a>
 */
public class TWikiEditor
    extends AbstractTextMultiPageEditorPart
{
    private TextEditor editor;

    @Override
    public TextEditor getTextEditor()
    {
        if ( editor != null )
        {
            return editor;
        }

        editor = new TWikiTextEditor();
        editor.setAction( IActionConstants.BOLD_ACTION, new AbstractBoldAction( editor )
        {
            @Override
            public String getStartMarkup()
            {
                return "*";
            }

            @Override
            public String getEndMarkup()
            {
                return "*";
            }
        } );
        editor.setAction( IActionConstants.ITALIC_ACTION, new AbstractItalicAction( editor )
        {
            @Override
            public String getStartMarkup()
            {
                return "_";
            }

            @Override
            public String getEndMarkup()
            {
                return "_";
            }
        } );
        editor.setAction( IActionConstants.MONOSPACED_ACTION, new AbstractMonospacedAction( editor )
        {
            @Override
            public String getStartMarkup()
            {
                return "=";
            }

            @Override
            public String getEndMarkup()
            {
                return "=";
            }
        } );
        editor.setAction( IActionConstants.LINK_ACTION, new AbstractLinkAction( editor )
        {
            @Override
            protected String generateLink( Link link )
            {
                if ( StringUtils.isEmpty( link.getName() ) )
                {
                    return "[[" + link.getURL() + "]]";
                }

                return "[[" + link.getURL() + "][" + link.getName() + "]]";
            }
        } );
        editor.setAction( IActionConstants.TABLE_ACTION, new AbstractTableAction( editor )
        {
            @Override
            protected String generateTable( Table table )
            {
                StringBuffer sb = new StringBuffer();

                for ( int i = 0; i < table.getRows(); i++ )
                {
                    sb.append( "|" );

                    for ( int j = 0; j < table.getColumns(); j++ )
                    {
                        sb.append( DEFAULT_CELL_TEXT );
                        sb.append( "|" );
                    }
                    sb.append( Markup.EOL );
                }

                return sb.toString();
            }
        } );

        return editor;
    }

    @Override
    public String getFormat()
    {
        return TWikiPlugin.getDoxiaFormat();
    }

    class TWikiTextEditor
        extends AbstractTextEditor
    {
        @Override
        public String getEditorId()
        {
            return "org.apache.maven.doxia.ide.eclipse.twiki.ui.editor.TWikiEditor";
        }

        @Override
        public IDocumentProvider getFileDocumentProvider()
        {
            return new TWikiDocumentProvider();
        }

        @Override
        public TextSourceViewerConfiguration getTextSourceViewerConfiguration()
        {
            return new TWikiSourceViewerConfiguration();
        }
    }

    class TWikiSourceViewerConfiguration
        extends AbstractTextSourceViewerConfiguration
    {
        /**
         * Default constructor.
         */
        public TWikiSourceViewerConfiguration()
        {
            super();
        }

        @Override
        protected RuleBasedScanner getScanner()
        {
            return new TWikiScanner();
        }

        @Override
        public IHyperlinkDetector[] getHyperlinkDetectors( ISourceViewer sourceViewer )
        {
            return new IHyperlinkDetector[] { new AptURLHyperlinkDetector() };
        }

        @Override
        public IContentAssistant getContentAssistant( ISourceViewer sourceViewer )
        {
            ContentAssistant assistant = new ContentAssistant();
            assistant.setDocumentPartitioning( getConfiguredDocumentPartitioning( sourceViewer ) );

            IContentAssistProcessor processor = new TWikiContentAssistProcessor();
            assistant.setContentAssistProcessor( processor, IDocument.DEFAULT_CONTENT_TYPE );
            assistant.setContentAssistProcessor( processor, TWikiPartitionScanner.DOXIA_PARTITION_CONTENT );

            assistant.setContextInformationPopupOrientation( IContentAssistant.CONTEXT_INFO_ABOVE );
            assistant.setInformationControlCreator( getInformationControlCreator( sourceViewer ) );

            return assistant;
        }

        /**
         * Based on <code>URLHyperlinkDetector</code> with a small fix in the <code>tokenizer</code> implementation
         * to handle as well <code>[</code> and <code>]</code>.
         *
         * @see org.eclipse.jface.text.hyperlink.URLHyperlinkDetector
         */
        class AptURLHyperlinkDetector
            extends URLHyperlinkDetector
        {
            /**
             * Default constructor.
             */
            public AptURLHyperlinkDetector()
            {
                super();
            }

            @Override
            public IHyperlink[] detectHyperlinks( ITextViewer textViewer, IRegion region,
                                                  boolean canShowMultipleHyperlinks )
            {
                if ( region == null || textViewer == null )
                    return null;

                IDocument document = textViewer.getDocument();

                int offset = region.getOffset();

                String urlString = null;
                if ( document == null )
                    return null;

                IRegion lineInfo;
                String line;
                try
                {
                    lineInfo = document.getLineInformationOfOffset( offset );
                    line = document.get( lineInfo.getOffset(), lineInfo.getLength() );
                }
                catch ( BadLocationException ex )
                {
                    return null;
                }

                int offsetInLine = offset - lineInfo.getOffset();

                boolean startDoubleQuote = false;
                int urlOffsetInLine = 0;
                int urlLength = 0;

                int urlSeparatorOffset = line.indexOf( "://" ); //$NON-NLS-1$
                while ( urlSeparatorOffset >= 0 )
                {

                    // URL protocol (left to "://")
                    urlOffsetInLine = urlSeparatorOffset;
                    char ch;
                    do
                    {
                        urlOffsetInLine--;
                        ch = ' ';
                        if ( urlOffsetInLine > -1 )
                            ch = line.charAt( urlOffsetInLine );
                        startDoubleQuote = ch == '"';
                    }
                    while ( Character.isUnicodeIdentifierStart( ch ) );
                    urlOffsetInLine++;

                    // Right to "://"
                    // APT FIX
                    StringTokenizer tokenizer = new StringTokenizer( line.substring( urlSeparatorOffset + 3 ),
                                                                     " \t\n\r\f<>[]", false ); //$NON-NLS-1$
                    if ( !tokenizer.hasMoreTokens() )
                        return null;

                    urlLength = tokenizer.nextToken().length() + 3 + urlSeparatorOffset - urlOffsetInLine;
                    if ( offsetInLine >= urlOffsetInLine && offsetInLine <= urlOffsetInLine + urlLength )
                        break;

                    urlSeparatorOffset = line.indexOf( "://", urlSeparatorOffset + 1 ); //$NON-NLS-1$
                }

                if ( urlSeparatorOffset < 0 )
                    return null;

                if ( startDoubleQuote )
                {
                    int endOffset = -1;
                    int nextDoubleQuote = line.indexOf( '"', urlOffsetInLine );
                    int nextWhitespace = line.indexOf( ' ', urlOffsetInLine );
                    if ( nextDoubleQuote != -1 && nextWhitespace != -1 )
                        endOffset = Math.min( nextDoubleQuote, nextWhitespace );
                    else if ( nextDoubleQuote != -1 )
                        endOffset = nextDoubleQuote;
                    else if ( nextWhitespace != -1 )
                        endOffset = nextWhitespace;
                    if ( endOffset != -1 )
                        urlLength = endOffset - urlOffsetInLine;
                }

                // Set and validate URL string
                try
                {
                    urlString = line.substring( urlOffsetInLine, urlOffsetInLine + urlLength );
                    new URL( urlString );
                }
                catch ( MalformedURLException ex )
                {
                    urlString = null;
                    return null;
                }

                IRegion urlRegion = new Region( lineInfo.getOffset() + urlOffsetInLine, urlLength );
                return new IHyperlink[] { new URLHyperlink( urlRegion, urlString ) };
            }
        }
    }

    class TWikiScanner
        extends AbstractTextScanner
    {
        /**
         * Default constructor.
         */
        public TWikiScanner()
        {
            super();
        }

        @Override
        public List<IRule> getRules()
        {
            List<IRule> rules = new LinkedList<IRule>();

            // horizontal rule
            rules.add( new EndOfLineRule( "-------", AbstractTextScanner.SinkToken.HORIZONTALRULE_TOKEN ) );

            // sections title rule
            rules.add( new EndOfLineRule( "---+++++", SinkToken.SECTIONTITLE5_TOKEN ) );
            rules.add( new EndOfLineRule( "---++++", SinkToken.SECTIONTITLE4_TOKEN ) );
            rules.add( new EndOfLineRule( "---+++", SinkToken.SECTIONTITLE3_TOKEN ) );
            rules.add( new EndOfLineRule( "---++", SinkToken.SECTIONTITLE2_TOKEN ) );
            rules.add( new EndOfLineRule( "---+", SinkToken.SECTIONTITLE1_TOKEN ) );

            // verbatim rules
            rules.add( new MultiLineRule( "<verbatim>", "</verbatim>", SinkToken.VERBATIM_TOKEN ) );

            // bold rule or list
            rules.add( new SingleLineRule( "*", " ", SinkToken.BOLD_TOKEN ) );

            // bold + italic rule
            rules.add( new SingleLineRule( "__", "__",
                                           new Token( new TextAttribute( ColorManager.SinkColor.ITALIC_COLOR, null,
                                                                         SWT.ITALIC + SWT.BOLD ) ) ) );

            // italic rule
            rules.add( new SingleLineRule( "_", "_", SinkToken.ITALIC_TOKEN ) );

            // bold + fixed rule
            rules.add( new SingleLineRule( "==", "==",
                                           new Token( new TextAttribute( ColorManager.SinkColor.MONOSPACED_COLOR, null,
                                                                         SWT.NORMAL, new Font( Display.getDefault(),
                                                                                               "Courier", Display
                                                                                                   .getDefault()
                                                                                                   .getSystemFont()
                                                                                                   .getFontData()[0]
                                                                                                   .getHeight(),
                                                                                               SWT.BOLD ) ) ) ) );

            // monospaced rule
            rules.add( new SingleLineRule( "=", "=", SinkToken.MONOSPACED_TOKEN ) );

            // numbered list rule
            // Multiple of three spaces, a type character, a dot, and another space.
            rules.add( new SingleLineRule( "   1", ". ", SinkToken.NUMBEREDLISTITEM_TOKEN ) );
            // lower case
            rules.add( new SingleLineRule( "   a", ". ", SinkToken.NUMBEREDLISTITEM_TOKEN ) );
            // upper case
            rules.add( new SingleLineRule( "   A", ". ", SinkToken.NUMBEREDLISTITEM_TOKEN ) );
            // roman lower case
            rules.add( new SingleLineRule( "   i", ". ", SinkToken.NUMBEREDLISTITEM_TOKEN ) );
            // roman upper case
            rules.add( new SingleLineRule( "   I", ". ", SinkToken.NUMBEREDLISTITEM_TOKEN ) );

            // definition list rule
            // Three spaces, a dollar sign, the term, a colon, a space, followed by the definition.
            rules.add( new SingleLineRule( "   $", " ", SinkToken.DEFINITIONLIST_TOKEN ) );

            // table rule
            rules.add( new EndOfLineRule( "|", SinkToken.TABLE_TOKEN ) );

            // link rule
            rules.add( new SingleLineRule( "![[", "]]", DEFAULT_TOKEN ) );
            rules.add( new SingleLineRule( "[[", "]]", SinkToken.LINK_TOKEN ) );
            // inline links
            rules.add( new SingleLineRule( "http://", " ", SinkToken.LINK_TOKEN ) );
            rules.add( new SingleLineRule( "https://", " ", SinkToken.LINK_TOKEN ) );
            rules.add( new SingleLineRule( "ftp://", " ", SinkToken.LINK_TOKEN ) );
            rules.add( new SingleLineRule( "gopher://", " ", SinkToken.LINK_TOKEN ) );
            rules.add( new SingleLineRule( "news://", " ", SinkToken.LINK_TOKEN ) );
            rules.add( new SingleLineRule( "file://", " ", SinkToken.LINK_TOKEN ) );
            rules.add( new SingleLineRule( "telnet://", " ", SinkToken.LINK_TOKEN ) );
            rules.add( new SingleLineRule( "mailto:", " ", SinkToken.LINK_TOKEN ) );

            // anchor rule
            rules.add( new SingleLineRule( "#", " ", SinkToken.ANCHOR_TOKEN ) );

            // TODO better unsupported
            // some other advanced formatting unsupported by Doxia
            rules.add( new MultiLineRule( "<noautolink>", "</noautolink>", UNSUPPORTED_TOKEN ) );
            // Unsupported HTML tags
            try
            {
                Field[] fields = Tag.class.getDeclaredFields();
                for ( int i = 0; i < fields.length; i++ )
                {
                    Field field = fields[i];
                    if ( field.getModifiers() == ( Modifier.PUBLIC + Modifier.STATIC + Modifier.FINAL ) )
                    {
                        String fieldValue = field.get( null ).toString();
                        rules.add( new MultiLineRule( "<" + fieldValue + ">", "</" + fieldValue + ">",
                                                      UNSUPPORTED_TOKEN ) );
                    }
                }
            }
            catch ( Exception e )
            {
                // nop
            }
            // TWiki Variables
            rules.add( new SingleLineRule( "%", "%", UNSUPPORTED_TOKEN ) );

            return rules;
        }
    }

    class TWikiContentAssistProcessor
        extends AbstractContentAssistProcessor
    {
        /**
         * Default constructor.
         */
        public TWikiContentAssistProcessor()
        {
            super();
        }

        @Override
        public Image[] getImageMarkups()
        {
            return new Image[] {
                CommonPlugin.getImage( CommonPlugin.IMG_ITALIC ),
                CommonPlugin.getImage( CommonPlugin.IMG_BOLD ),
                CommonPlugin.getImage( CommonPlugin.IMG_MONOSPACED ),
                CommonPlugin.getImage( CommonPlugin.IMG_LINK ) };
        }

        @Override
        public String[] getStartMarkups()
        {
            return new String[] { "_", "*", "=", "[[" };
        }

        @Override
        public String[] getEndMarkups()
        {
            return new String[] { "_", "*", "=", "]]" };
        }
    }
}
