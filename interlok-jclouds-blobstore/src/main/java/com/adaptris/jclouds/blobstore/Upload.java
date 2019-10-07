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

import org.apache.commons.lang3.BooleanUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.Blob;
import org.jclouds.blobstore.domain.BlobBuilder;
import org.jclouds.blobstore.domain.BlobBuilder.PayloadBlobBuilder;
import org.jclouds.blobstore.options.PutOptions;
import org.jclouds.blobstore.strategy.internal.MultipartUploadSlicingAlgorithm;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.lms.FileBackedMessage;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Upload an object.
 * 
 * @config jclouds-blobstore-upload
 */
@XStreamAlias("jclouds-blobstore-upload")
@DisplayOrder(order = {"containerName", "name", "useMultipart"})
@ComponentProfile(summary = "Upload a blob", tag = "jclouds")
public class Upload extends OperationImpl {

  /**
   * Whether or not to use multiparts when uploading.
   * <p>
   * If not specified, then defaults to {@code true}; however, if
   * {@code MultipartUploadSlicingAlgorithm} indicates that the object will not be at least two parts
   * then this option has no effect.
   * </p>
   */
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean useMultipart;

  // see MultipartUploadSlicingAlgorithm

  public Upload() {

  }

  public Upload(String container, String name) {
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
      Blob blob = build(store.blobBuilder(name), msg);
      store.putBlob(container, blob, buildPutOptions(store, msg));
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private Blob build(BlobBuilder builder, AdaptrisMessage msg) throws Exception {
    PayloadBlobBuilder payloadBuilder = null;
    if (msg instanceof FileBackedMessage) {
      payloadBuilder = builder.payload(((FileBackedMessage) msg).currentSource());
    } else {
      payloadBuilder =  builder.payload(msg.getInputStream());
    }
    payloadBuilder.contentLength(msg.getSize());
    Blob blob = builder.build();
    return blob;
  }
  
  private PutOptions buildPutOptions(BlobStore store, AdaptrisMessage msg) {
    PutOptions result = PutOptions.NONE;
    if (atLeastTwoParts(store, msg.getSize())) {
      result = PutOptions.Builder.multipart(BooleanUtils.toBooleanDefaultIfNull(getUseMultipart(), true));
    } else {
      log.trace("Message of size {} probably won't benefit from a multipart-upload", msg.getSize());
    }
    return result;
  }

  private boolean atLeastTwoParts(BlobStore store, long msgSize) {
    // Testing with backblaze, if you enable multiparts, and you're only uploading a small
    // file, it complains as it wants at least 2 parts.
    // AWS-S3 doesn't seem to care.
    MultipartUploadSlicingAlgorithm slicer = new MultipartUploadSlicingAlgorithm(store.getMinimumMultipartPartSize(),
        store.getMaximumMultipartPartSize(), store.getMaximumNumberOfParts());
    slicer.calculateChunkSize(msgSize);
    return slicer.getParts() > 1;
  }
}
