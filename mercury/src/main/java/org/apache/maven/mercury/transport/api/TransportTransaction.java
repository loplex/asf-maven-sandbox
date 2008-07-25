package org.apache.maven.mercury.transport.api;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Repository access transaction. Consists of a collection of bindings
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public class TransportTransaction
{
  public static final int DEFAULT_SIZE = 32;
  
  protected List<Binding> _bindings;
  
  //------------------------------------------------------------------------------------------------
  private void init()
  {
    init( DEFAULT_SIZE );
  }
  //------------------------------------------------------------------------------------------------
  private void init( int n )
  {
    if( _bindings == null )
      _bindings = new ArrayList<Binding>( n );
  }
  //------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public TransportTransaction()
  {
    init();
  }
  //------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public TransportTransaction add( Binding binding )
  {
    init();
    
    _bindings.add( binding );
    
    return this;
  }
  //------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public TransportTransaction add( URI remoteResource, URI localResource, boolean lenientChecksum )
  {
    init();
    
    _bindings.add( new Binding( remoteResource, localResource, lenientChecksum ) );
    
    return this;
  }
  //------------------------------------------------------------------------------------------------
  /**
   * 
   */
  public TransportTransaction add( URI remoteResource, URI localResource )
  {
    init();
    
    _bindings.add( new Binding( remoteResource, localResource, true ) );
    
    return this;
  }
  //------------------------------------------------------------------------------------------------
  @SuppressWarnings("unchecked")
  public List<Binding> getBindings()
  {
    return _bindings == null ? (List<Binding>)Collections.EMPTY_LIST : _bindings;
  }

  public void setBindings( List<Binding> bindings )
  {
    this._bindings = bindings;
  }
  //------------------------------------------------------------------------------------------------
  public void clearErrors()
  {
    if( _bindings == null )
      return;
    
    for( Binding b : _bindings )
      b.setError( null );
  }
  //------------------------------------------------------------------------------------------------
  //------------------------------------------------------------------------------------------------
}