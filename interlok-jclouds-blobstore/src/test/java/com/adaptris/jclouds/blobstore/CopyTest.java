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

import static org.junit.Assert.assertTrue;
import org.jclouds.blobstore.BlobStore;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class CopyTest extends OperationCase {


  @Test
  public void testCopy() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    String destName = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service =
        new BlobStoreService(con,
            new Copy().withDestinationName(destName).withDestinationContainerName(container)
                .withName(name).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, name, "hello world");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      BlobStore store = con.getBlobStoreContext().getBlobStore();
      assertTrue(store.blobExists(container, destName));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

}
