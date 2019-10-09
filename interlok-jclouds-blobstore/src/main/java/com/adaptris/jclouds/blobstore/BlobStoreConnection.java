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
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.Setter;

/**
 * Interacting with cloud storage via apache jclouds.
 * 
 * <p>
 * You will need to also have one of the <a href="https://jclouds.apache.org/reference/providers/#blobstore">supported providers</a>
 * in your classpath to be able to use this connection. Use the associated provider in your configuration. Note that
 * {@code identity} and {@code credentials} are not mandatory (and could be overriden via {@link #setConfiguration(KeyValuePairSet)}
 * or system properties). If not explicitly configured, then those values are left to the underlying provider to make a choice about
 * what credentials will be used to access cloud storage (for the aws-s3 provider, it will always fail if no identity/credentials
 * are provided as it doesn't use the java AWS SDK to handle authentication).
 * </p>
 * </p>
 * 
 * @config jclouds-blobstore-connection
 *
 */
@XStreamAlias("jclouds-blobstore-connection")
@DisplayOrder(order =
{
    "provider", "identity", "credentials"
})
@ComponentProfile(summary = "Connect via apache jclouds to a pluggable cloud storage provider",
    recommended = {BlobStoreConnection.class}, tag = "blob,s3,azure,backblaze,cloud")
public class BlobStoreConnection extends AdaptrisConnectionImp {

  /**
   * The cloud storage provider.
   * <p>
   * The value specified here will be passed into {@code ContextBuilder#newBuilder(String)} without
   * any validation. Since jclouds is pluggable; please check
   * <a href="http://jclouds.apache.org/reference/providers/#blobstore-providers">their
   * documentation</a> for the list of supported providers. Please note that individual providers may
   * require additional jars that will not be delivered as part of the standard distribution.
   * </p>
   */
  @NotBlank
  @Getter
  @Setter
  private String provider;
  /**
   * Set the identity used to connect to the storage provider, generally the access key.
   */
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  private String identity;
  /**
   * Set any credentials that are required, generally the secret key.
   */
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  private String credentials;
  /**
   * Set any overrides that are required.
   * <p>
   * These properties will be passed through to
   * {@link ContextBuilder#overrides(java.util.Properties)}.
   * </p>
   * 
   */
  @AdvancedConfig
  @Getter
  @Setter
  @Valid
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
    try {
      ContextBuilder builder = ContextBuilder.newBuilder(getProvider());
      builder.overrides(KeyValuePairBag.asProperties(overrideConfiguration()));
      if (BooleanUtils.or(new boolean[] {
          isNotBlank(getCredentials()), isNotBlank(getIdentity())})) {
        builder.credentials(Password.decode(ExternalResolver.resolve(getIdentity())),
            Password.decode(ExternalResolver.resolve(getCredentials())));
      }
      context = builder.buildView(BlobStoreContext.class);
    } catch (PasswordException e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void startConnection() throws CoreException {
    blobStore = context.getBlobStore();
  }

  @Override
  protected void stopConnection() {
    // nothing to do.
  }

  @Override
  protected void closeConnection() {
    context.close();
    blobStore = null;
    context = null;
  }

  public KeyValuePairSet overrideConfiguration() {
    return ObjectUtils.defaultIfNull(getConfiguration(), new KeyValuePairSet());
  }
}
