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
package com.adaptris.jclouds.sts;

import org.jclouds.aws.domain.SessionCredentials;
import org.jclouds.domain.Credentials;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.jclouds.common.DefaultCredentialsBuilder;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.google.common.base.Supplier;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * AWS STS Credentials Provider
 *
 *
 * @config jclouds-sts-credentials-builder
 *
 */
@XStreamAlias("jclouds-sts-credentials-builder")
@DisplayOrder(order = { "identity", "credentials", "sessionToken" })
@ComponentProfile(summary = "Provide credentials via Amazon STS")
public class SessionTokenCredentialsBuilder extends DefaultCredentialsBuilder {

  /**
   * Set the session token required.
   */
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  private String sessionToken;

  @Override
  public Supplier<Credentials> build() {
    return () -> credentials();
  }

  @SuppressWarnings("unchecked")
  public <T extends SessionTokenCredentialsBuilder> T withSessionToken(String token) {
    setSessionToken(token);
    return (T) this;
  }

  @SneakyThrows(PasswordException.class)
  private Credentials credentials() {
    return SessionCredentials.builder().identity(Password.decode(ExternalResolver.resolve(getIdentity())))
        .credential(Password.decode(ExternalResolver.resolve(getCredentials())))
        .sessionToken(Password.decode(ExternalResolver.resolve(getSessionToken()))).build();
  }

}
