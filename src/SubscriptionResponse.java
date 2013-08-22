package fileloader;

/**
 * A class that represents the JSON subscription watch response.
 *
 */
public class SubscriptionResponse {

  private String kind;
  private String id;
  private String resourceId;
  private String resourceUri;
  private String token;

  /**
   * @return the kind
   */
  public String getKind() {
    return kind;
  }

  /**
   * @param kind the kind to set
   */
  public void setKind(String kind) {
    this.kind = kind;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    this.id = id;
  }

  /**
   * @return the resourceId
   */
  public String getResourceId() {
    return resourceId;
  }

  /**
   * @param resourceId the resourceId to set
   */
  public void setResourceId(String resourceId) {
    this.resourceId = resourceId;
  }

  /**
   * @return the resourceUri
   */
  public String getResourceUri() {
    return resourceUri;
  }

  /**
   * @param resourceUri the resourceUri to set
   */
  public void setResourceUri(String resourceUri) {
    this.resourceUri = resourceUri;
  }

  /**
   * @return the token
   */
  public String getToken() {
    return token;
  }

  /**
   * @param token the token to set
   */
  public void setToken(String token) {
    this.token = token;
  }

}
