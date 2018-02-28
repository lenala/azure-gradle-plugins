/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.functions.handlers;

import com.microsoft.azure.gradle.functions.AzureStorageHelper;
import com.microsoft.azure.gradle.functions.FunctionsTask;
import com.microsoft.azure.management.appservice.AppSetting;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.storage.CloudStorageAccount;
import org.apache.commons.lang3.StringUtils;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MSDeployArtifactHandlerImpl implements ArtifactHandler {
    private static final String DEPLOYMENT_PACKAGE_CONTAINER = "java-functions-deployment-packages";
    private static final String ZIP_EXT = ".zip";
    private static final String CREATE_ZIP_START = "Step 1 of 4: Creating ZIP package...";
    private static final String CREATE_ZIP_DONE = "Successfully saved ZIP package at ";
    private static final String STAGE_DIR_NOT_FOUND = "Function App stage directory '%s' not found. " +
            "Please run package task first.";
    private static final String LOCAL_SETTINGS_FILE = "local.settings.json";
    private static final String REMOVE_LOCAL_SETTINGS = "Remove local.settings.json from ZIP package.";
    private static final String INTERNAL_STORAGE_KEY = "AzureWebJobsStorage";
    private static final String INTERNAL_STORAGE_NOT_FOUND = "Application setting 'AzureWebJobsStorage' not found.";
    private static final String INTERNAL_STORAGE_CONNECTION_STRING = "Function App Internal Storage Connection String: ";
    private static final String UPLOAD_PACKAGE_START = "Step 2 of 4: Uploading ZIP package to Azure Storage...";
    private static final String UPLOAD_PACKAGE_DONE = "Successfully uploaded ZIP package to ";
    private static final String DEPLOY_PACKAGE_START = "Step 3 of 4: Deploying Function App with package...";
    private static final String DEPLOY_PACKAGE_DONE = "Successfully deployed Function App with package.";
    private static final String DELETE_PACKAGE_START = "Step 4 of 4: Deleting deployment package from Azure Storage...";
    private static final String DELETE_PACKAGE_DONE = "Successfully deleted deployment package ";
    private static final String DELETE_PACKAGE_FAIL = "Failed to delete deployment package ";

    private FunctionsTask functionsTask;

    public MSDeployArtifactHandlerImpl(final FunctionsTask functionsTask) {
        this.functionsTask = functionsTask;
    }

    @Override
    public void publish() throws Exception {
        final File zipPackage = createZipPackage();

        final FunctionApp app = functionsTask.getFunctionApp();

        functionsTask.getLogger().quiet("FunctionApp " + app);

        final CloudStorageAccount storageAccount = getCloudStorageAccount(app);

        final String blobName = getBlobName();

        final String packageUri = uploadPackageToAzureStorage(zipPackage, storageAccount, blobName);

        deployWithPackageUri(app, packageUri, () -> deletePackageFromAzureStorage(storageAccount, blobName));
    }

    protected void logInfo(final String message) {
        if (functionsTask != null) {
            functionsTask.getLogger().quiet(message);
        }
    }

    private void logDebug(final String message) {
        if (functionsTask != null) {
            functionsTask.getLogger().quiet(message);
        }
    }

    private void logError(final String message) {
        if (functionsTask != null) {
            functionsTask.getLogger().quiet(message);
        }
    }

    private File createZipPackage() throws Exception {
        logInfo("");
        logInfo(CREATE_ZIP_START);

        final String stageDirectoryPath = functionsTask.getDeploymentStageDirectory();
        final File stageDirectory = new File(stageDirectoryPath);
        final File zipPackage = new File(stageDirectoryPath.concat(ZIP_EXT));

        if (!stageDirectory.exists()) {
            String errorMessage = String.format(STAGE_DIR_NOT_FOUND, stageDirectoryPath);
            logError(STAGE_DIR_NOT_FOUND);
            throw new Exception(STAGE_DIR_NOT_FOUND);
        }

        ZipUtil.pack(stageDirectory, zipPackage);

        logDebug(REMOVE_LOCAL_SETTINGS);
        ZipUtil.removeEntry(zipPackage, LOCAL_SETTINGS_FILE);

        logInfo(CREATE_ZIP_DONE + stageDirectoryPath.concat(ZIP_EXT));
        return zipPackage;
    }

    protected CloudStorageAccount getCloudStorageAccount(final FunctionApp app) throws Exception {
        final AppSetting internalStorageSetting = app.getAppSettings().get(INTERNAL_STORAGE_KEY);
        if (internalStorageSetting == null || StringUtils.isEmpty(internalStorageSetting.value())) {
            logError(INTERNAL_STORAGE_NOT_FOUND);
            throw new Exception(INTERNAL_STORAGE_NOT_FOUND);
        }
        logDebug(INTERNAL_STORAGE_CONNECTION_STRING + internalStorageSetting.value());
        return CloudStorageAccount.parse(internalStorageSetting.value());
    }

    protected String getBlobName() {
        return functionsTask.getAppName()
                .concat(new SimpleDateFormat(".yyyyMMddHHmmssSSS").format(new Date()))
                .concat(ZIP_EXT);
    }

    protected String uploadPackageToAzureStorage(final File zipPackage, final CloudStorageAccount storageAccount,
                                                 final String blobName) throws Exception {
        logInfo("");
        logInfo(UPLOAD_PACKAGE_START);
        final String packageUri = AzureStorageHelper.uploadFileAsBlob(zipPackage, storageAccount,
                DEPLOYMENT_PACKAGE_CONTAINER, blobName);
        logInfo(UPLOAD_PACKAGE_DONE + packageUri);
        return packageUri;
    }

    protected void deployWithPackageUri(final FunctionApp app, final String packageUri, Runnable onDeployFinish) {
        try {
            logInfo("");
            logInfo(DEPLOY_PACKAGE_START);
            app.deploy()
                    .withPackageUri(packageUri)
                    .withExistingDeploymentsDeleted(false)
                    .execute();
            logInfo(DEPLOY_PACKAGE_DONE);
        } finally {
            onDeployFinish.run();
        }
    }

    protected void deletePackageFromAzureStorage(final CloudStorageAccount storageAccount, final String blobName) {
        try {
            logInfo("");
            logInfo(DELETE_PACKAGE_START);
            AzureStorageHelper.deleteBlob(storageAccount, DEPLOYMENT_PACKAGE_CONTAINER, blobName);
            logInfo(DELETE_PACKAGE_DONE + blobName);
        } catch (Exception e) {
            logError(DELETE_PACKAGE_FAIL + blobName);
        }
    }
}
