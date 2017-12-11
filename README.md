# azure-gradle-plugins

## Compiling plugin

In `azure-webapp-gradle-plugin` folder in `build.gradle` update reference to local mave repo. Then run
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
