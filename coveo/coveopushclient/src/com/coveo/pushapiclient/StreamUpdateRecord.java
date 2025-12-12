package com.coveo.pushapiclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

public class StreamUpdateRecord extends BatchUpdateRecord {

  private final JsonObject[] partialUpdate;
  private final boolean useAddOrMerge;

  public StreamUpdateRecord(
      JsonObject[] addOrUpdate, JsonObject[] delete, JsonObject[] partialUpdate) {
    this(addOrUpdate, delete, partialUpdate, false);
  }

  /**
   * Creates a StreamUpdateRecord with optional addOrMerge mode for Catalog sources.
   *
   * @param addOrUpdateOrMerge Array of documents (field name depends on useAddOrMerge flag).
   * @param delete Array of documents to delete.
   * @param partialUpdate Array of partial update operations.
   * @param useAddOrMerge If true, serializes as "addOrMerge"; if false, as "addOrUpdate".
   */
  public StreamUpdateRecord(
      JsonObject[] addOrUpdateOrMerge,
      JsonObject[] delete,
      JsonObject[] partialUpdate,
      boolean useAddOrMerge) {
    super(addOrUpdateOrMerge, delete);
    this.partialUpdate = partialUpdate;
    this.useAddOrMerge = useAddOrMerge;
  }

  public JsonObject[] getPartialUpdate() {
    return partialUpdate;
  }

  public boolean isUseAddOrMerge() {
    return useAddOrMerge;
  }

  /**
   * Converts this record to a JsonObject with the appropriate field names.
   * For catalog sources, uses "addOrMerge"; for regular sources, uses "addOrUpdate".
   *
   * @return JsonObject representation with correct field names for the API.
   */
  public JsonObject toJsonObject() {
    JsonObject result = new JsonObject();

    // Use appropriate field name based on source type
    String addFieldName = useAddOrMerge ? "addOrMerge" : "addOrUpdate";
    JsonArray addArray = new JsonArray();
    for (JsonObject obj : this.getAddOrUpdate()) {
      addArray.add(obj);
    }
    result.add(addFieldName, addArray);

    // Add delete array
    JsonArray deleteArray = new JsonArray();
    for (JsonObject obj : this.getDelete()) {
      deleteArray.add(obj);
    }
    result.add("delete", deleteArray);

    // Add partialUpdate array
    JsonArray partialArray = new JsonArray();
    for (JsonObject obj : this.partialUpdate) {
      partialArray.add(obj);
    }
    result.add("partialUpdate", partialArray);

    return result;
  }

  @Override
  public String toString() {
    String fieldName = useAddOrMerge ? "addOrMerge" : "addOrUpdate";
    return "StreamUpdateRecord["
        + fieldName + "="
        + Arrays.toString(this.getAddOrUpdate())
        + ", delete="
        + Arrays.toString(this.getDelete())
        + ", partialUpdate="
        + Arrays.toString(partialUpdate)
        + ']';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    StreamUpdateRecord that = (StreamUpdateRecord) obj;
    return useAddOrMerge == that.useAddOrMerge
        && Arrays.equals(this.getAddOrUpdate(), that.getAddOrUpdate())
        && Arrays.equals(this.getDelete(), that.getDelete())
        && Arrays.equals(partialUpdate, that.partialUpdate);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Arrays.hashCode(partialUpdate);
    result = 31 * result + Boolean.hashCode(useAddOrMerge);
    return result;
  }
}
