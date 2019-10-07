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
public abstract class OperationImpl implements Operation {
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
  /**
   * The name of the object to manipulate
   * 
   */
  @NotNull
  @Setter
  @Getter
  @Valid
  @InputFieldHint(expression = true)
  @NonNull
  private String name;

  public OperationImpl() {
  }


  public <T extends OperationImpl> T withContainerName(String s) {
    setContainerName(s);
    return (T) this;
  }

  public <T extends OperationImpl> T withName(String s) {
    setName(s);
    return (T) this;
  }
}
