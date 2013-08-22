package fileloader;

import java.util.List;

/**
 * A class that represents the JSON object for the name_configs.json.
 *
 */
public class Configuration {
  private String applicationId;
  private String projectNumber;
  private String projectId;
  private String serviceAccount;
  private String apiKey;
  private CloudStorageConfigs cloudStorageConfigs;
  private BigqueryConfigs bigqueryConfigs;

  public static class CloudStorageConfigs {
    private String channelId;

    /**
     * @return the channelId
     */
    public String getChannelId() {
      return channelId;
    }
  }

  public static class BigqueryConfigs {
    private String datasetId;
    private String tableName;
    private String delimiter;
    private List<Column> columns;
    private Integer headerLines;
    private Integer jobTimeout;
    /**
     * @return the datasetId
     */
    public String getDatasetId() {
      return datasetId;
    }
    /**
     * @return the tableName
     */
    public String getTableName() {
      return tableName;
    }
    /**
     * @return the delimiter
     */
    public String getDelimeter() {
      return delimiter;
    }
    /**
     * @return the columns
     */
    public List<Column> getColumns() {
      return columns;
    }
    /**
     * @return the headerLines
     */
    public Integer getHeaderLines() {
      return headerLines;
    }
    /**
     * @return the jobTimeout
     */
    public Integer getJobTimeout() {
      return jobTimeout;
    }
  }

  public static class Column {
    private String name;
    private String type;
    /**
     * @return the name
     */
    public String getName() {
      return name;
    }
    /**
     * @return the type
     */
    public String getType() {
      return type;
    }
  }

  /**
   * @return the applicationId
   */
  public String getApplicationId() {
    return applicationId;
  }
  /**
   * @return the projectNumber
   */
  public String getProjectNumber() {
    return projectNumber;
  }
  /**
   * @return the projectId
   */
  public String getProjectId() {
    return projectId;
  }
  /**
   * @return the serviceAccount
   */
  public String getServiceAccount() {
    return serviceAccount;
  }
  /**
   * @return the apiKey
   */
  public String getApiKey() {
    return apiKey;
  }
  /**
   * @return the cloudStorageConfigs
   */
  public CloudStorageConfigs getCloudStorageConfigs() {
    return cloudStorageConfigs;
  }
  /**
   * @return the bigqueryConfigs
   */
  public BigqueryConfigs getBigqueryConfigs() {
    return bigqueryConfigs;
  }
}
