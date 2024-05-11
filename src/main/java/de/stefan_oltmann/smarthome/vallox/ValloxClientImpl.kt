/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 * Copyright (c) 2024 Stefan Oltmann
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package de.stefan_oltmann.smarthome.vallox

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.util.moveToByteArray
import io.ktor.websocket.send
import java.nio.ByteBuffer

/**
 * The [ValloxClientImpl] is responsible for socket communication with the vallox ventilation unit
 *
 * @author Björn Brings - Initial contribution
 * @author Stefan Oltmann - Refactorings and Kotlin conversion
 */
class ValloxClientImpl(
    private val host: String
) : ValloxClient {

    private val httpClient = HttpClient {

        install(HttpTimeout) {
            this.requestTimeoutMillis = 5000
        }

        install(WebSockets)
    }

    override suspend fun readStatus(): ValloxStatus? =
        sendBytesToService(
            ValloxMessageHandler.generateReadRequestBytes(),
            ValloxDataMode.READ_TABLES
        )

    override suspend fun turnOn() {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesOnOff(
                updateState = 1
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun turnOff() {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesOnOff(
                updateState = 0
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun switchProfile(profile: Profile) {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesSwitchToProfile(
                profile = profile
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setExtractFanBalanceBase(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesExtractFanBalanceBase(
                fanSpeed
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setSupplyFanBalanceBase(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesSupplyFanBalanceBase(
                fanSpeed
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setFanSpeed(profile: Profile, fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesFanSpeed(
                profile,
                fanSpeed
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setFireplaceExtractFanSpeed(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesFireplaceExtractFanSpeed(
                fanSpeed
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setFireplaceSupplyFanSpeed(fanSpeed: Int) {

        require(fanSpeed in 0..100)

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesFireplaceSupplyFanSpeed(
                fanSpeed
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setTargetTemperature(profile: Profile, targetTemperature: Int) {

        require(targetTemperature in 5..25)

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(
                profile,
                targetTemperature
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setBytesBoostTime(boostTimeInMinutes: Int) {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesBoostTime(
                boostTimeInMinutes
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setBoostTimerEnabled(enabled: Boolean) {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesBoostTimerEnabled(
                enabled
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setFireplaceTime(fireplaceTimeInMinutes: Int) {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesFireplaceTime(
                fireplaceTimeInMinutes
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setFireplaceTimerEnabled(enabled: Boolean) {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesFireplaceTimerEnabled(
                enabled
            ),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    override suspend fun setWeeklyTimerEnabled(enabled: Boolean) {

        sendBytesToService(
            requestBytes = ValloxMessageHandler.generateWriteRequestBytesWeeklyTimerEnabled(enabled),
            dataMode = ValloxDataMode.WRITE_DATA
        )
    }

    private suspend fun sendBytesToService(
        requestBytes: ByteBuffer,
        dataMode: ValloxDataMode
    ): ValloxStatus? {

        var valloxStatus: ValloxStatus? = null

        httpClient.webSocket(
            method = HttpMethod.Get,
            host = host,
            port = 80,
            path = "/"
        ) {

            send(requestBytes.moveToByteArray())

            val responseBytes = incoming.receive().data

            valloxStatus = ValloxMessageHandler.readMessage(dataMode, responseBytes)
        }

        return valloxStatus
    }
}
