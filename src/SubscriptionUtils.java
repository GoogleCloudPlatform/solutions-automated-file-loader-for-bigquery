package fileloader;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Date;

/**
 * A utility and constants class for subscription-related uses.
 *
 */
public class SubscriptionUtils {

  private static Configuration configs;

  public static final String NOTIFICATIONS_SCOPE =
      "https://www.googleapis.com/auth/devstorage.full_control";

  /**
   * Event types for Cloud Storage bucket notification events.
   *
   * SYNC: bucket is synched with subscription
   * EXISTS: object is inserted into bucket or updated in bucket
   * NOT_EXISTS: object is removed from bucket
   */
  public static enum NotificationEventType {
    SYNC, EXISTS, NOT_EXISTS
  }

  private static DatastoreService DATASTORE_SINGLETON =
      DatastoreServiceFactory.getDatastoreService();

  public static DatastoreService getDatastore() {
    return DATASTORE_SINGLETON;
  }

  public static Configuration getConfigs() throws FileNotFoundException {
    if (configs == null) {
      configs = new GsonBuilder().create().fromJson(
          new FileReader(Constants.CONFIG_FILE_LOCATION), Configuration.class);
    }
    return configs;
  }

  /**
   * Returns the bigqueryloader-queue {@link Queue}.
   */
  public static Queue getTaskQueueForBigqueryLoader() {
    return QueueFactory.getQueue("bigqueryloader-queue");
  }

  /**
   * Creates a Key for a Subscription Entity for storage in the Datastore. Use this method to
   * consistently form keys for the entity.
   *
   * @param channelId the subscription ID
   * @return a new {@link Key} for the Subscription
   */
  private static Key createDatastoreKeyForSubscription(
      final String bucketName, final String channelId) {
    return KeyFactory.createKey("Subscription", bucketName + "|" + channelId);
  }

  /**
   * Adds new subscription information to the Datastore.
   *
   * @param channelId the subscription ID of the new subscription
   * @param bucketName the GCS bucket that is subscribed to
   */
  public static void addSubscription(
      final String bucketName, final String channelId, final String resourceId) {
    Key subscriptionKey = createDatastoreKeyForSubscription(bucketName, channelId);
    Entity notificationSubscriptions = new Entity(subscriptionKey);
    notificationSubscriptions.setProperty("gcs_bucket", bucketName);
    notificationSubscriptions.setProperty("channel_id", channelId);
    notificationSubscriptions.setProperty("resource_id", resourceId);
    notificationSubscriptions.setProperty("active", new Boolean(true));
    notificationSubscriptions.setProperty("timestamp", new Date().toString());
    DATASTORE_SINGLETON.put(notificationSubscriptions);
  }

  /**
   * Adds new subscription information to the Datastore.
   *
   * @param channelId the subscription ID of the new subscription
   * @param bucketName the GCS bucket that is subscribed to
   */
  public static String getSubscriptionResourceId(final String bucketName, final String channelId)
      throws EntityNotFoundException {
    Key subscriptionKey = createDatastoreKeyForSubscription(bucketName, channelId);
    Entity notificationSubscriptions = DATASTORE_SINGLETON.get(subscriptionKey);
    return (String) notificationSubscriptions.getProperty("resource_id");
  }

  /**
   * Updates the active value in Datastore for the record.
   *
   * @param bucketName the name of the bucket
   * @param channelId the ID of the channel that has been deactivated
   * @throws EntityNotFoundException if the subscription was not found
   */
  public static void deactivateSubscription(final String bucketName, final String channelId)
      throws EntityNotFoundException {
    Entity subscription =
        DATASTORE_SINGLETON.get(createDatastoreKeyForSubscription(bucketName, channelId));
    subscription.setProperty("active", new Boolean(false));
    DATASTORE_SINGLETON.put(subscription);
  }
}
