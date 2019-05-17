siddhi-io-feed
======================================

The **siddhi-io-feed extension** is an extension to <a target="_blank" href="https://wso2.github.io/siddhi">Siddhi</a> that allows you to receive and publish atom entry events via http and 
also allow you to receive RSS item events via Http. This extension works with WSO2 Stream Processor and with standalone Siddhi.

Find some useful links below:

* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-feed">Source code</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-feed/releases">Releases</a>
* <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-feed/issues">Issue tracker</a>

## Latest API Docs 

## How to use 

**Using the extension in <a target="_blank" href="https://github.com/wso2/product-sp">WSO2 Stream Processor</a>**

* You can use this extension in the latest <a target="_blank" href="https://github.com/wso2/product-sp/releases">WSO2 Stream Processor</a> that is a part of <a target="_blank" href="http://wso2.com/analytics?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">WSO2 Analytics</a> offering, with editor, debugger and simulation support.

* This extension is shipped by default with WSO2 Stream Processor, if you wish to use an alternative version of this extension you can replace the component <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-feed/releases">jar</a> that can be found in the `<STREAM_PROCESSOR_HOME>/lib` directory.

**Using the extension as a <a target="_blank" href="https://wso2.github.io/siddhi/documentation/running-as-a-java-library">java library</a>**

* This extension can be added as a maven dependency along with other Siddhi dependencies to your project.
```
     <dependency>
        <groupId>org.wso2.extension.siddhi.io.</groupId>
        <artifactId>siddhi-io-feed</artifactId>
        <version>x.x.x</version>
     </dependency>
```
 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>.

 * Siddhi developers can be contacted via the mailing lists:

    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)

    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)

* We are committed to ensuring support for this extension in production. Our unique approach ensures that all support leverages our open development methodology and is provided by the very same engineers who build the technology.

* For more details and to take advantage of this unique opportunity contact us via <a target="_blank" href="http://wso2.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>.


## Jenkins Build Status

---

|  Branch | Build Status |
| :------ |:------------ |
| master  | [![Build Status](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-feed/badge/icon)](https://wso2.org/jenkins/job/siddhi/job/siddhi-io-feed/) |

---


## Features

**<a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-feed/api/latest/#sink">feed</a><a target="_blank" href="https://siddhi-io.github.io/siddhi/documentation/siddhi-4.0/">(Sink)</a>**

The feed sink publishes the atom entries using HTTP.


**<a target="_blank" href="https://wso2-extensions.github.io/siddhi-io-feed/api/latest/#source">feed</a><a target="_blank" href="https://siddhi-io.github.io/siddhi/documentation/siddhi-4.0/">(Source)</a>**

The feed source consumes the Atom entries and RSS items using HTTP.

## How to Contribute
 
  * Report issues at <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-feed/issues">GitHub Issue Tracker</a>.
  
  * Send your contributions as pull requests to the <a target="_blank" href="https://github.com/wso2-extensions/siddhi-io-feed/tree/master">master branch</a>. 
 
## Contact us 

 * Post your questions with the <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">"Siddhi"</a> tag in <a target="_blank" href="http://stackoverflow.com/search?q=siddhi">Stackoverflow</a>. 
 
 * Siddhi developers can be contacted via the following mailing lists:
 
    Developers List   : [dev@wso2.org](mailto:dev@wso2.org)
    
    Architecture List : [architecture@wso2.org](mailto:architecture@wso2.org)
 
## Support 

* We are committed to provide support for this extension in production. Our unique approach ensures that all support 
leverages our open development methodology, and is provided by the very same engineers who build the technology. 

* For more details and to take advantage of this unique opportunity contact us via <a target="_blank" href="http://wso2
.com/support?utm_source=gitanalytics&utm_campaign=gitanalytics_Jul17">http://wso2.com/support/</a>. 
