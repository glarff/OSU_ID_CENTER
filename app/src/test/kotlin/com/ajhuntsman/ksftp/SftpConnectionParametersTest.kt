package com.osuidsftp

import junit.framework.TestCase
import org.junit.Test
import org.junit.Assert

/**
 * Unit tests for {@see ConnectionParameters}.
 */
class SftpConnectionParametersTest : TestCase() {

    @Test
    @Throws(Exception::class)
    fun testValidParameters() {
        SftpConnectionParametersBuilder.newInstance().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withUsername("john.doe")
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testMissingPort() {
        SftpConnectionParametersBuilder.newInstance().createConnectionParameters()
                .withHost("myserver.com")
                .withUsername("john.doe")
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testMissingUsername() {
        SftpConnectionParametersBuilder.newInstance().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withPassword("1234".toByteArray())
                .create()
    }

    @Test
    @Throws(Exception::class)
    fun testMissingPassword() {
        SftpConnectionParametersBuilder.newInstance().createConnectionParameters()
                .withHost("myserver.com")
                .withPort(2222)
                .withUsername("john.doe")
                .create()
    }

}