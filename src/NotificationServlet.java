package fileloader;

import com.google.appengine.api.backends.BackendServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class which handles incoming notifications from Cloud Storage.
 *
 */
public class NotificationServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(NotificationServlet.class.getName());

  /**
   * Processes object change notifications and adds them to a TaskQueue for processing into
   * Bigquery.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    String resourceState = req.getHeader("X-Goog-Resource-State");
    String resourceId = req.getHeader("X-Goog-Resource-Id");
    String channelId = req.getHeader("X-Goog-Channel-Id");

    if (resourceState.equalsIgnoreCase(SubscriptionUtils.NotificationEventType.EXISTS.toString()) ||
          resourceState.equalsIgnoreCase(
            SubscriptionUtils.NotificationEventType.NOT_EXISTS.toString())) {
      // Get notification
      Gson gson = new GsonBuilder().create();
      NotificationResponse notification =
          gson.fromJson(req.getReader(), NotificationResponse.class);
      if (notification != null) {
        String fullId = notification.getNewObjectFullId();
        log.info("Notification for channel ID " + channelId + " received for file " + fullId
            + " at resource ID " + resourceId);
        if (resourceState.equalsIgnoreCase(
            SubscriptionUtils.NotificationEventType.EXISTS.toString())) {
          // Add the task to load new data to the task queue
          Queue queue = SubscriptionUtils.getTaskQueueForBigqueryLoader();
          TaskOptions taskOptions =
              TaskOptions.Builder.withUrl(Constants.BIGQUERY_URL)
              .param("bucket", notification.getBucketName())
              .param("name", notification.getNewObjectName())
              .header("Host", BackendServiceFactory.getBackendService()
                  .getBackendAddress(Constants.BACKEND_ADDRESS))
              .method(TaskOptions.Method.POST);
          queue.add(taskOptions);
          log.info("Task to load new data from [" + fullId
              + "] to Bigquery has been added to the queue");
        } else if (resourceState.equalsIgnoreCase(
            SubscriptionUtils.NotificationEventType.NOT_EXISTS.toString())) {
          log.info("The file [" + fullId + "] has been removed from the bucket");
        }
      } else {
        log.log(Level.SEVERE, "Notification for channel_id [" + channelId
            + "] not processed. Please check your files.");
      }
    } else if (resourceState.equalsIgnoreCase(
                 SubscriptionUtils.NotificationEventType.SYNC.toString())) {
      log.info("Channel with channel_id: " + channelId + " is now synched.");
    }
  }
}
