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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.EnumSet;

import org.apache.commons.io.FileUtils;
import org.jclouds.blobstore.BlobStore;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.lms.FileBackedMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory;
import com.adaptris.core.stubs.DefectiveMessageFactory.WhenToBreak;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;

public class DownloadTest extends OperationCase {

  @Test
  public void testDownload() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service = new BlobStoreService(con, new Download().withName(name).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, name, "hello world");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      assertEquals("hello world", msg.getContent());
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  // If you inspect the BlobStore implementation for `filesystem` then it when you use BlobStore.downloadBlob()
  // So this is a mock-alike test that ensures that it doesn't fail.
  @Test
  public void testDownload_DownloadBlob() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service = new BlobStoreService(con,
        new OverrideDownloadBlob("hello world").withName(name).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, name, "hello world");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      assertEquals("hello world", msg.getContent());
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testDownload_TempDir() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service = new BlobStoreService(con,
        new Download().withTempDirectory(FileUtils.getTempDirectoryPath()).withName(name).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, name, "hello world");
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      assertEquals("hello world", msg.getContent());
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testWriteFile() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    File file = TempFileUtils.createTrackedFile(msg);
    FileUtils.write(file, "Hello World", Charset.defaultCharset());
    new Download().write(file, msg);
    assertEquals("Hello World", msg.getContent());

    AdaptrisMessage fileBacked = new FileBackedMessageFactory().newMessage();
    File file2 = TempFileUtils.createTrackedFile(fileBacked);
    FileUtils.write(file2, "Goodbye Cruel World", Charset.defaultCharset());
    new Download().write(file2, fileBacked);
    assertEquals("Goodbye Cruel World", fileBacked.getContent());

  }

  @Test
  public void testDownload_Error() throws Exception {
    String name = guid.safeUUID();
    String container = guid.safeUUID();
    BlobStoreConnection con = createConnection();
    BlobStoreService service = new BlobStoreService(con, new Download().withName(name).withContainerName(container));
    try {
      LifecycleHelper.initAndStart(service);
      createBlob(con.getBlobStoreContext(), container, name, "hello world");
      AdaptrisMessage msg = new DefectiveMessageFactory(EnumSet.of(WhenToBreak.OUTPUT, WhenToBreak.INPUT)).newMessage("");
      assertThrows(ServiceException.class, () -> service.doService(msg));
    } finally {
      LifecycleHelper.stopAndClose(service);
    }
  }

  @Test
  public void testAttemptDownloadBlob() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    File file = TempFileUtils.createTrackedFile(msg);
    FileUtils.write(file, "Hello World", Charset.defaultCharset());
    OverrideTempFile download = new OverrideTempFile(file);

    BlobStore blobstore = Mockito.mock(BlobStore.class);
    assertTrue(download.tryDownloadBlob(blobstore, "container", "name", msg));
    assertEquals("Hello World", msg.getContent());
  }

  @Test
  public void testAttemptDownloadBlob_Unsupported() throws Exception {
    AdaptrisMessage msg = new DefaultMessageFactory().newMessage();
    File file = TempFileUtils.createTrackedFile(msg);
    FileUtils.write(file, "Hello World", Charset.defaultCharset());
    OverrideTempFile download = new OverrideTempFile(file);

    BlobStore blobstore = Mockito.mock(BlobStore.class);
    Mockito.doThrow(new UnsupportedOperationException()).when(blobstore).downloadBlob(any(), any(), (File) any());
    assertFalse(download.tryDownloadBlob(blobstore, "container", "name", msg));
  }

  // Just for coverage, and it makes me a little sad.
  private class OverrideTempFile extends Download {

    private File tempFile;

    public OverrideTempFile(File f) {
      tempFile = f;
    }

    @Override
    protected File tempFile() {
      return tempFile;
    }
  }

  // Just for coverage, and it makes me a little sad.
  private class OverrideDownloadBlob extends Download {
    private String payload;

    public OverrideDownloadBlob(String payload) {
      this.payload = payload;
    }

    @Override
    protected boolean tryDownloadBlob(BlobStore store, String container, String name, AdaptrisMessage msg)
        throws InterlokException, IOException {
      msg.setContent(payload, Charset.defaultCharset().name());
      return true;
    }
  }

}
