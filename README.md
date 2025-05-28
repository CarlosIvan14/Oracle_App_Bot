# oci-react-samplesss
A repository for full stack Cloud Native applications with a React JS frontend and various backends (Java, Python, DotNet, and so on) on the Oracle Cloud Infrastructure.

![image](https://user-images.githubusercontent.com/7783295/116454396-cbfb7a00-a814-11eb-8196-ba2113858e8b.png)
  

## MyToDo React JS
The `mtdrworkshop` repository hosts the materiald (code, scripts and instructions) for building and deploying Cloud Native Application using a Java/Helidon backend


### Requirements
The lab executes scripts that require the following software to run properly: (These are already installed on and included with the OCI Cloud Shell)
* oci-cli
* python 2.7^
* terraform
* mvn (maven) 

### TEAM TEC-GDL-46 SETUP
1. Clone this repository.
2. Download the wallet I passed on Discord and extract it on a known defined path without weird chars.
3. Update the Wallet Path on the MtdrSpring/backend/src/main/resources/application.properties on spring.datasource.url
4. Go to MtdrSpring/backend & Run using:
```
mvn clean install
mvn spring-boot:run
```
5. Wait for 30 seconds to let it run and go to http://140.84.174.78
