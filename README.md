Automated Data Streaming to BigQuery
====================================

Copyright
---------

Copyright 2013 Google Inc. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


Disclaimer
----------

This sample application is not an official Google product.


Summary
-------
This package provides a complete end-to-end sample solution that uses the Object Change Notification service on Google Cloud Storage. It shows how you can easily build a simple App Engine application to automatically pick up new data in Cloud Storage based on your business logic, and load it directly to Google BigQuery.


Supported Components
--------------------

Languages:
Java

Google Cloud Platform:
App Engine
Cloud Storage
BigQuery

Downloads
---------

Download the sample code. Notice that there are two main directories: the src/ and the war/. The src/ folder contains backend Java code. The war/ folder contains frontend code and other configurations for your application.

App Engine development is very quick and easy on Eclipse, because you get to use the nice Google Plugin. Download the following:

1) Make sure you have Java installed.
2) Download and set up Eclipse.
3) Download the [App Engine Java SDK] (http://googleappengine.googlecode.com/files/appengine-java-sdk-1.8.0.zip)
4) Download the [Google Plugin for Eclipse] (https://developers.google.com/eclipse/docs/getting_started) for your IDE version. Set it up using the instructions on that page.


Setting up the Application
--------------------------

This sample requires that you have projects on App Engine and the API Console. If you already have existing projects, make sure your APIs project has the following services enabled:

* BigQuery API
* Google Cloud Storage
* Google Cloud Storage JSON API

Also make sure that your App Engine project has permissions to your APIs project.

If you are unsure that you have these, follow the instructions below. The following sections assume that you are creating projects from scratch. Make sure that you have done all the things that are specified.

#### App Engine Project

Note: If you have not developed applications using App Engine before, it may be beneficial (though not strictly necessary) to skim through the [Getting Started Guide] (https://developers.google.com/appengine/docs/java/gettingstarted/).

1) Create a new App Engine project by clicking on the Google Plugin icon in the upper left hand corner of your Eclipse IDE. Select "New Web Application Project". This will set you up with all of the JARs and libraries that you need. The Project and Package name should both be "fileloader". Uncheck "Use Google Web Toolkit" and "Generate project sample code".

2) From the downloaded sample code, copy the files from src/ into your project's src/ folder.

3) From the downloaded sample code, copy the files from war/ into your project's war/ folder. It will ask whether you want to overwrite some of the files such as appengine-web.xml. Click "Yes to all".

4) Add the Google BigQuery API. You can do this by clicking on the Google Plugin icon and selecting "Add Google APIs...". In the new window that pops up, search for "BigQuery API" and select it. Click Finish. This will add the BigQuery library to your project. It will also add the necessary JARs to your war/WEB-INF/lib/ folder, so that they can be deployed with your App Engine app.

#### API Console

Now that you have created a new project, you will need to check its API settings. Go to the [the API console] (https://code.google.com/apis/console/) and select your project.

1) Make sure the following Services are enabled:

* BigQuery API
* Google Cloud Storage
* Google Cloud Storage JSON API

2) Under API Access, create a new client ID, or create another client ID if you already have one. Choose the Service account option. After it's been created, take note of the Email Address value. You will need this later on.

3) Under API Access, create a Simple API Access key for browser apps. After it's been created, take note of the API key. You will need this for your application to access the services that you enabled in this project.

4) Under API Access, you will need to add your App Engine app to the Allowed Domains in Notification Endpoints. You won't be able to do this yet; you need to deploy your application first. There will be a section below to deal with this later.

5) Finally, remember to enable Billing.

#### Cloud Storage

There are several ways to set up and configure Cloud Storage, and you can find more info on the developer's portal. Otherwise, do the following:

