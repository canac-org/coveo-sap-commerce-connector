package com.coveo.pushapiclient;

import com.google.gson.JsonObject;

import java.util.Collections;
import java.util.List;

public class StreamUpdate extends BatchUpdate {

  private final List<PartialUpdateDocument> partialUpdate;
  private final List<ShallowMergeDocument> shallowMerge;
  private final boolean useAddOrMerge;

  public StreamUpdate(
      List<DocumentBuilder> addOrUpdate,
      List<DeleteDocument> delete,
      List<PartialUpdateDocument> partialUpdate) {
    this(addOrUpdate, delete, partialUpdate, Collections.emptyList(), false);
  }

  /**
   * Creates a StreamUpdate with shallow merge support for Catalog sources.
   *
   * @param addOrUpdate List of documents to add or update (ignored if useAddOrMerge is true).
   * @param delete List of documents to delete.
   * @param partialUpdate List of partial update operations.
   * @param shallowMerge List of shallow merge documents (for catalog sources only).
   * @param useAddOrMerge If true, uses addOrMerge instead of addOrUpdate in the payload.
   */
  public StreamUpdate(
      List<DocumentBuilder> addOrUpdate,
      List<DeleteDocument> delete,
      List<PartialUpdateDocument> partialUpdate,
      List<ShallowMergeDocument> shallowMerge,
      boolean useAddOrMerge) {
    super(addOrUpdate, delete);
    this.partialUpdate = partialUpdate;
    this.shallowMerge = shallowMerge != null ? shallowMerge : Collections.emptyList();
    this.useAddOrMerge = useAddOrMerge;
  }

  @Override
  public StreamUpdateRecord marshal() {
    if (useAddOrMerge) {
      // For catalog sources, use addOrMerge instead of addOrUpdate
      return new StreamUpdateRecord(
          this.shallowMerge.stream()
              .map(ShallowMergeDocument::marshalJsonObject)
              .toArray(JsonObject[]::new),
          this.getDelete().stream()
              .map(DeleteDocument::marshalJsonObject)
              .toArray(JsonObject[]::new),
          this.partialUpdate.stream()
              .map(PartialUpdateDocument::marshalJsonObject)
              .toArray(JsonObject[]::new),
          true); // Use addOrMerge field name
    } else {
      // For regular sources, use addOrUpdate
      return new StreamUpdateRecord(
          this.getAddOrUpdate().stream()
              .map(DocumentBuilder::marshalJsonObject)
              .toArray(JsonObject[]::new),
          this.getDelete().stream()
              .map(DeleteDocument::marshalJsonObject)
              .toArray(JsonObject[]::new),
          this.partialUpdate.stream()
              .map(PartialUpdateDocument::marshalJsonObject)
              .toArray(JsonObject[]::new),
          false); // Use addOrUpdate field name
    }
  }

  public List<PartialUpdateDocument> getPartialUpdate() {
    return partialUpdate;
  }

  public List<ShallowMergeDocument> getShallowMerge() {
    return shallowMerge;
  }

  public boolean isUseAddOrMerge() {
    return useAddOrMerge;
  }

  @Override
  public String toString() {
    if (useAddOrMerge) {
      return "StreamUpdate["
          + "addOrMerge="
          + shallowMerge
          + ", delete="
          + getDelete()
          + ", partialUpdate="
          + partialUpdate
          + ']';
    } else {
      return "StreamUpdate["
          + "addOrUpdate="
          + getAddOrUpdate()
          + ", delete="
          + getDelete()
          + ", partialUpdate="
          + partialUpdate
          + ']';
    }
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    StreamUpdate that = (StreamUpdate) obj;
    return useAddOrMerge == that.useAddOrMerge
        && getAddOrUpdate().equals(that.getAddOrUpdate())
        && getDelete().equals(that.getDelete())
        && partialUpdate.equals(that.partialUpdate)
        && shallowMerge.equals(that.shallowMerge);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + partialUpdate.hashCode();
    result = 31 * result + shallowMerge.hashCode();
    result = 31 * result + Boolean.hashCode(useAddOrMerge);
    return result;
  }
}
