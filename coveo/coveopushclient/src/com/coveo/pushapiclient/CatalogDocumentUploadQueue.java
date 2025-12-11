package com.coveo.pushapiclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;

/**
 * A specialized queue for catalog update operations that supports shallow merge (addOrMerge).
 * 
 * <p>This queue extends StreamDocumentUploadQueue to add support for shallow merge operations
 * which are specific to Catalog sources.
 * 
 * @see <a href="https://docs.coveo.com/en/p4eb0515/coveo-for-commerce/partial-catalog-data-updates">Partial Catalog Data Updates</a>
 */
public class CatalogDocumentUploadQueue extends StreamDocumentUploadQueue {

  private static final Logger logger = LogManager.getLogger(CatalogDocumentUploadQueue.class);
  protected ArrayList<ShallowMergeDocument> documentToShallowMergeList;

  public CatalogDocumentUploadQueue(UploadStrategy uploader) {
    super(uploader);
    this.documentToShallowMergeList = new ArrayList<>();
  }

  /**
   * Flushes the accumulated documents by applying the upload strategy.
   *
   * @throws IOException If an I/O error occurs during the upload.
   * @throws InterruptedException If the upload process is interrupted.
   */
  @Override
  public void flush() throws IOException, InterruptedException {
    if (this.isEmpty()) {
      logger.debug("Empty batch. Skipping upload");
      return;
    }
    StreamUpdate catalogUpdate = this.getStreamWithShallowMerge();
    logger.info("Uploading catalog update with shallow merge");
    this.uploader.apply(catalogUpdate);

    this.size = 0;
    this.documentToAddList.clear();
    this.documentToDeleteList.clear();
    this.documentToPartiallyUpdateList.clear();
    this.documentToShallowMergeList.clear();
  }

  /**
   * Adds a {@link ShallowMergeDocument} to the upload queue and flushes the queue if it exceeds
   * the maximum content length.
   *
   * @param document The document to be shallow merged into the index.
   * @throws IOException If an I/O error occurs during the upload.
   * @throws InterruptedException If the upload process is interrupted.
   */
  public void add(ShallowMergeDocument document) throws IOException, InterruptedException {
    if (document == null) {
      return;
    }

    final int sizeOfDoc = document.marshalJsonObject().toString().getBytes().length;
    if (this.size + sizeOfDoc >= this.maxQueueSize) {
      this.flush();
    }
    documentToShallowMergeList.add(document);
    if (logger.isDebugEnabled()) {
      logger.debug("Adding document to shallow merge batch: " + document.documentId);
    }
    this.size += sizeOfDoc;
  }

  /**
   * Creates a StreamUpdate with shallow merge support for catalog sources.
   *
   * @return The StreamUpdate containing all queued documents with addOrMerge mode enabled.
   */
  public StreamUpdate getStreamWithShallowMerge() {
    return new StreamUpdate(
        new ArrayList<>(this.documentToAddList), // Not used when useAddOrMerge=true
        new ArrayList<>(this.documentToDeleteList),
        new ArrayList<>(this.documentToPartiallyUpdateList),
        new ArrayList<>(this.documentToShallowMergeList),
        true); // Use addOrMerge field name
  }

  @Override
  public StreamUpdate getStream() {
    throw new UnsupportedOperationException(
        "CatalogDocumentUploadQueue does not support regular getStream. Use getStreamWithShallowMerge instead.");
  }

  @Override
  public boolean isEmpty() {
    return super.isEmpty() && documentToShallowMergeList.isEmpty();
  }
}
