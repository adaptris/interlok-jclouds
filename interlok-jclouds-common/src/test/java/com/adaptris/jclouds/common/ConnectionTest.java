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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jclouds.filesystem.reference.FilesystemConstants;
import org.junit.jupiter.api.Test;

import com.adaptris.core.CoreException;
import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class ConnectionTest {

  @Test
  public void testConfiguration() throws Exception {
    MyJcloudsConnection con = new MyJcloudsConnection();
    assertNotNull(con.overrideConfiguration());
    assertEquals(0, con.overrideConfiguration().size());
    KeyValuePairSet cfg = new KeyValuePairSet();
    cfg.add(new KeyValuePair(FilesystemConstants.PROPERTY_BASEDIR, "/tmp"));
    con.withConfiguration(cfg);
    assertNotNull(con.overrideConfiguration());
    assertEquals(cfg, con.overrideConfiguration());
  }

  @Test
  public void testLifecycle() throws Exception {
    JcloudsConnection con = createConnection();
    try {
      LifecycleHelper.initAndStart(con);
      assertFalse(con.credentialsBuilder().isPresent());
      assertNotNull(con.newContextBuilder());
    } finally {
      LifecycleHelper.stopAndClose(con);
    }
  }

  @Test
  public void testLifecycle_WithCredentials() throws Exception {
    JcloudsConnection con = createConnection().withCredentials(new DefaultCredentialsBuilder().withCredentials("c").withIdentity("i"));
    try {
      LifecycleHelper.initAndStart(con);
      assertTrue(con.credentialsBuilder().isPresent());
      assertNotNull(con.newContextBuilder());

    } finally {
      LifecycleHelper.stopAndClose(con);
    }
  }

  public static JcloudsConnection createConnection() throws Exception {
    KeyValuePairSet config = new KeyValuePairSet();
    config.add(new KeyValuePair(FilesystemConstants.PROPERTY_BASEDIR, TempFileUtils.createTrackedDir(config).getCanonicalPath()));
    MyJcloudsConnection c = new MyJcloudsConnection().withConfiguration(config).withCredentials(null).withProvider("filesystem");
    return c;
  }

  private static class MyJcloudsConnection extends JcloudsConnection {
    @Override
    protected void initConnection() throws CoreException {
    }

    @Override
    protected void startConnection() throws CoreException {
    }

    @Override
    protected void stopConnection() {
    }

    @Override
    protected void closeConnection() {
    }
  }

}
