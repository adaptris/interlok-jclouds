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

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.io.StringReader;
import java.util.AbstractSet;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.commons.io.IOUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.Tier;
import org.jclouds.blobstore.domain.internal.StorageMetadataImpl;
import org.junit.Test;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.util.LifecycleHelper;

public class ListBlobsTest extends OperationCase {


  @Test
  public void testList_NoFilter() throws Exception {
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service =
        new BlobStoreService(con,
            new ListOperation().withPrefix("").withFilterSuffix("")
                .withOutputStyle(null)
                .withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, "jsonfile.json", "hello world");
      createBlob(con.getBlobStoreContext(), container, "file.csv", "hello world");
      createBlob(con.getBlobStoreContext(), container, "README.txt", "hello world");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      List<String> content = IOUtils.readLines(new StringReader(msg.getContent()));
      assertEquals(3, content.size());
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }
  @Test
  public void testList_WithPrefix() throws Exception {
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service =
        new BlobStoreService(con, new ListOperation().withPrefix("prefix/")
            .withOutputStyle(null).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, "jsonfile.json", "hello world");
      createBlob(con.getBlobStoreContext(), container, "file.csv", "hello world");
      createBlob(con.getBlobStoreContext(), container, "README.txt", "hello world");
      createBlob(con.getBlobStoreContext(), container, "prefix/README.txt", "hello world");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      System.err.println(msg.getContent());
      List<String> content = IOUtils.readLines(new StringReader(msg.getContent()));
      assertEquals(1, content.size());
      assertEquals("prefix/README.txt", content.get(0));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testList_Filtering() throws Exception {
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service =
        new BlobStoreService(con, new ListOperation().withPrefix("")
            .withFilterSuffix(".json").withOutputStyle(null).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, "jsonfile.json", "hello world");
      createBlob(con.getBlobStoreContext(), container, "file.csv", "hello world");
      createBlob(con.getBlobStoreContext(), container, "README.txt", "hello world");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      List<String> content = IOUtils.readLines(new StringReader(msg.getContent()));
      assertEquals(1, content.size());
      assertEquals("jsonfile.json", content.get(0));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  // Mocked because life is too short...
  @Test
  public void testList_MultiplePages() throws Exception {
    String container = guid.safeUUID();

    BlobStoreConnection con = mock(BlobStoreConnection.class);
    BlobStore blobstore = mock(BlobStore.class);
    Set<StorageMetadata> wrappedList = createBlobList();
    MyPageSet bloblist = new MyPageSet(createBlobList());
    when(con.retrieveConnection(any())).thenReturn(con);
    when(con.getBlobStore(any())).thenReturn(blobstore);
    doReturn(bloblist).when(blobstore).list(any(), any());

    BlobStoreService service =
        new BlobStoreService(con, new ListOperation().withContainerName(container));

    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      List<String> content = IOUtils.readLines(new StringReader(msg.getContent()));
      // MyPageSet returns 2x iterators, each with 4 items; of which 1 is a folder... so we get 6 in total.
      assertEquals(6, content.size());
    } finally {
      LifecycleHelper.stopAndClose(service);
    }

  }

  private Set<StorageMetadata> createBlobList() {
    HashSet<StorageMetadata> result = new HashSet<>();
    result.add(new StorageMetadataImpl(StorageType.FOLDER, null, "myFolder", null, null, null,
        new Date(), new Date(), Collections.EMPTY_MAP, 0L, Tier.STANDARD));
    result.add(new StorageMetadataImpl(StorageType.BLOB, null, "jsonfile.json", null, null, null,
        new Date(), new Date(), Collections.EMPTY_MAP, 0L, Tier.STANDARD));
    result.add(new StorageMetadataImpl(StorageType.BLOB, null, "file.csv", null, null, null,
        new Date(), new Date(), Collections.EMPTY_MAP, 0L, Tier.STANDARD));
    result.add(new StorageMetadataImpl(StorageType.BLOB, null, "README.txt", null, null, null,
        new Date(), new Date(), Collections.EMPTY_MAP, 0L, Tier.STANDARD));
    return result;
  }

  private class MyPageSet extends AbstractSet<StorageMetadata>
      implements PageSet<StorageMetadata> {
    private Set<StorageMetadata> set;
    private AtomicBoolean isFinished = new AtomicBoolean(false);

    public MyPageSet(Set<StorageMetadata> set) {
      this.set = set;
    }

    @Override
    public String getNextMarker() {
      if (!isFinished.getAndSet(true)) {
        return this.getClass().getCanonicalName();
      }
      return null;
    }

    @Override
    public Iterator<StorageMetadata> iterator() {
      return set.iterator();
    }

    @Override
    public int size() {
      return set.size();
    }
  }
}