1) Install [gsutil] (https://devsite.googleplex.com/storage/docs/gsutil_install). If you have previously used gsutil with another project, run the following command to reconfigure the tool to your project:
* $ gsutil config
2) Create a bucket, or several. Name them however you wish.
* $ gsutil mb gs://your-new-bucket
3) Modify the ACLs for your new bucket. NOTE: you can read more about access control [here] (https://developers.google.com/storage/docs/accesscontrol), but this sample will use default object ACLs.
* $ gsutil getdefacl gs://your-new-bucket/ > /tmp/acls.txt
* Add a new <Entry>:
<Entry>
   <Scope type="UserByEmail">
      <EmailAddress>
         your-app-id@appspot.gserviceaccount.com
      </EmailAddress>
   </Scope>
   <Permission>
      FULL_CONTROL
   </Permission>
</Entry>
* $ gsutil setdefacl /tmp/acls.txt gs://your-new-bucket/

Currently, with the way the project is set up, your bucket location will default to servers in the US. To specify a custom bucket location, follow the instructions [here] (https://developers.google.com/storage/docs/concepts-techniques#specifyinglocations).

#### BigQuery

Create a new dataset for your BigQuery service. You can do so at the [web UI] (https://bigquery.cloud.google.com). You do not need to create a new table.

Updating Configurations in Eclipse
----------------------------------

#### JSON config file

Edit war/WEB-INF/name_configs.json. This file contains the configurations about your application and project information that is required by the sample application. All values should be string values, and surrounded with double quotes. For example:

"applicationId": "myappid"

The first 5 values you will need to pull from your App Engine and API projects.

applicationId: Your Application Identifier from your App Engine app.
projectNumber: Your Project Number in the Overview tab of your API console.
projectId: Your Project ID can be found in the same place as the project number.
serviceAccount: This is the Email Address for your client ID that you created in your API project, under API Access.
apiKey: Your API project's API key can be found under API Access.

Under cloudStorageConfigs:
channelId: This is the string ID of the channel you want your project to watch; the channel your Cloud Storage bucket will notify. Since this sample will simply have one project watch one bucket on one channel, specify any ID.

Under bigqueryConfigs:
datasetId: You should update this value to the ID of the new dataset you created for BigQuery in the Bigquery step above.

You do not need to modify any other values to run this sample.

#### appengine-web.xml

1) Update the <application> tag to your own application ID, so that it looks like this: <application>myproject</application>

2) Verify ownership of your app domain. Go to the [Webmaster Tools] (https://www.google.com/webmasters/tools/home?hl=en) and click on "Add a Site". Enter your domain: your-app-id.appspot.com. On the next page, follow the first step by downloading the HTML verification file. Move this verification file into the war/ directory of your application. In your appengine-web.xml file, add the following snippet (take care to replace the HTML file name with the one you downloaded) anywhere inside of the <appengine-web-app> tags:
<appengine-web-app>
  ...
  <static-files>
      <include path="the-name-of-your-downloaded-html-file.html" />
  </static-files>
  ...
</appengine-web-app>
Keep the Webmaster Tools page open; the rest of the steps on that page will be explained in the next section.

Deployment
----------

Deploy your App Engine application by right-clicking your project in Eclipse and selecting Google --> Deploy to App Engine. This takes care of step #2 on the Webmaster Tools page. Now complete the rest of the steps on that page to verify your application.

#### API Console, Again

In the first API Console section above, you had to wait to add Allowed Domains to your Notification Endpoints. The time has come now to do so. Follow the directions [here] (https://developers.google.com/storage/docs/object-change-notification#_Authorization).

#### Running the app

Go to the landing page at your-app-id.appspot.com for further details and instructions.

Further Implementation
----------------------

For purposes of this demonstration, this tool provides a simple framework. As you play around with the sample, you can modify it and build upon it to suit your needs.

For example, you can add data validation steps to the process, to ensure that the data written to Google Bigquery is clean. You could update the application to write to several different Google Bigquery tables, depending on data structure. Perhaps you could try to add some data processing with MapReduce, as well.

Reading
-------

[Google Developer Documentation] (https://developers.google.com/storage/docs/object-change-notification)
