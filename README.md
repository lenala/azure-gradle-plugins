# azure-gradle-plugins


- [Azure WebApp plugin](#webapp-plugin)
  - [Compiling plugin](#compile-webapp-plugin)
  - [Running sample ToDo app](#sample-app)
  - [Common settings](#common-settings)
  - [Web App on Windows](#web-app-on-windows)
  - [Web App on Linux](#web-app-on-linux)
  - [Web Apps on Containers](#web-app-on-containers)
    - [Deployment from Private Container Registry (Azure Container Registry)](#web-app-acr)
    - [Deployment from public Docker Hub](#web-app-public-docker)
    - [Deployment from private Docker Hub](#web-app-private-docker)
- [Azure Functions plugin](#azure-functions-plugin)  
- [Azure Authentication settings](#azure-authentication)  

## Compiling plugin

In `azure-webapp-gradle-plugin` folder in `build.gradle` update reference to local maven repo. Then run
```cmd
gradle install
```

## Running sample ToDo app

In `samples/todo-app-on-azure` folder, update reference to local maven repo, appName and Azure Container Registry url and credentials.

In `gradle.properties`, update container registry credentials.

In `application.properties`, update CosmosDB credentials.

To deploy app to Azure, run
```cmd
gradle dockerPushImage
gradle azureWebappDeploy
```

## Common settings

Name | Value
---|---
deploymentType | Deployment type - one of {FTP, WEBDEPLOY, WARDEPLOY}. Optional, default value is FTP.
resourceGroup | Azure resource group to create Web App
appName | Web App name
region | Azure region. Optional, default is WEST_US
pricingTier | Pricing tier
authFile | File with authentication information. Optional, see [Azure Authentication settings](#azure-authentication)
target | Target artifact to deploy. Not used for Web Apps for containers.
stopAppDuringDeployment | Specifies whether to stop Web App during deployment. Optional, default is false

# 4 types of deployment are supported:

## Web App on Windows

`appServiceOnWindows` block should be specified, with the values:

Name | Value
---|---
javaVersion | Java version. Supported versions are: {1.7, 1.7.0_51, 1.7.0_71, 1.8, 1.8.0_25, 1.8.0_60, 1.8.0_73, 1.8.0_111, 1.8.0_92, 1.8.0_102, 1.8.0_144}
javaWebContainer | Web Container. Optional, default is newest Tomcat 8.5.

TOMCAT_8_5_NEWEST
```
azurewebapp {
    resourceGroup = <resource_group>
    appName = <appName>
    pricingTier = "S2"
    target = <path_to_war_file>
    appServiceOnWindows = {
        javaWebContainer = "tomcat 8.5"
        javaVersion = "1.8.0_102"
    }
}
``` 

## Web App on Linux

`appServiceOnLinux` block should be specified, with the values:

Name | Value
---|---
runtimeStack | Base image name. Right now possible values are: {'TOMCAT 9.0-jre8', 'TOMCAT 8.5-jre8}
urlPath | Url path. Optional, if not specified Web App will be deployed to root

```
azurewebapp {
    deploymentType = 'wardeploy'
    resourceGroup = <resource_group>
    appName = <appName>
    pricingTier = "S2"
    target = <path_to_war_file>
    appServiceOnLinux = {
        runtimeStack = 'TOMCAT 9.0-jre8'
        urlPath = <url_path>
    }
}
``` 

## Web Apps on Containers

`containerSettings` block should be specified.

### Deployment from Private Container Registry (Azure Container Registry)

```
azurewebapp {
    resourceGroup = <resource_group>
    appName = <appName>
    pricingTier = "S1"
    containerSettings = {
        imageName = <image_name>
        serverId = <server_id>
        registryUrl = "https://" + serverId
    }
}
```

### Deployment from public Docker Hub

### Deployment from private Docker Hub

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
