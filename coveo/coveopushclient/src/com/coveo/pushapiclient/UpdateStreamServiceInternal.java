package com.coveo.pushapiclient;

import java.io.IOException;

/** For internal use only. Made to easily test the service without having to use PowerMock */
class UpdateStreamServiceInternal {
  private final StreamDocumentUploadQueue queue;

  public UpdateStreamServiceInternal(final StreamDocumentUploadQueue queue) {
    this.queue = queue;
  }

  public void addOrUpdate(DocumentBuilder document)
      throws IOException, InterruptedException {
    queue.add(document);
  }

  public void addPartialUpdate(PartialUpdateDocument document)
      throws IOException, InterruptedException {
    queue.add(document);
  }

  public void addShallowMerge(ShallowMergeDocument document)
      throws IOException, InterruptedException {
    if (queue instanceof CatalogDocumentUploadQueue) {
      ((CatalogDocumentUploadQueue) queue).add(document);
    } else {
      throw new UnsupportedOperationException(
          "Shallow merge is only supported for Catalog sources. Use CatalogDocumentUploadQueue.");
    }
  }

  public void delete(DeleteDocument document) throws IOException, InterruptedException {
    queue.add(document);
  }

  public void close()
      throws IOException, InterruptedException {
    queue.flush();
  }
}
