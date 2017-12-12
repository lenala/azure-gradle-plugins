# azure-gradle-plugins

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
gradle deploy
```

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

# 4 types of deployment are supported:

## Deployment from Private Container Registry (Azure Container Registry)

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

## Deplyment from public Docke Hub

## Deployment from private Docker Hub

## Deployment to App Service on Windows
```
azurewebapp {
    resourceGroup = <resource_group>
    appName = <appName>
    javaVersion = "1.8.0_102"
    pricingTier = "S2"
    javaWebContainer = "tomcat 7.0"
    target = <path_to_war_file>
}
``` 