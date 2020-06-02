package com.adaptris.jclouds.common;

import org.jclouds.domain.Credentials;
import com.google.common.base.Supplier;

@FunctionalInterface
public interface CredentialsBuilder {

  /**
   * Build the supplier of credentials.
   * 
   */
  Supplier<Credentials> build();
}
