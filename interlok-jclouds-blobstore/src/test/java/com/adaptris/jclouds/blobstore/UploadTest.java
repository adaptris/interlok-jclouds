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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class UploadTest extends OperationCase {

  @Test
  public void testSetters() throws Exception {
    Upload upload = new Upload();
    assertNull(upload.getUseMultipart());
    upload.setUseMultipart(true);
    assertEquals(true, upload.getUseMultipart());
    assertNull(upload.getContainerName());
    upload.setContainerName("hello");
    assertEquals("hello", upload.getContainerName());
    assertNull(upload.getName());
    upload.setName("world");
    assertEquals("world", upload.getName());
  }

  @Test
  public void testUpload() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service = new BlobStoreService(con, new Upload().withName(name).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      BlobStoreContext ctx = con.getBlobStoreContext();
      BlobStore store = ctx.getBlobStore();
      // Create the container first before running the service.
      store.createContainerInLocation(null, container);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("hello world");
      service.doService(msg);
      assertNotNull(store.getBlob(container, name));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testUpload_FileBacked() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service = new BlobStoreService(con, new Upload().withName(name).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      BlobStoreContext ctx = con.getBlobStoreContext();
      BlobStore store = ctx.getBlobStore();
      // Create the container first before running the service.
      store.createContainerInLocation(null, container);
      AdaptrisMessage msg = new FileBackedMessageFactory().newMessage("hello world");
      service.doService(msg);
      assertNotNull(store.getBlob(container, name));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testAtLeastTwoParts() throws Exception {
    Upload op = new Upload();
    BlobStore blobstore = Mockito.mock(BlobStore.class);
    Mockito.when(blobstore.getMaximumMultipartPartSize()).thenReturn(1024L);
    Mockito.when(blobstore.getMaximumNumberOfParts()).thenReturn(Integer.MAX_VALUE);
    Mockito.when(blobstore.getMinimumMultipartPartSize()).thenReturn(1L);
    assertTrue(op.atLeastTwoParts(blobstore, 1024 * 1024));
    assertFalse(op.atLeastTwoParts(blobstore, 1023));
  }

  @Test
  public void testBuildPutOptions() throws Exception {
    Upload op = new Upload();
    BlobStore blobstore = Mockito.mock(BlobStore.class);
    Mockito.when(blobstore.getMaximumMultipartPartSize()).thenReturn(1024L);
    Mockito.when(blobstore.getMaximumNumberOfParts()).thenReturn(Integer.MAX_VALUE);
    Mockito.when(blobstore.getMinimumMultipartPartSize()).thenReturn(1L);
    AdaptrisMessage msg = Mockito.mock(AdaptrisMessage.class);
    Mockito.when(msg.getSize()).thenReturn(1024 * 1024L);
    assertNotNull(op.buildPutOptions(blobstore, msg));

    AdaptrisMessage msg2 = Mockito.mock(AdaptrisMessage.class);
    Mockito.when(msg2.getSize()).thenReturn(1023L);
    assertNotNull(op.buildPutOptions(blobstore, msg2));
  }

}
