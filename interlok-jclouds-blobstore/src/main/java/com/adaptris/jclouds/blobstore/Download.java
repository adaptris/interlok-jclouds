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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Download an object.
 * 
 * @config jclouds-blobstore-download
 */
@XStreamAlias("jclouds-blobstore-download")
@DisplayOrder(order = {"containerName", "name", "tempDirectory"})
@ComponentProfile(summary = "Download a blob", tag = "jclouds")
public class Download extends OperationImpl {

  /**
   * Set the temp directory to store files, if not specified defaults to {@code java.io.tmpdir}
   */
  @Getter
  @Setter
  @AdvancedConfig
  private String tempDirectory;

  public Download() {

  }

  @Override
  public void execute(BlobStoreConnection conn, AdaptrisMessage msg) throws Exception {
    String container = msg.resolve(getContainerName());
    String name = msg.resolve(getName());
    BlobStore store = conn.getBlobStore(container);
    // downloadBlob using a temp file is marked as @Beta...
    // entirely expected that it doesn't work.
    if (!tryDownloadBlob(store, container, name, msg)) {
      tryGetBlob(store, container, name, msg);
    }
  }

  protected void tryGetBlob(BlobStore store, String container, String name, AdaptrisMessage msg)
      throws InterlokException, IOException {
    Blob blob = store.getBlob(container, name);
    try (InputStream in = blob.getPayload().openStream(); OutputStream out = msg.getOutputStream()) {
      IOUtils.copy(in, out);
    }
  }

  protected boolean tryDownloadBlob(BlobStore store, String container, String name,
      AdaptrisMessage msg)
      throws InterlokException, IOException {
    boolean rc;
    try {
      File destFile = tempFile();
      store.downloadBlob(container, name, destFile);
      write(destFile, msg);
      rc = true;
    } catch (UnsupportedOperationException e) {
      rc = false;
    }
    return rc;
  }

  protected File tempFile() throws IOException {
    File tempDir = StringUtils.isBlank(getTempDirectory()) ? null : new File(getTempDirectory());
    return File.createTempFile(this.getClass().getSimpleName(), "", tempDir);
  }

  protected void write(File f, AdaptrisMessage msg) throws IOException {
    if (msg instanceof FileBackedMessage) {
      log.trace("Initialising Message from {}", f.getCanonicalPath());
      ((FileBackedMessage) msg).initialiseFrom(f);
    } else {
      try (InputStream in = new FileInputStream(f); OutputStream out = msg.getOutputStream()) {
        IOUtils.copy(in, out);
      }
    }
  }

  public Download withTempDirectory(String s) {
    setTempDirectory(s);
    return this;
  }

}
