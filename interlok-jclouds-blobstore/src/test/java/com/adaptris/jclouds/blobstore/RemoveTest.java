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

import static org.junit.Assert.assertFalse;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class RemoveTest extends OperationCase {


  @Before
  public void setUp() throws Exception {

  }

  @After
  public void tearDown() throws Exception {

  }

  @Test
  public void testDownload() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service = new BlobStoreService(con, new Remove(container, name));
    try {
      LifecycleHelper.initAndStart(service);
      BlobStoreContext ctx = con.getBlobStoreContext();
      // Create the container first before running the service.
      BlobStore store = ctx.getBlobStore();
      store.createContainerInLocation(null, container);
      BlobBuilder builder = store.blobBuilder(name);
      builder.payload("hello world");
      store.putBlob(container, builder.build());
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      assertFalse(store.blobExists(container, name));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

}
