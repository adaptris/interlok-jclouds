package com.adaptris.jclouds.common;

import org.jclouds.domain.Credentials;

import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.google.common.base.Supplier;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

/**
 * Default implementation of {@link CredentialsBuilder} for use with jclouds.
 *
 */
@XStreamAlias("jclouds-default-credentials-builder")
@DisplayOrder(order = { "identity", "credentials" })
@ComponentProfile(summary = "Provide credentials")
public class DefaultCredentialsBuilder implements CredentialsBuilder {
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

  @Override
  public Supplier<Credentials> build() {
    return () -> credentials();
  }

  @SneakyThrows(PasswordException.class)
  private Credentials credentials() {
    return new Credentials(Password.decode(ExternalResolver.resolve(getIdentity())),
        Password.decode(ExternalResolver.resolve(getCredentials())));
  }

  @SuppressWarnings("unchecked")
  public <T extends DefaultCredentialsBuilder> T withIdentity(String id) {
    setIdentity(id);
    return (T) this;
  }

  @SuppressWarnings("unchecked")
  public <T extends DefaultCredentialsBuilder> T withCredentials(String s) {
    setCredentials(s);
    return (T) this;
  }

}
