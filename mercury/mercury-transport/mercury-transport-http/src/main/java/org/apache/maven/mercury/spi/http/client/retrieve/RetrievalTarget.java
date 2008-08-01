/**
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

package org.apache.maven.mercury.spi.http.client.retrieve;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import org.apache.maven.mercury.spi.http.client.FileExchange;
import org.apache.maven.mercury.spi.http.client.HttpClientException;
import org.apache.maven.mercury.spi.http.validate.Validator;
import org.apache.maven.mercury.transport.api.Binding;
import org.apache.maven.mercury.transport.api.StreamObserver;
import org.apache.maven.mercury.transport.api.Verifier;
import org.mortbay.jetty.client.HttpExchange;


/**
 * RetrievalTarget
 * <p/>
 * A RetrievalTarget is a remote file that must be downloaded locally, checksummed
 * and then atomically moved to its final location. The RetrievalTarget encapsulates
 * the temporary local file to which the remote file is downloaded, and also the
 * retrieval of the checksum file(s) and the checksum calculation(s).
 */
public abstract class RetrievalTarget
{
    public static final String __PREFIX = "JTY_";
    public static final String __TEMP_SUFFIX = ".tmp";
    public static final int __START_STATE = 1;
    public static final int __REQUESTED_STATE = 2;
    public static final int __READY_STATE = 3;

    protected int _checksumState;
    protected int _targetState;
    
    protected HttpClientException _exception;
    protected Binding _binding;
    protected File _tempFile;
    protected DefaultRetriever _retriever;
    protected boolean _complete;
    protected HttpExchange _exchange;
    protected Set<Validator> _validators;
    protected Set<StreamObserver> _observers = new HashSet<StreamObserver>();
    protected List<Verifier> _verifiers = new ArrayList<Verifier>();
    protected Map<Verifier, String> _verifierMap = new HashMap<Verifier, String>();
 
    
    public abstract void onComplete();

    public abstract void onError( HttpClientException exception );

    /**
     * Constructor
     *
     * @param binding
     * @param callback
     */
    public RetrievalTarget( DefaultRetriever retriever, Binding binding, Set<Validator> validators, Set<StreamObserver> observers )
    {
        if ( binding == null || 
                (binding.getRemoteResource() == null) || 
                (binding.isFile() && (binding.getLocalFile() == null)) ||
                (binding.isInMemory() && (binding.getLocalOutputStream() == null)))
        {
            throw new IllegalArgumentException( "Nothing to retrieve" );
        }
        _retriever = retriever;
        _binding = binding;
        _validators = validators;
       
        //sift out the potential checksum verifiers
        for (StreamObserver o: observers)
        {
            if (Verifier.class.isAssignableFrom(o.getClass()))
                _verifiers.add((Verifier)o);
            else
                _observers.add(o);
        }
        
        if (_binding.isFile())
        {
            _tempFile = new File( _binding.getLocalFile().getParentFile(),
                                  __PREFIX + _binding.getLocalFile().getName() + __TEMP_SUFFIX );        
            _tempFile.deleteOnExit();
            if ( !_tempFile.getParentFile().exists() )
            {
                _tempFile.getParentFile().mkdirs();
            }

            if ( _tempFile.exists() )
            {
                onError( new HttpClientException( binding, "File exists " + _tempFile.getAbsolutePath() ) );
            }
            else if ( !_tempFile.getParentFile().canWrite() )
            {
                onError( new HttpClientException( binding,
                        "Unable to write to dir " + _tempFile.getParentFile().getAbsolutePath() ) );
            }
        }
    }

   

    public File getTempFile()
    {
        return _tempFile;
    }

    public String getUrl()
    {
        return _binding.getRemoteResource().toExternalForm();
    }


    /** Start by getting the appropriate checksums */
    public void retrieve()
    {
        //if there are no checksum verifiers configured, proceed directly to get the file
        if (_verifiers.size() == 0)
        {
            _checksumState = __READY_STATE;
            updateTargetState(__START_STATE, null);
        }
        else
        {
            _checksumState = __START_STATE;
            updateChecksumState(-1, null);
        }
    }


    /** Move the temporary file to its final location */
    public boolean move()
    {
        if (_binding.isFile())
            return _tempFile.renameTo( _binding.getLocalFile() );
        else
            return true;
    }

    /** Cleanup temp files */
    public synchronized void cleanup()
    {
        deleteTempFile();
        if ( _exchange != null )
        {
            _exchange.cancel();
        }
    }

    public synchronized boolean isComplete()
    {
        return _complete;
    }

    public String toString()
    {
        return "T:" + _binding.getRemoteResource() + ":" + _targetState + ":" + _checksumState + ":" + _complete;
    }

    private void updateChecksumState (int index, Throwable ex)
    {
        if ( _exception == null && ex != null )
        {
            if ( ex instanceof HttpClientException )
            {
                _exception = (HttpClientException) ex;
            }
            else
            {
                _exception = new HttpClientException( _binding, ex );
            }
        }
        
        if (ex != null)
        {
            _checksumState = __READY_STATE;
            onError(_exception);
        }
        else
        {     
            boolean proceedWithTargetFile = false;
            if (index >= 0)
            {
                //check if the just-completed retrieval means that we can stop trying to download checksums 
                Verifier v = _verifiers.get(index);
                if (_verifierMap.containsKey(v) && v.isSufficient())
                    proceedWithTargetFile = true;
            }

            index++;
            
            if ((index < _verifiers.size()) && !proceedWithTargetFile)
            {
                retrieveChecksum(index);
            }
            else
            {
                _checksumState = __READY_STATE;

                //finished retrieving all possible checksums. Add all verifiers
                //that had matching checksums into the observers list
                _observers.addAll(_verifierMap.keySet());

                //now get the file now we have the checksum sorted out
                updateTargetState( __START_STATE, null );
            }
        }
    }
    
    
   
   

