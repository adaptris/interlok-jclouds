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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.Args;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.InterlokException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @config jclouds-blobstore-service
 *
 */
@XStreamAlias("jclouds-blobstore-service")
public class BlobStoreService extends ServiceImp implements ConnectedService {

  @Valid
  @NotNull
  private AdaptrisConnection connection;
  @NotNull
  @Valid
  private Operation operation;

  public BlobStoreService() {
  }

  public BlobStoreService(AdaptrisConnection c, Operation o) {
    this();
    setConnection(c);
    setOperation(o);
  }

  @Override
  public void prepare() throws CoreException {
    try {
      Args.notNull(connection, "connection");
      Args.notNull(operation, "operation");
      LifecycleHelper.prepare(getConnection());
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  @Override
  protected void initService() throws CoreException {
    LifecycleHelper.init(getConnection());
  }

  @Override
  public void start() throws CoreException {
    super.start();
    LifecycleHelper.start(getConnection());
  }

  @Override
  public void stop() {
    super.stop();
    LifecycleHelper.stop(getConnection());
  }

  @Override
  protected void closeService() {
    LifecycleHelper.close(getConnection());
  }

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    try {
      getOperation().execute(getConnection().retrieveConnection(BlobStoreConnection.class), msg);
    } catch (InterlokException e) {
      throw ExceptionHelper.wrapServiceException(e);
    }
  }

  public AdaptrisConnection getConnection() {
    return connection;
  }

  public void setConnection(AdaptrisConnection c) {
    this.connection = Args.notNull(c, "connection");
  }

  public Operation getOperation() {
    return operation;
  }

  public void setOperation(Operation operation) {
    this.operation = operation;
  }

}
