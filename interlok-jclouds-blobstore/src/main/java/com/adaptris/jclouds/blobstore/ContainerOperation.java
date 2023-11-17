package com.adaptris.jclouds.blobstore;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.InputFieldHint;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Abstract base class for Blobstore Operations.
 *
 */
public abstract class ContainerOperation implements Operation {
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  /**
   * The name of the container / S3 bucket.
   *
   */
  @NotNull
  @Valid
  @InputFieldHint(expression = true)
  @Setter
  @Getter
  @NonNull
  private String containerName;

  public ContainerOperation() {
  }

  @SuppressWarnings("unchecked")
  public <T extends ContainerOperation> T withContainerName(String s) {
    setContainerName(s);
    return (T) this;
  }

}
