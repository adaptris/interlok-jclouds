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

import java.util.ArrayList;
import java.util.List;

import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceCase;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;

public class BlobStoreServiceTest extends ServiceCase {
  private static final String HYPHEN = "-";

  private enum OperationsBuilder {

    Download {
      @Override
      Operation build() {
        Download op = new Download("s3-bucket", "%message{s3-key}");
        op.setTempDirectory("/path/to/temp/dir/if/required");
        return op;
      }
      
    },
    Remove {
      @Override
      Operation build() {
        return new Remove("s3-bucket", "%message{s3-key}");
      }
    },
    Upload {
      @Override
      Operation build() {
        return new Upload("s3-bucket", "%message{s3-key}");
      }
    };
    abstract Operation build();
  }

  public BlobStoreServiceTest(String name) {
    super(name);
  }

  public void testLifecycle() throws Exception {
    BlobStoreService service = new BlobStoreService();
    try {
      LifecycleHelper.init(service);
      fail();
    } catch (CoreException expected) {

    }
    service.setConnection(OperationCase.createConnection());
    service.setOperation(new Upload("container", "name"));
    try {
      LifecycleHelper.initAndStart(service);
    } finally {
      LifecycleHelper.stopAndClose(service);
    }

  }

  @Override
  protected BlobStoreService retrieveObjectForSampleConfig() {
    return null;
  }

  @Override
  protected final List retrieveObjectsForSampleConfig() {
    ArrayList result = new ArrayList();
    for (OperationsBuilder b : OperationsBuilder.values()) {
      result.add(new BlobStoreService(new BlobStoreConnection("aws-s3", exampleClientConfig()), b.build()));
    }
    return result;
  }

  protected KeyValuePairSet exampleClientConfig() {
    KeyValuePairSet kvps = new KeyValuePairSet();
    kvps.add(new KeyValuePair("jclouds.relax-hostname", "true"));
    kvps.add(new KeyValuePair("jclouds.trust-all-certs", "true"));
    return kvps;
  }

  @Override
  protected String createBaseFileName(Object object) {
    return super.createBaseFileName(object) + HYPHEN + ((BlobStoreService) object).getOperation().getClass().getSimpleName();
  }

}
