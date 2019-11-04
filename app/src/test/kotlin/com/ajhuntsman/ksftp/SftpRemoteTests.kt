package com.osuidsftp

import junit.framework.TestCase
import org.apache.commons.lang3.StringUtils
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.IOException
import java.util.*

/**
 * Batch Upload test suite - upload files to SFTP server
 * KSFTP variables must be set in Environment Variables at this time - refer to Readme
 */
class SftpRemoteTests : TestCase() {

    // These environment variables must be defined on your machine
    private val ENVIRONMENT_VARIABLE_HOST = "KSFTP_HOST"
    private val ENVIRONMENT_VARIABLE_PORT = "KSFTP_PORT"
    private val ENVIRONMENT_VARIABLE_USERNAME = "KSFTP_USERNAME"
    private val ENVIRONMENT_VARIABLE_PASSWORD = "KSFTP_PASSWORD"

    // Remote directory for upload - a folder at the user's root level on SFTP server
    // Directory will be created if it does not exist
    private val remoteDirectoryForUploads = "Folder1/"

    private var testFiles: Array<File>? = null
    private var sftpClient: SftpClient? = null

    @Before
    @Throws(Exception::class)
    public override fun setUp() {

        super.setUp()

        // Cretate connection paramaters
        sftpClient = SftpClient.create(createConnectionParameters())

        // Retrieve image directory location from local resources
        val testImagesSourceDirectory = getImagesDirectory("/testImages/")
        TestCase.assertTrue("Test images directory could not be found!", testImagesSourceDirectory.isDirectory)

        // Check for files in image directory
        testFiles = testImagesSourceDirectory.listFiles()
        TestCase.assertTrue("No test image files were found!", testFiles != null && testFiles!!.size > 0)
        KsftpLog.logDebug("Found " + testFiles!!.size + " test image files")
    }

    /**
     * Gets a file from the test resources package, or throws an exception if the test file doesn't exist.
     * @param relativeFilePath the file path, relative to "src/test/resources"
     */
    @Throws(Exception::class)
    private fun getImagesDirectory(relativeFilePath: String): File {
        var theRelativeFilePath = relativeFilePath
        val url = SftpRemoteTests::class.java.getResource(theRelativeFilePath)

        KsftpLog.logDebug("theRelativeFilePath is " + theRelativeFilePath)
        KsftpLog.logDebug("Url is " + url)

        val testFile = File(url!!.file)
        TestCase.assertTrue("No test file exists for relative path '$theRelativeFilePath'", testFile.exists())
        return testFile
    }

    /**
     * Ensures that a directory exists for the specified path, and returns the [File],
     * or `null` if it could not be created.

     * @param directoryPath the directory path to ensure
     */
    @Throws(IOException::class)
    private fun ensureDirectory(directoryPath: String): File {
        val errorMessage = "Could not create directory for path '$directoryPath'"
        if (StringUtils.isEmpty(directoryPath)) {
            throw IOException(errorMessage)
        }

        val directory = File(directoryPath)
        if (directory.exists()) {
            if (!directory.isDirectory) {
                throw IOException("File '$directory' exists and is not a directory. Unable to create directory.")
            }
        } else {
            if (!directory.mkdirs()) {
                // Double-check that some other thread or process hasn't made
                // the directory in the background
                if (!directory.isDirectory) {
                    throw IOException("Unable to create directory '$directory'")
                }
            }
        }

        if (!directory.isDirectory) {
            throw IOException(errorMessage)
        }
        return directory
    }

    /**
     * Creates new connection parameters.
     */
    private fun createConnectionParameters(): SftpConnectionParameters {
        return SftpConnectionParametersBuilder.newInstance().createConnectionParameters()
                .withHostFromEnvironmentVariable(ENVIRONMENT_VARIABLE_HOST)
                .withPortFromEnvironmentVariable(ENVIRONMENT_VARIABLE_PORT)
                .withUsernameFromEnvironmentVariable(ENVIRONMENT_VARIABLE_USERNAME)
                .withPasswordFromEnvironmentVariable(ENVIRONMENT_VARIABLE_PASSWORD)
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testAllSftpOperations() {
        executeBatchUpload()
    }

    @Throws(Exception::class)
    private fun executeBatchUpload() {

        val remoteFilePaths = ArrayList<String>()
        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectoryForUploads + File.separator + testFile.name
            filePairs.add(FilePair(testFile.path, remoteFilePath))
            remoteFilePaths.add(remoteFilePath)
        }

        TestCase.assertTrue("Files were not uploaded!", sftpClient!!.upload(filePairs, 120*testFiles!!.size))
        TestCase.assertTrue("Files don't exist on server!", sftpClient!!.checkFiles(remoteFilePaths))
    }

    /*
    @Test
    @Throws(Exception::class)
    fun testBatchUploadTimeout() {
        val remoteFilePaths = ArrayList<String>()
        val filePairs = ArrayList<FilePair>()
        for (testFile in testFiles!!) {
            val remoteFilePath = remoteDirectoryForUploads + File.separator + testFile.name
            filePairs.add(FilePair(testFile.path, remoteFilePath))
            remoteFilePaths.add(remoteFilePath)
        }

        assertFailsWith(UploadTimeoutException::class, "Batch upload timed out with an unexpected exception") {
            client!!.upload(filePairs, 2, 5)
        }
    }*/
}