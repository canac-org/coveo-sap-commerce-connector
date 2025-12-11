package com.coveo.pushapiclient;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a shallow merge operation for Coveo Catalog sources.
 * 
 * <p>A shallow merge operation updates one or more fields in an item. If the item doesn't exist,
 * it will be created. If a specified field is missing, it will be added.
 * 
 * <p>Important: When performing a shallow merge on an existing dictionary field, the entire value
 * of the field is replaced. Use partial update operations (dictionaryPut/dictionaryRemove) instead
 * to append or remove individual values within a dictionary field without overwriting existing data.
 * 
 * @see <a href="https://docs.coveo.com/en/p4eb0515/coveo-for-commerce/partial-catalog-data-updates#shallow-merge-operations">Shallow Merge Operations</a>
 */
public class ShallowMergeDocument {

  /** The documentId of the document to merge. */
  public String documentId;

  /** The fields and their values to merge into the document. */
  public Map<String, Object> fields;

  /**
   * Creates a new ShallowMergeDocument.
   *
   * @param documentId The id of the document to merge.
   */
  public ShallowMergeDocument(String documentId) {
    if (documentId == null || documentId.trim().isEmpty()) {
      throw new IllegalArgumentException("DocumentId cannot be null or empty");
    }
    this.documentId = documentId;
    this.fields = new HashMap<>();
  }

  /**
   * Creates a new ShallowMergeDocument with initial fields.
   *
   * @param documentId The id of the document to merge.
   * @param fields The fields and their values to merge.
   */
  public ShallowMergeDocument(String documentId, Map<String, Object> fields) {
    if (documentId == null || documentId.trim().isEmpty()) {
      throw new IllegalArgumentException("DocumentId cannot be null or empty");
    }
    if (fields == null) {
      throw new IllegalArgumentException("Fields cannot be null");
    }
    this.documentId = documentId;
    this.fields = new HashMap<>(fields);
  }

  /**
   * Adds a field to be merged into the document.
   *
   * @param fieldName The name of the field.
   * @param value The value of the field.
   * @return This ShallowMergeDocument instance for method chaining.
   */
  public ShallowMergeDocument withField(String fieldName, Object value) {
    if (fieldName == null || fieldName.trim().isEmpty()) {
      throw new IllegalArgumentException("Field name cannot be null or empty");
    }
    this.fields.put(fieldName, value);
    return this;
  }

  /**
   * Adds multiple fields to be merged into the document.
   *
   * @param fields The fields and their values to merge.
   * @return This ShallowMergeDocument instance for method chaining.
   */
  public ShallowMergeDocument withFields(Map<String, Object> fields) {
    if (fields != null) {
      this.fields.putAll(fields);
    }
    return this;
  }

  /**
   * Marshals this shallow merge document into a JsonObject for the Coveo API.
   *
   * @return The JsonObject representation of this shallow merge document.
   */
  public JsonObject marshalJsonObject() {
    JsonObject jsonObject = new JsonObject();
    jsonObject.addProperty("documentId", this.documentId);
    
    // Add all fields to the JSON object
    Gson gson = new Gson();
    for (Map.Entry<String, Object> entry : this.fields.entrySet()) {
      jsonObject.add(entry.getKey(), gson.toJsonTree(entry.getValue()));
    }
    
    return jsonObject;
  }

  @Override
  public String toString() {
    return "ShallowMergeDocument[" +
        "documentId='" + documentId + '\'' +
        ", fields=" + fields +
        ']';
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null || getClass() != obj.getClass()) return false;
    ShallowMergeDocument that = (ShallowMergeDocument) obj;
    return documentId.equals(that.documentId) && fields.equals(that.fields);
  }

  @Override
  public int hashCode() {
    int result = documentId.hashCode();
    result = 31 * result + fields.hashCode();
    return result;
  }
}
