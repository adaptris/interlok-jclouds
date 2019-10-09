package com.adaptris.jclouds.blobstore;

import javax.validation.constraints.NotBlank;
import org.jclouds.blobstore.options.ListContainerOptions;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConnectedService;
import com.adaptris.core.CoreException;
import com.adaptris.core.DynamicPollingTemplate;
import com.adaptris.core.ServiceException;
import com.adaptris.core.ServiceImp;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.interlok.cloud.BlobListRenderer;
import com.adaptris.interlok.cloud.RemoteBlobFilter;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * Query a cloud provider location for a list of blobs store there.
 * <p>
 * Under the covers it re-uses {@link BlobStoreService} with a {@link ListOperation} and does a full
 * lifecycle on the underlying service each time it is triggered. This is intended for use as part
 * of a {@link DynamicPollingTemplate}; as a result keys are not intended to be resolved using the
 * {@code %message} expression language; they will, however, be passed as-is into the underlying
 * service (which may still resolve them).
 * </p>
 * 
 * @config jclouds-container-list
 */
@XStreamAlias("jclouds-container-list")
@ComponentProfile(summary = "List contents of an jclouds container as part of a polling-trigger",
    since = "3.9.2", tag = "jclouds,polling")
public class ListBlobs extends ServiceImp
    implements DynamicPollingTemplate.TemplateProvider, ConnectedService {

  @Setter
  @Getter
  private AdaptrisConnection connection;

  @Setter
  @Getter
  private BlobListRenderer outputStyle;

  /**
   * The name of the container / S3 bucket.
   * 
   */
  @NotBlank
  @Setter
  @Getter
  @NonNull
  private String container;

  /**
   * The prefix that will be passed as part of {@link ListContainerOptions}.
   * <p>
   * If the container in question is hierarchical, then set that path here to only list the contents of the container that match that
   * prefix.
   * </p>
   */
  @Setter
  @Getter
  private String prefix;

  /**
   * Apply any filtering on the remote blob before rendering.
   * 
   */
  @AdvancedConfig
  @Getter
  @Setter
  @InputFieldDefault(value = "accept all")
  private RemoteBlobFilter filter;

  @Override
  public void prepare() throws CoreException {}

  @Override
  protected void initService() throws CoreException {}

  @Override
  protected void closeService() {}

  @Override
  public void doService(AdaptrisMessage msg) throws ServiceException {
    BlobStoreService service = buildService();
    try {
      LifecycleHelper.initAndStart(service, false);
      service.doService(msg);
    } catch (Exception e) {
      throw ExceptionHelper.wrapServiceException(e);
    } finally {
      LifecycleHelper.stopAndClose(service, false);
    }
  }

  public ListBlobs withConnection(AdaptrisConnection c) {
    setConnection(c);
    return this;
  }

  public ListBlobs withPrefix(String key) {
    setPrefix(key);
    return this;
  }

  public ListBlobs withContainer(String bucket) {
    setContainer(bucket);
    return this;
  }

  public ListBlobs withFilter(RemoteBlobFilter filter) {
    setFilter(filter);
    return this;
  }


  public ListBlobs withOutputStyle(BlobListRenderer outputStyle) {
    setOutputStyle(outputStyle);
    return this;
  }

  private BlobStoreService buildService() {
    ListOperation op = new ListOperation().withPrefix(getPrefix()).withOutputStyle(getOutputStyle())
        .withFilter(getFilter()).withContainerName(getContainer());
    return new BlobStoreService(getConnection(), op);
  }

}
