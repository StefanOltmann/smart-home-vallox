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

import io.ktor.util.moveToByteArray
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer

/**
 * This class is working with the ByteBuffer
 */
object ValloxMessageHandler {

    private const val ACK_FLAG = 245

    private var boostTime = 0
    private var boostTimerEnabled = false
    private var fireplaceTime = 0
    private var fireplaceTimerEnabled = false

    fun generateReadRequestBytes(): ByteArray =
        byteArrayOf(3, 0, -10, 0, 0, 0, -7, 0)

    fun generateWriteRequestBytesSwitchToProfile(profile: Profile): ByteArray {

        when (profile) {

            Profile.AT_HOME ->
                return generateWriteRequestBytes(
                    mapOf(
                        4609 to 0,
                        4612 to 0,
                        4613 to 0
                    )
                )

            Profile.AWAY ->
                return generateWriteRequestBytes(
                    mapOf(
                        4609 to 1,
                        4612 to 0,
                        4613 to 0
                    )
                )

            Profile.BOOST ->
                return generateWriteRequestBytes(
                    mapOf(
                        4612 to if (boostTime > 0) boostTime else 30,
                        4613 to 0
                    )
                )

            Profile.FIREPLACE ->
                return generateWriteRequestBytes(
                    mapOf(
                        4612 to 0,
                        4613 to if (fireplaceTime > 0) fireplaceTime else 15
                    )
                )
        }
    }

    fun generateWriteRequestBytesOnOff(updateState: Int) =
        generateWriteRequestBytes(4610, updateState)

    fun generateWriteRequestBytesFanSpeed(profile: Profile, fanSpeed: Int) =
        when (profile) {
            Profile.AT_HOME -> generateWriteRequestBytes(20507, fanSpeed)
            Profile.AWAY -> generateWriteRequestBytes(20501, fanSpeed)
            Profile.BOOST -> generateWriteRequestBytes(20513, fanSpeed)
            Profile.FIREPLACE -> throw IllegalArgumentException("Fireplace has no unique fan speed setting")
        }

    fun generateWriteRequestBytesExtractFanBalanceBase(fanSpeed: Int) =
        generateWriteRequestBytes(20485, fanSpeed)

    fun generateWriteRequestBytesSupplyFanBalanceBase(fanSpeed: Int) =
        generateWriteRequestBytes(20486, fanSpeed)

    fun generateWriteRequestBytesFireplaceExtractFanSpeed(fanSpeed: Int) =
        generateWriteRequestBytes(20487, fanSpeed)

    fun generateWriteRequestBytesFireplaceSupplyFanSpeed(fanSpeed: Int) =
        generateWriteRequestBytes(20488, fanSpeed)

    fun generateWriteRequestBytesTargetTemperature(profile: Profile, targetTemperature: Int) =
        when (profile) {
            Profile.AT_HOME -> generateWriteRequestBytes(20508, targetTemperature)
            Profile.AWAY -> generateWriteRequestBytes(20502, targetTemperature)
            Profile.BOOST -> generateWriteRequestBytes(20514, targetTemperature)
            Profile.FIREPLACE -> throw IllegalArgumentException("Fireplace does not have this setting")
        }

    fun generateWriteRequestBytesBoostTime(boostTime: Int): ByteArray {
        ValloxMessageHandler.boostTime = boostTime
        return generateWriteRequestBytes(20544, boostTime)
    }

    fun generateWriteRequestBytesBoostTimerEnabled(enabled: Boolean): ByteArray {
        boostTimerEnabled = enabled
        return generateWriteRequestBytes(21766, if (enabled) 1 else 0)
    }

    fun generateWriteRequestBytesFireplaceTime(fireplaceTime: Int): ByteArray {
        ValloxMessageHandler.fireplaceTime = fireplaceTime
        return generateWriteRequestBytes(20545, fireplaceTime)
    }

