## If you encounter any errors please open an Issue

# EventTure Event Discovery App

# How to setup

## In Android Studio

1. On File -> New -> Project from Version Control.
2. Copy and paste the .git url from GitHub.
3. Get a gradle build.

### Update google-services.json

1. This project uses a Firebase Firestore to manage its data.
2. Follow the necessary steps in order to create a new Firebase project for android.
3. Install the provided google-services.json into your computer and then put it inside the app folder.

### Update local.properties

**If there is not a file called local.properties create one under the root folder**

Create the SDK property:

1. Create a field called = sdk.dir
2. Assign your sdk location to the property. In Windows, typically it looks like this : C\:\\Users\\USERNAME\\AppData\\Local\\Android\\Sdk

In order to use some features such as list nearby events and displaying onto the map you need to provide your own API keys.

**To get the ticketmaster key follow these steps:**

1. Go to https://developer-acct.ticketmaster.com/
2. Create an account and copy the Consumer Key field.
3. In local.properties define new field called "TICKETMASTER_API_KEY".
4. Next assign it to your consumer key.

**To get your Google Maps API key:**

1. Follow the necessary steps to create an api key in Google Cloud.
2. Copy your key and go to the local.properties file.
3. Create a new field called "MAPS_API_KEY"
4. Assign the key to the MAPS_API_KEY field.

**Finally get another build then start to project**

### Set a Web Client Id for Firebase

1. In the Firebase project you have created navigate to the Authentication tab.
2. Enable Google Sign-in and Facebook Sign-in.
3. Click on Google Sign-in.
4. From the Web SDK Configuration copy the Web client ID field.
5. On local.properties create new field called FIREBASE_WEB_CLIENT_ID
6. Assign the copied key to this property.
7. Build the project

### Set Facebook Login

1. In the Firebase project you have created navigate to the Authentication tab.
2. Click on Facebook.
3. Follow the necessary steps on the Developers for Meta page to create an android app for only auth purposes.
4. Copy the app id then the app secret into the local.properties to FACEBOOK_APP_ID and FACEBOOK_CLIENT_TOKEN respectively
5. Build the project
