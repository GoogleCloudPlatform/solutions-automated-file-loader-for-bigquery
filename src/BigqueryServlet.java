package fileloader;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.googleapis.services.json.AbstractGoogleJsonClientRequest;
import com.google.api.client.googleapis.services.json.CommonGoogleJsonClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.Bigquery.Jobs.Insert;
import com.google.api.services.bigquery.BigqueryRequest;
import com.google.api.services.bigquery.model.ErrorProto;
import com.google.api.services.bigquery.model.Job;
import com.google.api.services.bigquery.model.JobConfiguration;
import com.google.api.services.bigquery.model.JobConfigurationLoad;
import com.google.api.services.bigquery.model.JobReference;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableReference;
import com.google.api.services.bigquery.model.TableSchema;

import fileloader.Configuration.Column;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Class that handles data loading into Bigquery.
 *
 */
public class BigqueryServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(BigqueryServlet.class.getName());

  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();

  private Bigquery bigquery = null;

  private Configuration configs;

  /**
   * Returns the singleton instance of Bigquery, or instantiates one if it does not already exist.
   */
  private Bigquery getBigquery() {
    if (bigquery == null) {
      AppIdentityCredential credential =
          new AppIdentityCredential.Builder(Constants.BQ_SCOPE).build();
      GoogleClientRequestInitializer initializer = new CommonGoogleJsonClientRequestInitializer() {
        public void initialize(AbstractGoogleJsonClientRequest request) {
          BigqueryRequest bigqueryRequest = (BigqueryRequest) request;
          bigqueryRequest.setPrettyPrint(true);
        }
      };
      bigquery = new Bigquery.Builder(
          HTTP_TRANSPORT, JSON_FACTORY, credential).setHttpRequestInitializer(credential)
          .setGoogleClientRequestInitializer(initializer).build();
    }
    return bigquery;
  }

  private TableReference tableRef;
  private TableSchema tableSchema;

  public BigqueryServlet() throws FileNotFoundException {
    super();
    tableRef = new TableReference();
    tableSchema = new TableSchema();
    try {
      configs = SubscriptionUtils.getConfigs();
    } catch (FileNotFoundException e) {
      throw new FileNotFoundException(
          "Your config file could not be found. Please check location." + e);
    }
    // Describe the schema of the destination table in Bigquery
    List<TableFieldSchema> fields = new ArrayList<TableFieldSchema>();
    TableFieldSchema tableField;
    for (Column column : configs.getBigqueryConfigs().getColumns()) {
      tableField = new TableFieldSchema().setName(column.getName()).setType(column.getType());
      fields.add(tableField);
    }
    tableSchema.setFields(fields);
  }

  /**
   * Loads the new file from the Cloud Storage bucket (specified in the request) into Bigquery.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String bucketName = req.getParameter("bucket");
    String filename = req.getParameter("name");

    // Create Job and set configs
    Job job = new Job();
    JobConfiguration jobConfig = new JobConfiguration();
    JobConfigurationLoad loadConfig = new JobConfigurationLoad();
    jobConfig.setLoad(loadConfig);
    job.setConfiguration(jobConfig);

    loadConfig.setAllowQuotedNewlines(false);
    loadConfig.setFieldDelimiter(configs.getBigqueryConfigs().getDelimeter());
    loadConfig.setCreateDisposition("CREATE_IF_NEEDED"); // default
    loadConfig.setWriteDisposition("WRITE_APPEND"); // default
    loadConfig.setSkipLeadingRows(configs.getBigqueryConfigs().getHeaderLines());

    // Set GCS path as a source
    List<String> sources = new ArrayList<String>();
    String fullFilename = "gs://" + bucketName + "/" + filename;
    log.info("GCS source: " + fullFilename);
    sources.add(fullFilename);
    loadConfig.setSourceUris(sources);

    // Describe the destination table in Bigquery
    tableRef.setDatasetId(configs.getBigqueryConfigs().getDatasetId());
    tableRef.setTableId(configs.getBigqueryConfigs().getTableName());
    tableRef.setProjectId(configs.getProjectId());
    loadConfig.setDestinationTable(tableRef);

    // Set table schema
    loadConfig.setSchema(tableSchema);

    // Set up Bigquery Insert
    Insert insert = getBigquery().jobs().insert(configs.getProjectId(), job);
    insert.setProjectId(configs.getProjectId());
    log.info("Full JSON content: " + insert.getJsonContent());

    // Execute Insert
    JobReference jobRef = insert.execute().getJobReference();
    log.info("Job ID is: " + jobRef.getJobId());

    // Wait for Job to finish, then print any error-related information
    try {
      Job completedJob = pollJobStatus(jobRef, configs.getProjectId());
      if (completedJob == null) {
        throw new IOException();
      }
      log.info("Job with Job ID " + completedJob.getJobReference().getJobId() + " is complete.");
      ErrorProto fatalError = completedJob.getStatus().getErrorResult();
      List<ErrorProto> errors = completedJob.getStatus().getErrors();
      if (fatalError != null) {
        log.severe("Job failed while writing to Bigquery. " + fatalError.getReason() + ": "
            + fatalError.getMessage() + " at " + fatalError.getLocation());
      }
      if (errors != null) {
        for (ErrorProto error : errors) {
          log.log(Level.SEVERE, "Error: [REASON] " + error.getReason() + " [MESSAGE] "
              + error.getMessage() + " [LOCATION] " + error.getLocation());
        }
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    } catch (InterruptedException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
  }

  /**
   * Polls the status of the job every 1000 ms to check for completion.
   * Times out after set job timeout.
   *
   * @param jobRef the JobReference of the Job to be polled
   * @param projectId the project ID that this Bigquery instance is enabled for
   * @return the Job that was being polled
   * @throws IOException
   * @throws InterruptedException
   */
  private Job pollJobStatus(JobReference jobRef, String projectId)
      throws IOException, InterruptedException {
    long startTime = System.currentTimeMillis();
    long elapsedTime = 0;

    while (System.currentTimeMillis() - startTime < configs.getBigqueryConfigs().getJobTimeout()) {
      Job pollJob = getBigquery().jobs().get(projectId, jobRef.getJobId()).execute();
      elapsedTime = System.currentTimeMillis() - startTime;
      log.info("Job status: " + pollJob.getStatus().getState());
      if (pollJob.getStatus().getState().equals("DONE")) {
        return pollJob;
      }
      Thread.sleep(1000);
    }
    log.warning("Bigquery job was not able to be completed.");
    return null;
  }
}
