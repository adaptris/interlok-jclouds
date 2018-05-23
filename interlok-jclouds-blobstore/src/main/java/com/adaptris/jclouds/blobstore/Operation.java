package com.adaptris.jclouds.blobstore;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.InterlokException;

public interface Operation {

  void execute(BlobStoreConnection c, AdaptrisMessage msg) throws InterlokException;
  
}
