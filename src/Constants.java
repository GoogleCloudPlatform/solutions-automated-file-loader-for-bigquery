package fileloader;

/**
 * Utility and constants class for general project.
 *
 */
public class Constants {
  public static final String NOTIFICATION_URL = "/notify";
  public static final String BIGQUERY_URL = "/bigquery";
  public static final String CONFIG_FILE_LOCATION = "WEB-INF/name_configs.json";
  public static final String BACKEND_ADDRESS = "bigqueryloader";

  // Auth constants
  public static final String BQ_SCOPE = "https://www.googleapis.com/auth/bigquery";

  // GCS constants
  private static final String HOST = "https://www.googleapis.com/storage/";
  private static final String API_VERSION = "v1beta2/";
  public static final String SUBSCRIBE_BASE_URL = HOST + API_VERSION;
}
