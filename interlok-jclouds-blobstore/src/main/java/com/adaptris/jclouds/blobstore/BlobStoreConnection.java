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

import static org.apache.commons.lang.StringUtils.isNotBlank;

import org.hibernate.validator.constraints.NotBlank;
import org.jclouds.Constants;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * Interacting with cloud storage via apache jclouds.
 * 
 * <p>
 * You will need to also have one of the <a href="https://jclouds.apache.org/reference/providers/#blobstore">supported providers</a>
 * in your classpath to be able to use this connection. {@code identity} and {@code credentials} are not mandatory, and it is left
 * to the underlying provider to make a choice about what credentials will be used to access cloud storage.
 * </p>
 * </p>
 * 
 * @config jclouds-blobstore-connection
 *
 */
@XStreamAlias("jclouds-blobstore-connection")
public class BlobStoreConnection extends AdaptrisConnectionImp {

  @NotBlank
  private String provider;
  private String identity;
  private String credentials;
  @AdvancedConfig
  private KeyValuePairSet configuration;

  private transient BlobStoreContext context;
  private transient BlobStore blobStore;

  public BlobStoreConnection() {

  }

  public BlobStoreConnection(String provider, KeyValuePairSet cfg) {
    this();
    setProvider(provider);
    setConfiguration(cfg);
  }

  protected BlobStoreContext getBlobStoreContext() {
    return context;
  }

  protected BlobStore getBlobStore(String bucket) throws CoreException {
    if (!blobStore.containerExists(bucket)) {
      throw new CoreException(bucket + " does not exist");
    }
    return blobStore;
  }

  @Override
  protected void prepareConnection() throws CoreException {
    try {
      Args.notBlank(getProvider(), "provider");
    } catch (IllegalArgumentException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void initConnection() throws CoreException {
    ContextBuilder builder = ContextBuilder.newBuilder(getProvider());
    builder.overrides(KeyValuePairBag.asProperties(overrideConfiguration()));
    if (isNotBlank(getCredentials()) || isNotBlank(getIdentity())) {
      builder.credentials(getIdentity(), getCredentials());
    }
    context = builder.buildView(BlobStoreContext.class);
  }

  @Override
  protected void startConnection() throws CoreException {
    blobStore = context.getBlobStore();
  }

  @Override
  protected void stopConnection() {
  }

  @Override
  protected void closeConnection() {
    context.close();
    blobStore = null;
    context = null;
  }

  public String getProvider() {
    return provider;
  }

  public void setProvider(String provider) {
    this.provider = Args.notBlank(provider, "provider");
  }

  public String getIdentity() {
    return identity;
  }

  public void setIdentity(String id) {
    this.identity = id;
  }

  public String getCredentials() {
    return credentials;
  }

  public void setCredentials(String cred) {
    this.credentials = cred;
  }

  /**
   * @return the overrides
   */
  public KeyValuePairSet getConfiguration() {
    return configuration;
  }

  /**
   * Set any overrides that are required.
   * <p>
   * These properties will be passed through to {@link ContextBuilder#overrides(java.util.Properties)}.
   * </p>
   * 
   * @see Constants
   * @param b the overrides to set
   */
  public void setConfiguration(KeyValuePairSet b) {
    this.configuration = b;
  }

  public KeyValuePairSet overrideConfiguration() {
    return getConfiguration() != null ? getConfiguration() : new KeyValuePairSet();
  }
}
