package com.adaptris.jclouds.blobstore;

import java.util.Date;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.domain.PageSet;
import org.jclouds.blobstore.domain.StorageMetadata;
import org.jclouds.blobstore.domain.StorageType;
import org.jclouds.blobstore.options.ListContainerOptions;

import com.adaptris.interlok.cloud.RemoteBlob;
import com.adaptris.interlok.cloud.RemoteBlobFilter;
import com.adaptris.interlok.cloud.RemoteBlobIterableImpl;
import com.adaptris.util.NumberUtils;

class RemoteBlobIterable extends RemoteBlobIterableImpl<StorageMetadata> {

  private RemoteBlobFilter blobFilter = null;
  private BlobStore blobStorage;
  private String containerName;
  private String blobPrefix;
  private PageSet<? extends StorageMetadata> currentPage;
  private Iterator<? extends StorageMetadata> pageIterator;

  protected RemoteBlobIterable(BlobStore store, String container, String prefix, RemoteBlobFilter filter) {
    blobStorage = store;
    containerName = container;
    blobPrefix = prefix;
    blobFilter = filter;
  }

  @Override
  protected void iteratorInit() {
    currentPage = blobStorage.list(containerName, buildListOptions(blobPrefix, null));
    pageIterator = currentPage.iterator();
  }

  @Override
  protected Optional<StorageMetadata> nextStorageItem() throws NoSuchElementException {
    if (!pageIterator.hasNext()) {
      advanceToNextPage();
    }
    return Optional.ofNullable(pageIterator.next());
  }

  private void advanceToNextPage() throws NoSuchElementException {
    String hasNextPage = currentPage.getNextMarker();
    if (hasNextPage == null) {
      throw new NoSuchElementException();
    }
    currentPage = blobStorage.list(containerName, buildListOptions(blobPrefix, hasNextPage));
    pageIterator = currentPage.iterator();
  }

  @Override
  protected Optional<RemoteBlob> accept(StorageMetadata meta) {
    RemoteBlob blob = new RemoteBlob.Builder().setBucket(containerName).setLastModified(lastModified(meta))
        .setName(meta.getName()).setSize(NumberUtils.toLongDefaultIfNull(meta.getSize(), -1)).build();
    // Only accept if it's a blob, rather than a directory or similar.
    if (BooleanUtils.and(new boolean[] {blobFilter.accept(blob), meta.getType() == StorageType.BLOB})) {
      return Optional.of(blob);
    }
    return Optional.empty();
  }

  static long lastModified(StorageMetadata meta) {
    return Optional.ofNullable(meta.getLastModified()).map(Date::getTime).orElseGet(() -> -1L);
  }

  private ListContainerOptions buildListOptions(String prefix, String marker) {
    String pfx = StringUtils.defaultIfEmpty(prefix, "");
    ListContainerOptions options = ListContainerOptions.Builder.prefix(pfx);
    if (marker != null) {
      options = options.afterMarker(marker);
    }
    return options;
  }
}