    fun generateWriteRequestBytesFireplaceTimerEnabled(enabled: Boolean): ByteArray {
        fireplaceTimerEnabled = enabled
        return generateWriteRequestBytes(21767, if (enabled) 1 else 0)
    }

    fun generateWriteRequestBytesWeeklyTimerEnabled(enabled: Boolean) =
        generateWriteRequestBytes(4615, if (enabled) 1 else 0)

    /**
     * Method to generate ByteArray request to be sent to vallox online websocket
     * to request or set data
     */
    private fun generateWriteRequestBytes(parameters: Map<Int, Int>): ByteArray {

        /* Parameters (key + value) + Mode + Checksum */
        val numberParameters = parameters.size * 2 + 2
        val capacity = (numberParameters + 1) * 2

        val byteBuffer =
            ByteBuffer.allocate(capacity).put(convertIntegerIntoByteArray(numberParameters))

        byteBuffer.put(convertIntegerIntoByteArray(ValloxDataMode.WRITE_DATA.value))

        var checksum = numberParameters + ValloxDataMode.WRITE_DATA.value

        for ((key, value) in parameters) {
            byteBuffer.put(convertIntegerIntoByteArray(key))
            byteBuffer.put(convertIntegerIntoByteArray(value))
            checksum += key + value
        }

        byteBuffer.put(convertIntegerIntoByteArray(checksum))
        byteBuffer.position(0)

        return byteBuffer.moveToByteArray()
    }

    private fun generateWriteRequestBytes(key: Int, value: Int) =
        generateWriteRequestBytes(mapOf(key to value))

    private fun convertIntegerIntoByteArray(integer: Int) =
        byteArrayOf((integer and 0xff).toByte(), (integer shr 8 and 0xff).toByte())

    fun readMessage(dataMode: ValloxDataMode, bytes: ByteArray): ValloxStatus? {

        /* Initial sanity check. */
        if (!(bytes.size > 5 && bytes.size % 2 == 0))
            throw ValloxException("Response corrupted, length: ${bytes.size} bytes")

        /* Verify responses to requests */
        return if (dataMode == ValloxDataMode.WRITE_DATA || dataMode == ValloxDataMode.READ_DATA) {

            checkMessageBytesForWriteDataResponse(dataMode, bytes)

            /* If the check call triggered no exceptions everything went well. */
            null

        } else if (dataMode == ValloxDataMode.READ_TABLES) {

            parseBytesIntoValloxStatus(dataMode, bytes)

        } else
            throw ValloxException("Vallox command $dataMode not implemented")
    }

    /**
     * Checks if message is a valid response to the data write and if it's acknowledged
     */
    private fun checkMessageBytesForWriteDataResponse(dataMode: ValloxDataMode, bytes: ByteArray) {

        /* Just make sure this is not called with something else. */
        require(dataMode == ValloxDataMode.WRITE_DATA || dataMode == ValloxDataMode.READ_DATA)

        val dataLength = bytes.size / 2

        var checksum = 0

        val dataArray = IntArray(dataLength)

        for (index in 0 until dataLength)
            dataArray[index] = getNumberLE(bytes, index * 2)

        for (index in 0 until dataLength - 1)
            checksum += dataArray[index]

        checksum = checksum and 0xffff

        if (dataArray[0] != dataLength - 1 || dataArray[dataLength - 1] != checksum)
            throw ValloxException("Response corrupted, Data length or Checksum do not match")

        if (dataMode == ValloxDataMode.WRITE_DATA) {

            if (dataArray[1] != ACK_FLAG)
                throw ValloxException("Write data failed.")

            return
        }

        throw ValloxException("Vallox command $dataMode not implemented")
    }

