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

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import java.util.Optional;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.jclouds.ContextBuilder;
import org.jclouds.blobstore.BlobStore;
import org.jclouds.blobstore.BlobStoreContext;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.jclouds.common.CredentialsBuilder;
import com.adaptris.jclouds.common.DefaultCredentialsBuilder;
import com.adaptris.jclouds.common.JcloudsConnection;
import com.adaptris.util.KeyValuePairSet;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Interacting with cloud storage via apache jclouds.
 * 
 * <p>
 * You will need to also have one of the
 * <a href="https://jclouds.apache.org/reference/providers/#blobstore">supported providers</a> in
 * your classpath to be able to use this connection. Use the associated provider in your
 * configuration. Note that {@code identity} and {@code credentials} are not mandatory (and could be
 * overriden via {@link #setConfiguration(KeyValuePairSet)} or system properties). If not explicitly
 * configured, then those values are left to the underlying provider to make a choice about what
 * credentials will be used to access cloud storage (for the aws-s3 provider, it will always fail if
 * no identity/credentials are provided as it doesn't use the java AWS SDK to handle
 * authentication).
 * </p>
 * <p>
 * All the providers supported by jclouds are listed on their
 * <a href="https://jclouds.apache.org/reference/providers/">website</a>. You should use that as the
 * canonical reference. We have tested the blob storage with 3 different providers (the unit tests
 * use the filesystem provider); and the operations have been confirmed to work.
 * </p>
 * <ul>
 * <li>To access AWS-S3 via jclouds you will need to include the artefact
 * {@code org.apache.jclouds.provider:aws-s3:XYZ}; where {@code XYZ} is the appropriate version; and
 * use the provider {@code aws-s3}. This was tested for completeness, using the
 * {@code interlok-aws-s3} optional component is generally the better option.</li>
 * <li>To access Backblaze via jclouds you will need to include the artefact
 * {@code org.apache.jclouds.provider:b2:XYZ}; where {@code XYZ} is the appropriate version; and use
 * the provider {@code b2}</li>
 * <li>To access MS Azure blob storage via jclouds you will need to include the artefact
 * {@code org.apache.jclouds.provider:azureblob:XYZ}; where {@code XYZ} is the appropriate version;
 * and use the provider {@code azureblob}</li>
 * </ul>
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
@NoArgsConstructor
public class BlobStoreConnection extends JcloudsConnection {

  /**
   * Set the identity used to connect to the storage provider, generally the access key.
   * 
   * @deprecated since 3.10.2 use a {@code CredentialsBuilder} instead.
   */
  @AdvancedConfig(rare = true)
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  @Deprecated
  @Removal(version = "3.12.0", message = "use a credentials-builder instead")
  private String identity;
  /**
   * Set any credentials that are required, generally the secret key.
   * 
   * @deprecated since 3.10.2 use a {@code CredentialsBuilder} instead.
   */
  @AdvancedConfig(rare = true)
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  @Deprecated
  @Removal(version = "3.12.0", message = "use a credentials-builder instead")
  private String credentials;

  private transient BlobStoreContext context;
  private transient BlobStore blobStore;
  private transient boolean credentialsWarningLogged;
  private transient Optional<CredentialsBuilder> legacyCredentials;

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
    super.prepareConnection();
    if (BooleanUtils.or(new boolean[] {isNotBlank(getCredentials()), isNotBlank(getIdentity())})) {
      LoggingHelper.logWarning(credentialsWarningLogged, () -> credentialsWarningLogged = true,
          "[{}] uses static credentials/identity, use a credentials-builder instead",
          LoggingHelper.friendlyName(this));
      legacyCredentials = Optional.of(new DefaultCredentialsBuilder().withCredentials(getCredentials())
              .withIdentity(getIdentity()));
    }
  }


  @Override
  protected void initConnection() throws CoreException {
    ContextBuilder builder = newContextBuilder();
    context = builder.buildView(BlobStoreContext.class);
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
  @SuppressWarnings("deprecation")
  protected void closeConnection() {
    IOUtils.closeQuietly(context);
    blobStore = null;
    context = null;
  }

  @Override
  protected Optional<CredentialsBuilder> credentialsBuilder() {
    return ObjectUtils.defaultIfNull(legacyCredentials, super.credentialsBuilder());
  }

}
