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

import static org.junit.jupiter.api.Assertions.fail;

import org.jclouds.blobstore.BlobStoreContext;
import org.junit.jupiter.api.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.jclouds.common.CredentialsBuilder;
import com.adaptris.jclouds.common.DefaultCredentialsBuilder;

public class ConnectionTest extends OperationCase {

  @Test
  public void testConnection_LegacyCredentials() throws Exception {
    String container = guid.safeUUID();

    CredentialsBuilder credentialsBuilder = new DefaultCredentialsBuilder().withIdentity("x").withCredentials("x");

    BlobStoreConnection con = createConnection();
    con.setCredentialsBuilder(credentialsBuilder);

    LifecycleHelper.initAndStart(con);
    try {
      BlobStoreContext ctx = con.getBlobStoreContext();
      ctx.getBlobStore();
      con.getBlobStore(container);
      fail();
    } catch (CoreException expected) {

    } finally {
      LifecycleHelper.stopAndClose(con);
    }
  }

}
