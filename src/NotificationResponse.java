package fileloader;

/**
 * A class that represents the JSON object for the object change notification response.
 *
 */
public class NotificationResponse {
  private String kind;
  private String id; // bucketname/newobject
  private String selfLink;
  private String name;
  private String bucket;
  private Owner owner;

  public static class Owner {
    private String entity;
    private String entityId;
  }

  /**
   * Returns the ID of the new object. Format of the ID is {bucketname}/{new object name}.
   */
  public String getNewObjectFullId() {
    return id;
  }

  /**
   * Get name of the Cloud Storage bucket.
   */
  public String getBucketName() {
    return bucket;
  }

  /**
   * Get the name of the new object in the Cloud Storage bucket.
   */
  public String getNewObjectName() {
    return name;
  }
}
