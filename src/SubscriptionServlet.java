package fileloader;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A class which handles Cloud Storage subscription actions.
 *
 */
public class SubscriptionServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(SubscriptionServlet.class.getName());

  /**
   * Subscribes this project to a specific bucket.
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // Define information needed for HTTPRequest
    final String bucketName = req.getParameter("bucket_name");
    if (bucketName == null || bucketName.isEmpty()) {
      resp.getWriter().println("Please specify the bucket.");
      return;
    }

    final AppIdentityService appIdService = AppIdentityServiceFactory.getAppIdentityService();

    Configuration configs = SubscriptionUtils.getConfigs();

    final String channelId = configs.getCloudStorageConfigs().getChannelId();
    final String callbackUrl =
        "https://" + req.getServerName() + Constants.NOTIFICATION_URL;

    final StringBuilder subscribeUrl =
        new StringBuilder(Constants.SUBSCRIBE_BASE_URL);
    subscribeUrl.append("b/");
    subscribeUrl.append(bucketName);
    subscribeUrl.append("/o/watch?alt=json&key=");
    subscribeUrl.append(configs.getApiKey());

    JsonObject json = new JsonObject();
    json.addProperty("type", "web_hook");
    json.addProperty("id", channelId);
    json.addProperty("address", callbackUrl);

    List<String> scopes = new ArrayList<String>();
    scopes.add(SubscriptionUtils.NOTIFICATIONS_SCOPE);
    AppIdentityService.GetAccessTokenResult result = appIdService.getAccessToken(scopes);
    String accessToken = result.getAccessToken();

    // Create HTTPRequest and set headers
    HTTPRequest httpRequest = new HTTPRequest(new URL(subscribeUrl.toString()), HTTPMethod.POST);
    httpRequest.addHeader(new HTTPHeader("Authorization", "OAuth " + accessToken));
    httpRequest.addHeader(new HTTPHeader("Host", "www.googleapis.com"));
    httpRequest.addHeader(
        new HTTPHeader("Content-Length", String.valueOf(json.toString().length())));
    httpRequest.addHeader(new HTTPHeader("Content-Type", "application/json"));
    httpRequest.addHeader(new HTTPHeader("User-Agent", "google-api-java-client/1.0"));
    httpRequest.setPayload(json.toString().getBytes());

    // Send request
    URLFetchService fetcher = URLFetchServiceFactory.getURLFetchService();
    HTTPResponse httpResponse = fetcher.fetch(httpRequest);
    int responseCode = httpResponse.getResponseCode();
    if (responseCode == 200 || responseCode == 204) { // OK || No Content
      Gson gson = new GsonBuilder().create();
      SubscriptionResponse notification =
          gson.fromJson(new String(httpResponse.getContent()), SubscriptionResponse.class);
      SubscriptionUtils.addSubscription(bucketName, channelId, notification.getResourceId());
      log.info("Subscribed with channel ID " + channelId);
      resp.getWriter().println("Successfully subscribed to: " + bucketName + ", with channel ID: "
          + channelId + ". Check the Datastore to see your entry.");
    } else if (responseCode == 404) {
      resp.getWriter().println("Failed to subscribe to: " + bucketName
          + ". This bucket either does not exist or you do not have permission to access it.");
    } else {
      resp.getWriter().println("Failed to subscribe to: " + bucketName
          + " with response code " + responseCode);
    }
  }
}
