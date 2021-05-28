/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * Copyright (c) 2021 Stefan Oltmann
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package de.stefan_oltmann.smarthome.vallox

import org.eclipse.jetty.websocket.api.Session
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage
import org.eclipse.jetty.websocket.api.annotations.WebSocket
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

@WebSocket
class ValloxWebSocketListener(val callback: (ByteArray) -> Unit) {

    private val closeLatch = CountDownLatch(1)

    @OnWebSocketConnect
    fun onConnect(session: Session) = logger.info("Connected to: {}", session.remoteAddress)

    @OnWebSocketMessage
    fun onMessage(message: String?) = logger.info("Message from Server: {}", message)

    @OnWebSocketError
    fun onError(cause: Throwable) = logger.info("Connection failed: {}", cause.message)

    @OnWebSocketMessage
    fun onBinary(inputStream: InputStream) {

        logger.info("Got binary message")

        try {

            val bytes = readInputStream(inputStream)

            callback(bytes)

        } catch (ex: IOException) {
            logger.error("Error receiving binary from Socket.", ex)
        }
    }

    @Throws(IOException::class)
    private fun readInputStream(inputStream: InputStream): ByteArray {

        val buffer = ByteArrayOutputStream()

        val data = ByteArray(16384)

        var read: Int

        while (inputStream.read(data, 0, data.size).also { read = it } != -1)
            buffer.write(data, 0, read)

        buffer.flush()

        return buffer.toByteArray()
    }

    @OnWebSocketClose
    fun onClose(statusCode: Int, reason: String?) {

        logger.info("WebSocket closed with code {} because of {}", statusCode, reason)

        closeLatch.countDown()
    }

    @Throws(InterruptedException::class)
    fun awaitClose(duration: Int, unit: TimeUnit) =
        closeLatch.await(duration.toLong(), unit)

    companion object {

        private val logger = LoggerFactory.getLogger(ValloxWebSocketListener::class.java)

    }
}
