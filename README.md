# interlok-jclouds

[![GitHub tag](https://img.shields.io/github/tag/adaptris/interlok-jclouds.svg)](https://github.com/adaptris/interlok-jclouds/tags)
[![license](https://img.shields.io/github/license/adaptris/interlok-jclouds.svg)](https://github.com/adaptris/interlok-jclouds/blob/develop/LICENSE)
[![Actions Status](https://github.com/adaptris/interlok-jclouds/actions/workflows/gradle-publish.yml/badge.svg)](https://github.com/adaptris/interlok-jclouds/actions)
[![codecov](https://codecov.io/gh/adaptris/interlok-jclouds/branch/develop/graph/badge.svg)](https://codecov.io/gh/adaptris/interlok-jclouds)
[![CodeQL](https://github.com/adaptris/interlok-jclouds/workflows/CodeQL/badge.svg)](https://github.com/adaptris/interlok-jclouds/security/code-scanning)
[![Known Vulnerabilities](https://snyk.io/test/github/adaptris/interlok-jclouds/badge.svg?targetFile=build.gradle)](https://snyk.io/test/github/adaptris/interlok-jclouds?targetFile=build.gradle)
[![Closed PRs](https://img.shields.io/github/issues-pr-closed/adaptris/interlok-jclouds)](https://github.com/adaptris/interlok-jclouds/pulls?q=is%3Apr+is%3Aclosed)


The suggested name was `fantastic-train`

This provides interlok services based around apache jclouds (https://jclouds.apache.org). Initially for the BlobStore capability allowing you to use this to access backblaze/aws-s3/azure easily.

All the providers supported by jclouds are listed on their [website](https://jclouds.apache.org/reference/providers/). You should use that as the canonical reference. We have tested the blob storage with 3 different providers (the unit tests use the filesystem provider and the operations have been confirmed to work.

- To access AWS-S3 via jclouds you will need to include the artefact `org.apache.jclouds.provider:aws-s3:XYZ` where _XYZ_ is the appropriate version; use the provider `aws-s3`. This was tested for completeness, using the [interlok-aws](https://github.com/adaptris/interlok-aws) optional component is generally the better option.
- To access Backblaze via jclouds you will need to include the artefact `org.apache.jclouds.provider:b2:XYZ` where _XYZ_ is the appropriate version; use the provider `b2`
- To access MS Azure blob storage via jclouds you will need to include the artefact `org.apache.jclouds.provider:azureblob:XYZ` where _XYZ_ is the appropriate version; use the provider `azureblob`
