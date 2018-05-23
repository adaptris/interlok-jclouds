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

import org.jclouds.filesystem.reference.FilesystemConstants;
import org.junit.Rule;
import org.junit.rules.TestName;

import com.adaptris.core.stubs.TempFileUtils;
import com.adaptris.util.GuidGenerator;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public abstract class OperationCase {

  protected static GuidGenerator guid = new GuidGenerator();

  @Rule
  public TestName testName = new TestName();

  public static BlobStoreConnection createConnection() throws Exception {
    KeyValuePairSet config = new KeyValuePairSet();
    config.add(new KeyValuePair(FilesystemConstants.PROPERTY_BASEDIR, TempFileUtils.createTrackedDir(config).getCanonicalPath()));
    BlobStoreConnection c = new BlobStoreConnection("filesystem", config);
    return c;
  }
}
