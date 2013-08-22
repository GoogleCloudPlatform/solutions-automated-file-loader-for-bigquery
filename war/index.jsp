<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
  <head>
  </head>
  <body>

    <h1>Welcome to the Automated File Loader for BigQuery sample GAE Java application.</h1>
    <h3>Watching a Bucket</h3>
    You can tell your application to "watch" a Google Cloud Storage bucket. When you click "Start Watching", your application sends a request to watch the specified Google Cloud Storage bucket.
    <br>
    Included in this request is a callback URL.
    <br>
    <h3>File Loading</h3>
    Use gsutil to easily upload a file to your bucket. Use the sample file included in the sample package. If you are going to upload a different file with a different schema, make sure that file schema matches the schema that you specify and deploy in name_configs.json.
    <br>
    $ gsutil cp {sample code location}/person_data.csv gs://your-bucket-name
    <br>
    In this sample, new files and updated files are treated the same way and either type of update will trigger full file ingestion into Bigquery.
    <br>
    Google Cloud Storage uses the callback URL to notify your application that the bucket has been updated.
    <br>
    Watch the progress by checking out the Logs, and Task Queues on the left-hand navigation bar in your <a href="https://appengine.google.com/">App Engine web UI</a>. You can see the final result in <a href="https://bigquery.cloud.google.com">BigQuery</a>.
    <br><br>

    <h2>Try it!</h2>
    <div><label>Enter an existing bucket name (if your bucket is gs://your-bucket-name, enter "your-bucket-name"): </label></div>
    <form action="/subscribe" method="post">
      <div><input type="text" name="bucket_name" rows="1" cols="60"></input></div>
      <div><input type="submit" value="Start Watching"></div>
    </form>
    <form action="/unsubscribe" method="post">
      <div><input type="text" name="bucket_name" rows="1" cols="60"></input></div>
      <div><input type="submit" value="Stop Watching"></div>
    </form>

  </body>
</html>
