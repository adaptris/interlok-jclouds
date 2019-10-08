package com.adaptris.jclouds.blobstore;

import com.adaptris.core.AdaptrisMessage;

public interface Operation {

  void execute(BlobStoreConnection c, AdaptrisMessage msg) throws Exception;
  
}
