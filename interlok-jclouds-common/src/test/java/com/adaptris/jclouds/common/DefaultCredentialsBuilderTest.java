package com.adaptris.jclouds.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jclouds.domain.Credentials;
import org.junit.jupiter.api.Test;

import com.adaptris.security.exc.PasswordException;
import com.google.common.base.Supplier;

public class DefaultCredentialsBuilderTest {

  @Test
  public void testBuild() throws Exception {
    DefaultCredentialsBuilder builder = new DefaultCredentialsBuilder().withCredentials("credentials").withIdentity("identity");
    Supplier<Credentials> credentials = builder.build();
    assertNotNull(credentials.get());
  }

  @Test
  public void testBuild_SneakyPassword() throws Exception {
    DefaultCredentialsBuilder builder = new DefaultCredentialsBuilder().withCredentials("PW:X").withIdentity("identity");
    Supplier<Credentials> credentials = builder.build();
    assertThrows(PasswordException.class, () -> credentials.get());
  }

}
