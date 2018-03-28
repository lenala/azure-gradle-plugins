/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.gradle.functions.helpers;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.gradle.api.GradleException;
import org.gradle.api.logging.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Utility class to upload directory to FTP server
 */
public class FTPUploader {
    private static final String UPLOAD_START = "Starting uploading files to FTP server: ";
    private static final String UPLOAD_SUCCESS = "Successfully uploaded files to FTP server: ";
    private static final String UPLOAD_FAILURE = "Failed to upload files to FTP server, retrying immediately (%d/%d)";
    private static final String UPLOAD_RETRY_FAILURE = "Failed to upload files to FTP server after %d retries...";
    private static final String UPLOAD_DIR_START = "Starting uploading directory: %s --> %s";
    private static final String UPLOAD_DIR_FINISH = "Finished uploading directory: %s --> %s";
    private static final String UPLOAD_DIR_FAILURE = "Failed to upload directory: %s --> %s";
    private static final String UPLOAD_DIR = "%s[DIR] %s --> %s";
    private static final String UPLOAD_FILE = "%s[FILE] %s --> %s";
    private static final String UPLOAD_FILE_REPLY = "%s.......Reply Message : %s";

    private final Logger logger;

    public FTPUploader(Logger logger) {
        this.logger = logger;
    }

    /**
     * Upload directory to specified FTP server with retries.
     *
     * @param ftpServer
     * @param username
     * @param password
     * @param sourceDirectory
     * @param targetDirectory
     * @param maxRetryCount
     * @throws GradleException
     */
    public void uploadDirectoryWithRetries(final String ftpServer, final String username, final String password,
                                           final String sourceDirectory, final String targetDirectory,
                                           final int maxRetryCount) throws GradleException {
        int retryCount = 0;
        while (retryCount < maxRetryCount) {
            retryCount++;
            logger.quiet(UPLOAD_START + ftpServer);
            if (uploadDirectory(ftpServer, username, password, sourceDirectory, targetDirectory)) {
                logger.quiet(UPLOAD_SUCCESS + ftpServer);
                return;
            } else {
                logger.warn(String.format(UPLOAD_FAILURE, retryCount, maxRetryCount));
            }
        }
        // Reaching here means all retries failed.
        throw new GradleException(String.format(UPLOAD_RETRY_FAILURE, maxRetryCount));
    }

    /**
     * Upload directory to specified FTP server without retries.
     *
     * @param ftpServer
     * @param username
     * @param password
     * @param sourceDirectoryPath
     * @param targetDirectoryPath
     * @return Boolean to indicate whether uploading is successful.
     */
    private boolean uploadDirectory(final String ftpServer, final String username, final String password,
                                    final String sourceDirectoryPath, final String targetDirectoryPath) {
        logger.debug("FTP username: " + username);
        try {
            final FTPClient ftpClient = getFTPClient(ftpServer, username, password);

            logger.quiet(String.format(UPLOAD_DIR_START, sourceDirectoryPath, targetDirectoryPath));
            uploadDirectory(ftpClient, sourceDirectoryPath, targetDirectoryPath, "");
            logger.quiet(String.format(UPLOAD_DIR_FINISH, sourceDirectoryPath, targetDirectoryPath));

            ftpClient.disconnect();
            return true;
        } catch (Exception e) {
            logger.error(String.format(UPLOAD_DIR_FAILURE, sourceDirectoryPath, targetDirectoryPath), e);
        }

        return false;
    }

    /**
     * Recursively upload a directory to FTP server with the provided FTP client object.
     *
     * @param sourceDirectoryPath
     * @param targetDirectoryPath
     * @param logPrefix
     * @throws IOException
     */
    private void uploadDirectory(final FTPClient ftpClient, final String sourceDirectoryPath,
                                 final String targetDirectoryPath, final String logPrefix) throws IOException {
        logger.quiet(String.format(UPLOAD_DIR, logPrefix, sourceDirectoryPath, targetDirectoryPath));
        final File sourceDirectory = new File(sourceDirectoryPath);
        final File[] files = sourceDirectory.listFiles();
        if (files == null || files.length == 0) {
            logger.quiet(String.format("%sEmpty directory at %s", logPrefix, sourceDirectoryPath));
            return;
        }

        // Make sure target directory exists
        final boolean isTargetDirectoryExist = ftpClient.changeWorkingDirectory(targetDirectoryPath);
        if (!isTargetDirectoryExist) {
            ftpClient.makeDirectory(targetDirectoryPath);
        }

        final String nextLevelPrefix = logPrefix + "..";
        for (File file : files) {
            if (file.isFile()) {
                uploadFile(ftpClient, file.getAbsolutePath(), targetDirectoryPath, nextLevelPrefix);
            } else {
                uploadDirectory(ftpClient, Paths.get(sourceDirectoryPath, file.getName()).toString(),
                        targetDirectoryPath + "/" + file.getName(), nextLevelPrefix);
            }
        }
    }

    /**
     * Upload a single file to FTP server with the provided FTP client object.
     *
     * @param sourceFilePath
     * @param targetFilePath
     * @param logPrefix
     * @throws IOException
     */
    private void uploadFile(final FTPClient ftpClient, final String sourceFilePath, final String targetFilePath,
                            final String logPrefix) throws IOException {
        logger.quiet(String.format(UPLOAD_FILE, logPrefix, sourceFilePath, targetFilePath));
        final File sourceFile = new File(sourceFilePath);
        try (final InputStream is = new FileInputStream(sourceFile)) {
            ftpClient.changeWorkingDirectory(targetFilePath);
            ftpClient.storeFile(sourceFile.getName(), is);

            final int replyCode = ftpClient.getReplyCode();
            final String replyMessage = ftpClient.getReplyString();
            if (isCommandFailed(replyCode)) {
                logger.error(String.format(UPLOAD_FILE_REPLY, logPrefix, replyMessage));
                throw new IOException("Failed to upload file: " + sourceFilePath);
            } else {
                logger.quiet(String.format(UPLOAD_FILE_REPLY, logPrefix, replyMessage));
            }
        }
    }

    private FTPClient getFTPClient(final String ftpServer, final String username, final String password)
            throws Exception {
        final FTPClient ftpClient = new FTPClient();
        ftpClient.connect(ftpServer);
        ftpClient.login(username, password);
        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
        ftpClient.enterLocalPassiveMode();
        return ftpClient;
    }

    private boolean isCommandFailed(final int replyCode) {
        // https://en.wikipedia.org/wiki/List_of_FTP_server_return_codes
        // 2xx means command has been successfully completed
        return replyCode >= 300;
    }
}