    private fun parseBytesIntoValloxStatus(
        dataMode: ValloxDataMode,
        bytes: ByteArray
    ): ValloxStatus {

        /* Just make sure this is not called with something else. */
        require(dataMode == ValloxDataMode.READ_TABLES)

        val dataLength = bytes.size / 2

        if (dataLength < 705)
            throw ValloxException("Response corrupted, data table response not complete.")

        /* These values are needed for the next status evaluation. */
        boostTime = getNumberBE(bytes, 492)
        boostTimerEnabled = getBoolean(bytes, 528)
        fireplaceTime = getNumberBE(bytes, 494)
        fireplaceTimerEnabled = getBoolean(bytes, 530)

        /* Create and set new Status object */
        return ValloxStatus(

            /* Profile */
            profile = getNumberBE(bytes, 214),

            /* Current Fan Speed */
            fanSpeedPercentage = getNumberBE(bytes, 128),
            fanSpeedExtract = getNumberBE(bytes, 144),
            fanSpeedSupply = getNumberBE(bytes, 146),

            /* Current Temperatures */
            tempInside = getTemperature(bytes, 130),
            tempExhaust = getTemperature(bytes, 132),
            tempOutside = getTemperature(bytes, 134),
            tempIncomingBeforeHeating = getTemperature(bytes, 136),
            tempIncoming = getTemperature(bytes, 138),

            /* Humidity and Co2 */
            humidity = getNumberBE(bytes, 148),
            co2 = getNumberBE(bytes, 150),

            /* Timer */
            boostTimer = getNumberBE(bytes, 220),
            fireplaceTimer = getNumberBE(bytes, 222),

            /* Uptime Status */
            cellState = getNumberBE(bytes, 228),
            uptimeYears = getNumberBE(bytes, 230),
            uptimeHours = getNumberBE(bytes, 232),
            uptimeHoursCurrent = getNumberBE(bytes, 234),

            /* Filter Status */
            remainingTimeForFilter = getNumberBE(bytes, 236),
            filterChangedDateDay = getNumberBE(bytes, 496),
            filterChangedDateMonth = getNumberBE(bytes, 498) + 1,
            filterChangedDateYear = getNumberBE(bytes, 500),

            /* Fan Balance Base */
            extractFanBalanceBase = getNumberBE(bytes, 374),
            supplyFanBalanceBase = getNumberBE(bytes, 376),

            /* Fan Speed Setting */
            homeSpeedSetting = getNumberBE(bytes, 418),
            awaySpeedSetting = getNumberBE(bytes, 406),
            boostSpeedSetting = getNumberBE(bytes, 430),
            fireplaceExtractFan = getNumberBE(bytes, 378),
            fireplaceSupplyFan = getNumberBE(bytes, 380),

            /* Temperature Target */
            homeAirTempTarget = getTemperature(bytes, 420),
            awayAirTempTarget = getTemperature(bytes, 408),
            boostAirTempTarget = getTemperature(bytes, 432),

            /* Boost Timer */
            boostTime = getNumberBE(bytes, 492),
            boostTimerEnabled = getBoolean(bytes, 528),

            /* Fireplace Timer */
            fireplaceTime = getNumberBE(bytes, 494),
            fireplaceTimerEnabled = getBoolean(bytes, 530),

            /* Weekly Timer */
            weeklyTimerEnabled = getBoolean(bytes, 226)
        )
    }

    private fun getNumberBE(bytes: ByteArray, pos: Int) =
        bytes[pos].toInt() and 0xff shl 8 or (bytes[pos + 1].toInt() and 0xff)

    private fun getNumberLE(bytes: ByteArray, pos: Int) =
        bytes[pos].toInt() and 0xff or (bytes[pos + 1].toInt() and 0xff shl 8)

    private fun getTemperature(bytes: ByteArray, pos: Int) =
        convertCentiKelvinToCelsius(getNumberBE(bytes, pos))

    private fun getBoolean(bytes: ByteArray, pos: Int) =
        getNumberBE(bytes, pos) == 1

    private fun convertCentiKelvinToCelsius(temperatureCentiKelvin: Int) =
        (temperatureCentiKelvin - 27315) / 100.0
}
