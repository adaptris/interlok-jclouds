/*
 * Copyright 2018 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.adaptris.jclouds.blobstore;

import org.jclouds.blobstore.BlobStore;

import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Remove an object.
 * 
 * @config jclouds-blobstore-remove
 */
@XStreamAlias("jclouds-blobstore-remove")
@DisplayOrder(order =
{
    "containerName", "name",
})
public class Remove extends OperationImpl {

  public Remove() {

  }

  public Remove(String container, String name) {
    this();
    setContainerName(container);
    setName(name);
  }

  @Override
  public void execute(BlobStoreConnection conn, AdaptrisMessage msg) throws InterlokException {
    try {
      String container = msg.resolve(getContainerName());
      String name = msg.resolve(getName());
      BlobStore store = conn.getBlobStore(container);
      store.removeBlob(container, name);
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }
}
