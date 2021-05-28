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
import org.eclipse.jetty.websocket.client.ClientUpgradeRequest
import org.eclipse.jetty.websocket.client.WebSocketClient
import java.net.URI
import java.nio.ByteBuffer
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

/**
 * The [ValloxClientImpl] is responsible for socket communication with the vallox ventilation unit
 *
 * @author Björn Brings - Initial contribution
 * @author Stefan Oltmann - Refactorings and Kotlin conversion
 */
class ValloxClientImpl(private val uri: URI) : ValloxClient {

    private val webSocketClient = WebSocketClient()

    private val messageHandler = ValloxMessageHandler

    init {
        webSocketClient.start()
    }

    override fun readStatus(): ValloxStatus {

        val requestBytes = messageHandler.generateReadRequestBytes()

        return sendBytesToService(requestBytes, ValloxDataMode.READ_TABLES)!!
    }

    override fun turnOn() {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesOnOff(1),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun turnOff() {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesOnOff(0),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun switchProfile(profile: ValloxProfile) {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesSwitchToProfile(profile),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setExtractFanBalanceBase(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesExtractFanBalanceBase(fanSpeed),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setSupplyFanBalanceBase(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesSupplyFanBalanceBase(fanSpeed),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setFanSpeed(profile: ValloxProfile, fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesFanSpeed(profile, fanSpeed),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setFireplaceExtractFanSpeed(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesFireplaceExtractFanSpeed(fanSpeed),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setFireplaceSupplyFanSpeed(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesFireplaceSupplyFanSpeed(fanSpeed),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setTargetTemperature(profile: ValloxProfile, targetTemperature: Int) {

        require(targetTemperature in 5..25)

        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesTargetTemperature(
                profile,
                targetTemperature
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setBytesBoostTime(boostTimeInMinutes: Int) {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesBoostTime(boostTimeInMinutes),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setBoostTimerEnabled(enabled: Boolean) {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesBoostTimerEnabled(enabled),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setFireplaceTime(fireplaceTimeInMinutes: Int) {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesFireplaceTime(fireplaceTimeInMinutes),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setFireplaceTimerEnabled(enabled: Boolean) {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesFireplaceTimerEnabled(enabled),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override fun setWeeklyTimerEnabled(enabled: Boolean) {
        sendBytesToService(
            requestBytes = messageHandler.generateWriteRequestBytesWeeklyTimerEnabled(enabled),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    private fun sendBytesToService(
        requestBytes: ByteBuffer,
        dataMode: ValloxDataMode
    ): ValloxStatus? {

        var sessionFuture: Future<Session>? = null

        var valloxStatus: ValloxStatus? = null

        try {

            val socket = ValloxWebSocketListener { bytes ->
                valloxStatus = messageHandler.readMessage(dataMode, bytes)
            }

            val request = ClientUpgradeRequest()

            sessionFuture = webSocketClient.connect(socket, uri, request)

            /* Blocked wait until connection is established. */
            val session = sessionFuture.get()

            session.remote.sendBytes(requestBytes)

            /* Wait that the request has finished. */
            socket.awaitClose(2, TimeUnit.SECONDS)

            return valloxStatus

        } finally {

            if (sessionFuture != null && !sessionFuture.isDone)
                sessionFuture.cancel(true)
        }
    }
}
