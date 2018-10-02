[![Build Status](https://travis-ci.org/lenala/azure-gradle-plugins.svg?style=flat-square&label=build&branch=master)](https://travis-ci.org/lenala/azure-gradle-plugins)
# azure-gradle-plugins


- [Azure WebApp plugin](#azure-webapp-plugin)
  - [Compiling plugin](#compiling-plugin)
  - [Running sample ToDo app](#running-sample-todo-app)
  - [Common settings](#common-settings)
  - [Web App on Windows](#web-app-on-windows)
  - [Web App on Linux](#web-app-on-linux)
  - [Web Apps on Containers](#web-apps-on-containers)
    - [Deployment from Private Container Registry (Azure Container Registry)](#deployment-from-private-container-registry-azure-container-registry)
    - [Deployment from public Docker Hub](#deployment-from-public-docker-hub)
    - [Deployment from private Docker Hub](#deployment-from-private-docker-hub)
- [Azure Functions plugin](#azure-functions-plugin)
    - [Prerequisites](#prerequisites)
    - [Common configuration](#common-configuration)
    - [Tasks](#tasks)
    - [Running sample Azure Functions app](#running-sample-azure-functions-app)
- [Azure Authentication settings](#azure-authentication-settings)  

## Compiling plugin

In `azure-webapp-gradle-plugin` folder in `build.gradle` update reference to local maven repo. Then run

```cmd
# Windows
gradlew.bat install

# Linux/macOS
./gradlew install
```

## Running sample ToDo app

In `samples/todo-app-on-azure` folder, update reference to local maven repo, appName and Azure Container Registry url and credentials. Follow [this tutorial](https://docs.microsoft.com/en-us/azure/container-registry/container-registry-get-started-portal) to get started with Azure Container Registry.

In `gradle.properties`, set your container registry `serverId`, `serverUsername`, and `serverPassword`.

To push the Docker image, you'll need Docker running, then execute the `dockerPushImage` task:

```cmd
# Windows
gradlew.bat dockerPushImage

# Linux/macOS
./gradlew dockerPushImage
```

Create a new CosmosDB instance and set your CosmosDB credentials in `src/main/resources/application.properties`.

Configure `azurewebapp` in `build.gradle` with valid values for `resourceGroup`, `appName`, `pricingTier`, and `authFile` (see the section on [Azure Authentication settings](#azure-authentication-settings)).

Then deploy the ToDo web application to a Azure Container instance, you must specify  the `azureWebappDeploy` task:

```cmd
# Windows
gradlew.bat azureWebappDeploy

# Linux/macOS
./gradlew azureWebappDeploy
```

## Common settings

|Name | Required | Value |
|---|---|---|
|resourceGroup | true | Azure resource group to create Web App
|appName | true | Web App name
|region | false | Azure region. Optional, default is WEST_US2
|appServicePlanResourceGroup | false | Specifies the resource group of the existing App Service Plan when you do not want to create a new one. If this setting is not specified, plugin will use the value defined in <code>resourceGroup</code>
|appServicePlanName | false | Specifies the name of the existing App Service Plan when you do not want to create a new one.
|stopAppDuringDeployment | false | Specifies whether to stop Web App during deployment. Optional, default is false
| __appService__ | true | Block that specifies AppService settings
| type | true | Type of the AppService, one of {'linux','windows','docker'}
| runtimeStack ||
| javaWebContainer | false | One of WebContainer values, default is TOMCAT_8_5_NEWEST
| javaVersion | |
| imageName |
| startUpFile |
| serverId |
| username |
| password |
| registryUrl |
| pricingTier | false | 	Specifies the pricing tier for your Web App; the default value is S1.
| __authentication__ | true | Bloack that specifies authentication with Azure
| type | | Authentication type, one of {FILE,PROPERTIES,AZURECLI}
|authFile | false | File with authentication information. Optional, see [Azure Authentication settings](#azure-authentication-settings)
| client | |
| tenant | |
| key | |
| certificate | |
| certificatePassword;
| __deployment__ | true | Specifies deployment type and configuration
deploymentType | false | Deployment type - one of {FTP, WAR, ZIP, NONE}. Optional, default value is WAR.
target | false | Target artifact to deploy. Not used for Web Apps for containers. Optional, if not specified, default war file output produced by 'war' plugin will be used.
contextPath | | Url path


# 4 types of deployment are supported:

## Web App on Windows

`appService` block should be specified, with the values:

Name | Value
---|---
type | 'windows'
javaVersion | Java version. Supported versions are: {1.7, 1.7.0_51, 1.7.0_71, 1.8, 1.8.0_25, 1.8.0_60, 1.8.0_73, 1.8.0_111, 1.8.0_92, 1.8.0_102, 1.8.0_144}
javaWebContainer | Web Container. Optional, default is newest Tomcat 8.5.
```
azureWebApp {
    resourceGroup = "${System.env.WEBAPP_RESOURCE_GROUP}"
    appName = "${System.env.WEBAPP_NAME}"
    pricingTier = "S2"
    region = "southcentralus"

    appService = {
        type = 'windows'
        javaWebContainer = "tomcat 8.5"
        javaVersion = "1.8.0_102"
    }

    authentication = {
        type = "file"
        file = "<path_to_auth_file>"
    }

    deployment = {
        type = "war"
        // if 'warFile' is not specified, default output of the 'war' plugin will be used
        // warFile = '<path_to_war_file>'
        contextPath = 'todoapp'    // Url path. Optional, if not specified Web App will be deployed to root. Works for WAR deployment type only.
    }
}
```
[Usage example](./samples/todo-app-java-on-azure.appservice-on-linux)

## Web App on Linux

`appService` block should contain the values:

Name | Value
---|---
runtimeStack | Base image name. Right now possible values are: {'TOMCAT 9.0-jre8', 'TOMCAT 8.5-jre8}
urlPath | Url path. Optional, if not specified Web App will be deployed to root. Works for WARDEPLOY deployment type only.

```
azureWebApp {
    resourceGroup = <resource_group>
    appName = <appName>
    pricingTier = "S2"
    appService = {
        type = 'linux'
        runtimeStack = 'TOMCAT 9.0-jre8'
    }
    authentication = {
        type = "file"
        file = "<path_to_auth_file>"
    }
    deployment = {
        type = "war"
        // if 'warFile' is not specified, default output of the 'war' plugin will be used
        // warFile = '<path_to_war_file>'
        contextPath = 'todoapp'
    }
}
```
[Sample ToDo App](https://github.com/Microsoft/todo-app-java-on-azure/tree/tomcat-on-azure-app-service-linux-gradle)

## Web Apps on Containers

`containerSettings` block should be specified.

### Deployment from Private Container Registry (Azure Container Registry)

```
    resourceGroup = <resource_group>
    appName = <appName>
    pricingTier = "S1"
    appService = {
        type = 'docker'
        imageName = dockerImage
        serverId = project.property('serverId')
        registryUrl = "https://" + serverId
        username = project.property('serverUsername')
        password = project.property('serverPassword')
    }

    authentication = {
        type = "file"
        file = "C:/stuff/my2.azureauth"
    }

    deployment = {
        type = "none"
    }
}
```
[Usage example](./samples/todo-app-java-on-azure)

### Deployment from public Docker Hub

### Deployment from private Docker Hub

## Azure Functions plugin

### Prerequisites

Required only to run Azure Functions locally:
- [Azure Functions Core Tools 2.0 and above](https://www.npmjs.com/package/azure-functions-core-tools)

### Common configuration

```
azurefunctions {
    authFile = <file_with_authentication_info>
}
```

### Tasks

#### PackageTask
Package the functions for deployment.

```
task azureFunctionsPackage(type: PackageTask) {
    dependsOn jar
    appName = "myFunctionApp"
    resourceGroup = "myFunctionApp"
    region = "westus"
}
```

#### DeployTask
Deploy the project output jar to target Function App.
```
task azureFunctionsDeploy(type: DeployTask) {
    appName = "myFunctionApp"
    resourceGroup = "myFunctionApp"
    region = "westus"
}
```

#### RunTask
Invoke Azure Functions Local Emulator to run all functions.
```
task azureFunctionsRun(type: RunTask) {
    appName = "myFunctionApp"
    resourceGroup = "myFunctionApp"
    region = "westus"
}
```

#### AddTask
Create new Java function and add to current project.
```
task azureFunctionsAdd(type: AddTask) {
    functionPackageName = "my.function"
    functionTemplate = "HttpTrigger"
}
```

## Running sample Azure Functions app

To build and deploy Azure Functions application, run:
```cmd
gradle azureFunctionsPackage
gradle azureFunctionsDeploy
```
Where `azureFunctionsPackage` is of type `DeployTask` and `AzureFunctionsDeploy` of type `DeployTask`.

To verify function running, you can use `curl -X POST -d "Azure World" <Deployed Host URL>/api/hello`.


To add a new function, run:
```cmd
gradle azureFunctionsAdd
```
Where `azureFunctionsAdd` is of type `AddTask`.

To run function locally, use:
```cmd
gradle azureFunctionsRun
```
Where `azureFunctionsRun` is of type `RunTask`.

[Source code for sample app](./samples/walkthrough)

## Azure Authentication settings
To authenticate with Azure, device login can be used. To enable that, you need to sign in with Azure CLI first.
Alternatively, authentication file can be used. The authentication file, referenced as "my.azureauth" in the example,
contains the information of a service principal. You can generate this file using Azure CLI 2.0 through the following command.
Make sure you selected your subscription by az account set --subscription <name or id> and you have the privileges to create service principals.
```cmd                                                
az ad sp create-for-rbac --sdk-auth > my.azureauth
```

Please see [Authentication in Azure Management Libraries for Java](https://github.com/Azure/azure-libraries-for-java/blob/master/AUTH.md) for authentication file formats.
You can configure to use authentication file in gradle build script:
```
azurewebapp {
    ...
    authFile=<path_to_file>
    ...
}
```

Another way to authenticate with Azure would be to provide settings in `gradle.properties`:
```
client=<client_id>
tenant=<tenant_id>
key=(need key or certificate info)
certificate=(optional)
certificatePassword=(optional)
environment=(optional)
```

`subscriptionId` can be also provided in gradle.properties, in case it is different from default subscription id.
