// ========================================================================
// Copyright 2008 Sonatype Inc.
// ------------------------------------------------------------------------
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
// http://www.apache.org/licenses/LICENSE-2.0
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// ========================================================================
package org.apache.maven.mercury.client;

import org.mortbay.io.Buffer;
import org.mortbay.jetty.client.HttpClient;
import org.mortbay.jetty.client.HttpExchange;

import java.io.File;

/**
 * FileExchange
 * <p/>
 * Base class for asynchronously PUTting or GETting a file.
 */
public abstract class FileExchange extends HttpExchange
{
    public static final String __BATCH_HEADER = "Jetty-Batch-Id";
    public static final String __BATCH_SUPPORTED_HEADER = "Jetty-Batch-Supported";
    public static final String __BATCH_COMMIT_HEADER = "Jetty-Batch-Commit";
    public static final String __BATCH_DISCARD_HEADER = "Jetty-Batch-Discard";
    public static final String _digestAlgorithm = "SHA-1";

    protected HttpClient _httpClient;
    protected boolean _digestRequired;
    protected int _status;
    protected String _url;
    protected File _localFile;
    protected Binding _binding;

    public abstract void onFileComplete( String url, File localFile, String digest );

    public abstract void onFileError( String url, Exception e );


    public FileExchange( Binding binding, File localFile, boolean digestRequired, HttpClient client )
    {
        _binding = binding;
        _url = binding.getRemoteUrl();
        _localFile = localFile;
        _httpClient = client;
        _digestRequired = digestRequired;
        setURL( _url );
    }

    public void send()
    {
        try
        {
            _httpClient.send( this );
        }
        catch ( Exception e )
        {
            onFileError( _url, e );
        }
    }

    protected void onResponseStatus( Buffer version, int status, Buffer reason )
    {
        _status = status;
    }

    protected void onException( Throwable ex )
    {
        onFileError( _url, new Exception( ex ) );
    }

    protected void onExpire()
    {
        onFileError( _url, new Exception( "Timeout occurred" ) );
    }
}
