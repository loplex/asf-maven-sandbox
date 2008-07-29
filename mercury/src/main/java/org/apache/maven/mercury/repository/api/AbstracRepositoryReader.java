package org.apache.maven.mercury.repository.api;

import org.apache.maven.mercury.ArtifactBasicMetadata;


/**
 * This is to keep MetadataProcessor for all readers
 *
 * @author Oleg Gusakov
 * @version $Id$
 *
 */
public abstract class AbstracRepositoryReader
implements RepositoryReader, MetadataReader
{
  protected MetadataProcessor _mdProcessor;
  
  public void setMetadataProcessor( MetadataProcessor mdProcessor )
  {
    _mdProcessor = mdProcessor;
  }
  
  public MetadataProcessor getMetadataProcessor()
  {
    return _mdProcessor;
  }
  
  public byte[] readMetadata( ArtifactBasicMetadata bmd  )
  throws MetadataProcessingException
  {
    return readRawData( bmd, "", "pom" );
  }
  
}
