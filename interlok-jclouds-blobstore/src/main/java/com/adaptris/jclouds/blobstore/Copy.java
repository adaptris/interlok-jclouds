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

import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.options.CopyOptions;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Copy a blob to a different location in the same provider
 * 
 * 
 * @config jclouds-blobstore-copy
 */
@XStreamAlias("jclouds-blobstore-copy")
@DisplayOrder(order = {"containerName", "name", "destinationContainerName", "destinationName"})
@ComponentProfile(summary = "Copy a blob to a different location in the same provider", since = "3.9.2", tag = "jclouds")
public class Copy extends OperationImpl {

  /**
   * The destination container name.
   * <p>
   * If left null or blank, then the src container is assumed
   * </p>
   */
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  private String destinationContainerName;
  @NotNull
  @InputFieldHint(expression = true)
  @Getter
  @Setter
  @NonNull
  private String destinationName;

  public Copy() {

  }

  @Override
  public void execute(BlobStoreConnection conn, AdaptrisMessage msg) throws Exception {
    String srcContainer = msg.resolve(getContainerName());
    String srcName = msg.resolve(getName());
    String destContainer =
        StringUtils.defaultIfBlank(msg.resolve(getDestinationContainerName()), srcContainer);
    String destName = msg.resolve(getDestinationName());
    BlobStore store = conn.getBlobStore(srcContainer);
    store.copyBlob(srcContainer, srcName, destContainer, destName, CopyOptions.NONE);
  }


  public Copy withDestinationContainerName(String s) {
    setDestinationContainerName(s);
    return this;
  }

  public Copy withDestinationName(String s) {
    setDestinationName(s);
    return this;
  }
}
