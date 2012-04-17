# Kii Board Sample App#

A simple messages board that uses **Kii Cloud Storage**. It demonstrates:

- Register and login users
- Create topic
- Post messages to a topic
- Messages conversation view
- Links between topic, messages and users
- Persistent cache for topic
- Delete a topic including all the messages belong to the topic
- Show interoperability with [iOS Kii Board Sample App](https://github.com/kii-dev-jenkins/KiiiOSSampleApps/tree/master/KiiBoard)
- Show interoperability with [Kii Ad Network](https://github.com/kii-dev-jenkins/KiiAdNetworkSampleApp)

![Screen shots](https://github.com/kii-dev-jenkins/KiiBoard/raw/master/doc/screen_shots.jpg)

![Screen shot with Ad](https://github.com/kii-dev-jenkins/KiiBoard/raw/master/doc/withAds.png)


#Getting Started#

##Requirements##
- Support Android 2.2 and above


##Installation and Setup##

- Download the [sample app](https://github.com/kii-dev-jenkins/KiiBoard/zipball/master).

- If you are developing in Eclipse with the ADT Plugin, create a project for the "KiiBoard" sample app by starting a new Android Project, selecting "Create project from existing source".

- Update the sample app with your own application ID and application key at [Constants class](https://github.com/kii-dev-jenkins/KiiBoard/blob/master/src/com/kii/cloud/board/sdk/Constants.java).

- Kii Ad Network is disable by default, to enable refer to [Constants class](https://github.com/kii-dev-jenkins/KiiBoard/blob/master/src/com/kii/cloud/board/sdk/Constants.java). 

- The SDK javadoc is not automtatic visible in your eclipse project, you need to attach the JavaDoc([doc folder](https://github.com/kii-dev-jenkins/KiiBoard/tree/master/doc)) to the Kii Cloud SDK jar file ([lib folder](https://github.com/kii-dev-jenkins/KiiBoard/tree/master/libs)). 

- [Java API documenatation](http://static.kii.com/devportal/production/docs/storage/)


All of the samples are licensed under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0), so feel free to use any of the code in your own applications as needed!