    /**
     * Check the actual checksum against the expected checksum
     *
     * @return
     */
    public boolean verifyChecksum()
    {
        boolean ok = true;
        
        synchronized (_verifierMap)
        {
            Iterator<Map.Entry<Verifier, String>> itor = _verifierMap.entrySet().iterator();
            while (itor.hasNext() && ok)
            {               
                Map.Entry<Verifier, String> e = itor.next();
                ok = e.getKey().verifySignature(e.getValue());
            }
        }
        
        return ok;
    }
        
    

    public boolean validate( List<String> errors )
    {
        if ( _validators == null || _validators.isEmpty() )
        {
            return true;
        }

        String ext =  _binding.getRemoteResource().toString();
        if (ext.endsWith("/"))
            ext = ext.substring(0, ext.length()-1);
        
        int i = ext.lastIndexOf( "." );
        ext = ( i > 0 ? ext.substring( i + 1 ) : "" );

        for ( Validator v : _validators )
        {
            String vExt = v.getFileExtension();
            if ( vExt.equalsIgnoreCase( ext ) )
            {
                try
                {
                    if (_binding.isFile())
                    {
                        if ( !v.validate( _tempFile.getCanonicalPath(), errors ) )
                        {
                            return false;
                        }
                    }
                    else if (_binding.isInMemory())
                    {
                        //TODO ????
                        //v.validate(_binding.getInboundContent()) 
                    }
                }
                catch ( IOException e )
                {
                    errors.add( e.getMessage() );
                    return false;
                }
            }
        }
        return true;
    }

  

    protected synchronized void updateTargetState( int state, Throwable ex )
    {
        _targetState = state;
        if ( _exception == null && ex != null )
        {
            if ( ex instanceof HttpClientException )
            {
                _exception = (HttpClientException) ex;
            }
            else
            {
                _exception = new HttpClientException( _binding, ex );
            }
        }

        if ( _targetState == __START_STATE )
        {
            _exchange = retrieveTargetFile();
        }

        //if both checksum and target file are ready, we're ready to return callback
        if (_targetState == __READY_STATE )
        {
            _complete = true;
            if ( _exception == null )
            {
                onComplete();
            }
            else
            {
                onError( _exception );
            }
        }
    }

    /** Asynchronously fetch the checksum for the target file. */
    private HttpExchange retrieveChecksum(final int index)
    {
        HttpExchange exchange = new HttpExchange.ContentExchange()
        {
            protected void onException( Throwable ex )
            {
                //if the checksum is mandatory, then propagate the exception and stop processing
                if (!_verifiers.get(index).isLenient())
                {
                    updateChecksumState(index, ex);
                }
                else
                    updateChecksumState(index, null);
                
            }

            protected void onResponseComplete() throws IOException
            {
                super.onResponseComplete();
                Verifier v = _verifiers.get(index);
                
                if ( getResponseStatus() == HttpServletResponse.SC_OK )
                {
                    //We got a checksum so match it up with the verifier it is for
                    synchronized (_verifierMap)
                    {
                        if (v.isSufficient())
                            _verifierMap.clear(); //remove all other entries, we only need one checksum
                        _verifierMap.put(v, getResponseContent().trim());
                    }
                    updateChecksumState(index, null);
                }
                else 
                {
                    if (!v.isLenient()) 
                    {
                        //checksum file MUST be present, fail
                        updateChecksumState(index, new Exception ("Mandatory checksum file not found "+this.getURI()));
                    }
                    else
                        updateChecksumState(index, null);
                }
            }
        };
        
        exchange.setURL( getChecksumFileURLAsString( _verifiers.get(index)) );

        try
        {
            _retriever.getHttpClient().send( exchange );
        }
        catch ( IOException ex )
        {
            updateChecksumState(index, ex);
        }
        return exchange;
    }


    /** Asynchronously fetch the target file. */
    private HttpExchange retrieveTargetFile()
    {
        updateTargetState( __REQUESTED_STATE, null );

        //get the file, calculating the digest for it on the fly
        FileExchange exchange = new FileGetExchange( _binding, getTempFile(), _observers, _retriever.getHttpClient() )
        {
            public void onFileComplete( String url, File localFile )
            {
                //we got the target file ok, so tell our main callback
                _targetState = __READY_STATE;
                updateTargetState( __READY_STATE, null );
            }

            public void onFileError( String url, Exception e )
            {
                //an error occurred whilst fetching the file, return an error
                _targetState = __READY_STATE;
                updateTargetState( __READY_STATE, e );
            }
        };
        exchange.send();
        return exchange;
    }

    private String getChecksumFileURLAsString (Verifier verifier)
    {
        String extension = verifier.getExtension();
        if (extension.charAt(0) != '.')
            extension = "."+extension;
        return _binding.getRemoteResource().toString() + extension;
    }

    private boolean deleteTempFile()
    {
        if ( _tempFile != null && _tempFile.exists() )
        {
            return _tempFile.delete();
        }
        return false;
    }
    
}