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
import static com.adaptris.jclouds.blobstore.BlobStoreServiceTest.exampleClientConfig;
import static com.adaptris.jclouds.blobstore.OperationCase.createBlob;
import static com.adaptris.jclouds.blobstore.OperationCase.createConnection;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.cloud.RemoteBlobFilterWrapper;
import com.adaptris.interlok.junit.scaffolding.services.ExampleServiceCase;

public class ListBlobServiceTest extends ExampleServiceCase {

  @Test
  public void testService() throws Exception {
    String container = OperationCase.guid.safeUUID();
    BlobStoreConnection con = createConnection();

    ListBlobs service =
        new ListBlobs().withConnection(con).withOutputStyle(null)
        .withContainer(container);
    // Since the service does the lifecycle; we have do bootstrap the connection manually
    // so we can use it
    LifecycleHelper.initAndStart(con);

    try {
      createBlob(con.getBlobStoreContext(), container, "jsonfile.json", "hello world");
      createBlob(con.getBlobStoreContext(), container, "file.csv", "hello world");
      createBlob(con.getBlobStoreContext(), container, "README.txt", "hello world");

      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      List<String> content = IOUtils.readLines(new StringReader(msg.getContent()));
      assertEquals(3, content.size());
    } finally {
      LifecycleHelper.stopAndClose(service);
    }

  }

  @Test
  public void testService_Failure() throws Exception {
    String container = OperationCase.guid.safeUUID();
    BlobStoreConnection con =
        new BlobStoreConnection().withProvider("how-can-this-provider-exist");

    ListBlobs service =
        new ListBlobs().withConnection(con).withOutputStyle(null).withContainer(container);
    try {
      LifecycleHelper.initAndStart(service);
      AdaptrisMessage msg = AdaptrisMessageFactory.getDefaultInstance().newMessage("");
      service.doService(msg);
      fail();
    } catch (ServiceException expected) {

    } finally {
      LifecycleHelper.stopAndClose(service);
    }

  }

  @Override
  protected ListBlobs retrieveObjectForSampleConfig() {
    try {
      return new ListBlobs()
          .withConnection(new BlobStoreConnection().withProvider("aws-s3")
              .withConfiguration(exampleClientConfig()))
          .withContainer("s3-bucket").withPrefix("prefix/or/path/if/you/prefer/")
          .withFilter(
              new RemoteBlobFilterWrapper().withFilterExpression(".*")
              .withFilterImp(RegexFileFilter.class.getCanonicalName()));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

}
