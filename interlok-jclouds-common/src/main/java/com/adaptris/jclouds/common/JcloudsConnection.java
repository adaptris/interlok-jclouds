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
package com.adaptris.jclouds.common;

import java.util.Optional;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;

import org.apache.commons.lang3.ObjectUtils;
import org.jclouds.ContextBuilder;

import com.adaptris.core.AdaptrisConnectionImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.Args;
import com.adaptris.util.KeyValuePairBag;
import com.adaptris.util.KeyValuePairSet;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Connection for interactions via apache jclouds.
 *
 * @config jclouds-blobstore-connection
 *
 */
@NoArgsConstructor
public abstract class JcloudsConnection extends AdaptrisConnectionImp {

  /**
   * The supplier for the any credentials.
   *
   */
  @Getter
  @Setter
  @Valid
  private CredentialsBuilder credentialsBuilder;

  /**
   * The underlying provider.
   * <p>
   * The value specified here will be passed into {@code ContextBuilder#newBuilder(String)} without any validation. Since jclouds is
   * pluggable; please check <a href="http://jclouds.apache.org/reference/providers/">their documentation</a> for the list of supported
   * providers. Please note that individual providers may require additional jars that will not be delivered as part of the standard
   * distribution.
   * </p>
   */
  @NotBlank
  @Getter
  @Setter
  private String provider;

  /**
   * Set any overrides that are required.
   * <p>
   * These properties will be passed through to {@link ContextBuilder#overrides(java.util.Properties)}.
   * </p>
   *
   */
  @Getter
  @Setter
  @Valid
  private KeyValuePairSet configuration = new KeyValuePairSet();

  @Override
  protected void prepareConnection() throws CoreException {
    Args.notBlank(getProvider(), "provider");
  }

  protected ContextBuilder newContextBuilder() {
    ContextBuilder builder = ContextBuilder.newBuilder(getProvider());
    builder.overrides(KeyValuePairBag.asProperties(overrideConfiguration()));
    credentialsBuilder().ifPresent((credentials) -> {
      builder.credentialsSupplier(credentials.build());
    });
    return builder;
  }

  @SuppressWarnings("unchecked")
  public <T extends JcloudsConnection> T withCredentials(CredentialsBuilder b) {
    setCredentialsBuilder(b);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public <T extends JcloudsConnection> T withConfiguration(KeyValuePairSet cfg) {
    setConfiguration(cfg);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public <T extends JcloudsConnection> T withProvider(String provider) {
    setProvider(provider);
    return (T) this;
  }

  protected Optional<CredentialsBuilder> credentialsBuilder() {
    return Optional.ofNullable(getCredentialsBuilder());
  }

  protected KeyValuePairSet overrideConfiguration() {
    return ObjectUtils.defaultIfNull(getConfiguration(), new KeyValuePairSet());
  }

}
