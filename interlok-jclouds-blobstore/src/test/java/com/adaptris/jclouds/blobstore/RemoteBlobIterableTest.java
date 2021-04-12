package com.adaptris.jclouds.blobstore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;

import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.domain.Tier;
import org.jclouds.blobstore.domain.internal.StorageMetadataImpl;
import org.junit.Test;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.cloud.RemoteBlob;

public class RemoteBlobIterableTest extends OperationCase {

  @Test
  public void testIterator() throws Exception {
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    try {
      LifecycleHelper.initAndStart(con);
      createBlobs(con.getBlobStoreContext(), container, 5, ".txt");
      createBlobs(con.getBlobStoreContext(), container, 5, ".json");
      BlobStore store = con.getBlobStore(container);
      RemoteBlobIterable iterable = new RemoteBlobIterable(store, container, "", (f) -> true);
      Iterator<RemoteBlob> i = iterable.iterator();
      int count = 0;
      assertTrue(i.hasNext());
      while (i.hasNext()) {
        i.next();
        count++;
      }
      assertEquals(10, count);
    } finally {
      LifecycleHelper.stopAndClose(con);
    }
  }

  @Test(expected=IllegalStateException.class)
  public void testIterator_Double() throws Exception {
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    try {
      LifecycleHelper.initAndStart(con);
      createBlobs(con.getBlobStoreContext(), container, 5, ".txt");
      createBlobs(con.getBlobStoreContext(), container, 5, ".json");
      BlobStore store = con.getBlobStore(container);
      RemoteBlobIterable iterable = new RemoteBlobIterable(store, container, "", (f) -> true);
      iterable.iterator();
      // double iterator == IllegalStateException
      iterable.iterator();
    } finally {
      LifecycleHelper.stopAndClose(con);
    }
  }

  @Test
  public void testLastModified() {
    Date date = new Date();
    StorageMetadataImpl meta = new StorageMetadataImpl(StorageType.FOLDER, "id", "name", null, null, "eTag", date, date,
        Collections.emptyMap(), null, Tier.STANDARD);

    long lastModified = RemoteBlobIterable.lastModified(meta);

    assertEquals(date.getTime(), lastModified);
  }

  @Test
  public void testLastModifiedNull() {
    StorageMetadataImpl meta = new StorageMetadataImpl(StorageType.FOLDER, "id", "name", null, null, "eTag", new Date(),
        null, Collections.emptyMap(), null, Tier.STANDARD);

    long lastModified = RemoteBlobIterable.lastModified(meta);

    assertEquals(-1L, lastModified);
  }

}
