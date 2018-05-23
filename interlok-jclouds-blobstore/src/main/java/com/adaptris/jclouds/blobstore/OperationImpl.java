package com.adaptris.jclouds.blobstore;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.util.Args;

/**
 * Abstract base class for Blobstore Operations.
 * 
 */
public abstract class OperationImpl implements Operation {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());


  @NotNull
  @Valid
  @InputFieldHint(expression = true)
  private String containerName;
  @NotNull
  @Valid
  @InputFieldHint(expression = true)
  private String name;

  public OperationImpl() {
  }

  public String getName() {
    return name;
  }

  /**
   * Set the name of the object to manipulate.
   * 
   * @param key
   */
  public void setName(String key) {
    this.name = Args.notBlank(key, "key");
  }

  public String getContainerName() {
    return containerName;
  }

  /**
   * Set the name of the container.
   * 
   * @param name the name of the container / s3 bucket.
   */
  public void setContainerName(String name) {
    this.containerName = Args.notBlank(name, "containerName");
  }

}
