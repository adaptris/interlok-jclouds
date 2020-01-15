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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.cloud.RemoteBlobFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * List the contents of a remote blob store
 * 
 * 
 * @config jclouds-blobstore-list
 */
@XStreamAlias("jclouds-blobstore-list")
@DisplayOrder(order = {"containerName", "prefix", "outputStyle", "delimiter", "filterSuffix"})
@ComponentProfile(summary = "List the contents of a remote blob store", since = "3.9.2",
    tag = "jclouds")
public class ListOperation extends ContainerOperation {

  /**
   * The prefix that will be passed as part of {@link ListContainerOptions}.
   * <p>
   * If the container in question is hierarchical, then set that path here to only list the contents of the container that match that
   * prefix.
   * </p>
   */
  @Getter
  @Setter
  @InputFieldDefault(value = "")
  private String prefix;

  /**
   * Apply any filtering on the remote blob before rendering.
   * 
   */
  @AdvancedConfig
  @Getter
  @Setter
  @InputFieldDefault(value = "accept all")
  private RemoteBlobFilter filter;

  /**
   * Specify the output style.
   * 
   * <p>
   * If left as null, then only the names of the files will be emitted. You may require additional optional components to utilise
   * other rendering styles.
   * </p>
   */
  @Getter
  @Setter
  private BlobListRenderer outputStyle;

  public ListOperation() {

  }

  @Override
  public void execute(BlobStoreConnection conn, AdaptrisMessage msg) throws Exception {
    String container = msg.resolve(getContainerName());
    BlobStore store = conn.getBlobStore(container);
    outputStyle().render(list(store, container, msg), msg);
  }

  private Collection<RemoteBlob> list(BlobStore store, String container, AdaptrisMessage msg) throws Exception {
    List<RemoteBlob> blobs = new ArrayList<>();
    String marker = null;
    RemoteBlobFilter filter = blobFilter();
    do {
      PageSet<? extends StorageMetadata> bloblist = store.list(container, buildListOptions(msg, marker));
      for (StorageMetadata meta : bloblist) {
        RemoteBlob blob = new RemoteBlob.Builder().setBucket(container).setLastModified(meta.getLastModified().getTime())
            .setName(meta.getName()).setSize(meta.getSize()).build();
        // Only accept if it's a blob, rather than a directory or similar.
        if (BooleanUtils.and(new boolean[] {filter.accept(blob), meta.getType() == StorageType.BLOB})) {
          blobs.add(blob);
        }
      }
      marker = bloblist.getNextMarker();
    } while (marker != null);
    return blobs;
  }

  private ListContainerOptions buildListOptions(AdaptrisMessage msg, String marker) {
    String pfx = StringUtils.defaultIfEmpty(msg.resolve(getPrefix()), "");
    ListContainerOptions options = ListContainerOptions.Builder.prefix(pfx);
    if (marker != null) {
      options = options.afterMarker(marker);
    }
    return options;
  }

  public ListOperation withFilter(RemoteBlobFilter s) {
    setFilter(s);
    return this;
  }

  public ListOperation withPrefix(String s) {
    setPrefix(s);
    return this;
  }

  public ListOperation withOutputStyle(BlobListRenderer style) {
    setOutputStyle(style);
    return this;
  }

  private BlobListRenderer outputStyle() {
    return ObjectUtils.defaultIfNull(getOutputStyle(), new BlobListRenderer() {});
  }

  private RemoteBlobFilter blobFilter() {
    return ObjectUtils.defaultIfNull(getFilter(), (blob) -> true);
  }
}
