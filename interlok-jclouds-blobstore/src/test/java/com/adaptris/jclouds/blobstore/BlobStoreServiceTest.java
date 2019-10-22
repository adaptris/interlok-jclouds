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
    List {
      @Override
      Operation build() {
        return new ListOperation()
            .withContainerName("s3-bucket");
      }

    },
    Copy {
      @Override
      Operation build() {
        return new Copy()
            .withDestinationContainerName("s3-target-bucket")
            .withDestinationName("%message{s3-target-key}")
            .withName("%message{s3-key}").withContainerName("s3-src-bucket");
      }
      
    },
    Download {
      @Override
      Operation build() {
        return new Download().withTempDirectory("/path/to/temp/dir/if/required")
            .withName("%message{s3-key}").withContainerName("s3-bucket");
      }

    },
    Remove {
      @Override
      Operation build() {
        return new Remove().withName("%message{s3-key}").withContainerName("s3-bucket");
      }
    },
    Upload {
      @Override
      Operation build() {
        return new Upload().withName("%message{s3-key}").withContainerName("s3-bucket");
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
      LifecycleHelper.initAndStart(service);
      fail();
    } catch (CoreException expected) {

    } finally {
      LifecycleHelper.stopAndClose(service);
    }
    service.setConnection(OperationCase.createConnection());
    service.setOperation(new Upload().withName("name").withContainerName("container"));
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

  public static KeyValuePairSet exampleClientConfig() {
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
