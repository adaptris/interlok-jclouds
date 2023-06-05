package com.adaptris.jclouds.blobstore;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.InputFieldHint;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Abstract base class for Blobstore Operations.
 *
 */
public abstract class OperationImpl extends ContainerOperation {

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

  @SuppressWarnings("unchecked")
  public <T extends OperationImpl> T withName(String s) {
    setName(s);
    return (T) this;
  }

}
