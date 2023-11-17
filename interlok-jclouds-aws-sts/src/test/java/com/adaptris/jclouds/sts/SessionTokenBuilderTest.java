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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jclouds.domain.Credentials;
import org.junit.jupiter.api.Test;

import com.adaptris.security.exc.PasswordException;
import com.google.common.base.Supplier;

public class SessionTokenBuilderTest {

  @Test
  public void testBuild() throws Exception {
    SessionTokenCredentialsBuilder builder = new SessionTokenCredentialsBuilder().withSessionToken("token").withCredentials("credentials")
        .withIdentity("identity");
    Supplier<Credentials> credentials = builder.build();
    assertNotNull(credentials.get());
  }

  @Test
  public void testBuild_SneakyPassword() throws Exception {
    SessionTokenCredentialsBuilder builder = new SessionTokenCredentialsBuilder().withSessionToken("token").withCredentials("PW:X")
        .withIdentity("identity");
    Supplier<Credentials> credentials = builder.build();

    assertThrows(PasswordException.class, () -> credentials.get());
  }

}
