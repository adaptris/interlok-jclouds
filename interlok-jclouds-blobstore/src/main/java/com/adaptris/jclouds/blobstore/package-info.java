/**
 * Interact with cloud provider blob storage via <a href="https://jclouds.apache.org">apache jclouds</a>.
 * 
 * <p>
 * All the providers supported by jclouds are listed on their <a href="https://jclouds.apache.org/reference/providers/">website</a>.
 * You should use that as the canonical reference. We have tested the blob storage with 3 different providers (the unit tests use
 * the filesystem provider); and the operations have been confirmed to work.
 * </p>
 * <ul>
 * <li>To access AWS-S3 via jclouds you will need to include the artefact {@code org.apache.jclouds.provider:aws-s3:2.1.0} and use
 * the provider {@code aws-s3}. This was tested for completeness, using the {@code interlok-aws-s3} optional component is generally
 * the better option.</li>
 * <li>To access Backblaze via jclouds you will need to include the artefact {@code org.apache.jclouds.provider:b2:2.1.0} and use
 * the provider {@code b2}</li>
 * <li>To access MS Azure blob storage via jclouds you will need to include the artefact
 * {@code org.apache.jclouds.provider:azureblob:2.1.0} and use the provider {@code azureblob}</li>
 * </ul>
 * 
 */
package com.adaptris.jclouds.blobstore;