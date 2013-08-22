package fileloader;

import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
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
 * A class which handles unsubscribe requests from the user.
 *
 */
public class UnsubscriptionServlet extends HttpServlet {
  private static final Logger log = Logger.getLogger(UnsubscriptionServlet.class.getName());

  /**
   * Unsubscribes all subscriptions that this project has from a certain bucket.
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
    final StringBuilder unsubscribeUrl =
        new StringBuilder(Constants.SUBSCRIBE_BASE_URL);
    unsubscribeUrl.append("channels/stop?key=");
    unsubscribeUrl.append(configs.getApiKey());

    JsonObject json = new JsonObject();
    String resourceId = null;
    try {
      resourceId = SubscriptionUtils.getSubscriptionResourceId(bucketName, channelId);
    } catch (EntityNotFoundException e) {
      log.warning(
          "Record of this channel watch is not found. Attempting to stop channel watch anyway.");
    }
    json.addProperty("resourceId", resourceId);
    json.addProperty("id", channelId);

    List<String> scopes = new ArrayList<String>();
    scopes.add(SubscriptionUtils.NOTIFICATIONS_SCOPE);
    AppIdentityService.GetAccessTokenResult result = appIdService.getAccessToken(scopes);
    String accessToken = result.getAccessToken();

    // Create HTTPRequest and set headers
    HTTPRequest httpRequest = new HTTPRequest(new URL(unsubscribeUrl.toString()), HTTPMethod.POST);
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
      log.info("Unsubscribed from resource " + resourceId + " on channel ID " + channelId);
      resp.getWriter().println("Successfully unsubscribed from: " + bucketName
          + " with channel ID: " + channelId + ".");
    } else {
      log.warning(
          "Failed to unsubscribe from resource " + resourceId + " on channel ID " + channelId);
      log.warning("Response content: " + new String(httpResponse.getContent()));
      resp.getWriter().println(
          "Failed to unsubscribe from: " + bucketName + " with response code " + responseCode);
    }

    try {
      SubscriptionUtils.deactivateSubscription(bucketName, channelId);
    } catch (EntityNotFoundException e) {
      // TODO: handle this exception here
    }
  }
}
