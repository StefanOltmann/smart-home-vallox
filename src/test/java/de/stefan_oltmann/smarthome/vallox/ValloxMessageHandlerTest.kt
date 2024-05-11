package de.stefan_oltmann.smarthome.vallox

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.nio.ByteBuffer
import java.util.Arrays

/**
 * The purpose of this test is to "freeze" the byte arrays that have
 * been validated to work for certain actions. No refactoring should
 * change the resulting byte arrays and lead to errors.
 */
class ValloxMessageHandlerTest {

    private fun ByteArray.asByteArrayString() = toList().toString()

    @Test
    fun testGenerateReadRequestBytes() {

        assertEquals(
            "[3, 0, -10, 0, 0, 0, -7, 0]",
            ValloxMessageHandler.generateReadRequestBytes().asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesSwitchToProfile() {

        assertEquals(
            "[8, 0, -7, 0, 1, 18, 0, 0, 4, 18, 0, 0, 5, 18, 0, 0, 11, 55]",
            ValloxMessageHandler.generateWriteRequestBytesSwitchToProfile(Profile.AT_HOME)
                .asByteArrayString()
        )

        assertEquals(
            "[8, 0, -7, 0, 1, 18, 1, 0, 4, 18, 0, 0, 5, 18, 0, 0, 12, 55]",
            ValloxMessageHandler.generateWriteRequestBytesSwitchToProfile(Profile.AWAY)
                .asByteArrayString()
        )

        assertEquals(
            "[6, 0, -7, 0, 4, 18, 30, 0, 5, 18, 0, 0, 38, 37]",
            ValloxMessageHandler.generateWriteRequestBytesSwitchToProfile(Profile.BOOST)
                .asByteArrayString()
        )

        assertEquals(
            "[6, 0, -7, 0, 4, 18, 0, 0, 5, 18, 60, 0, 68, 37]",
            ValloxMessageHandler.generateWriteRequestBytesSwitchToProfile(Profile.FIREPLACE)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesOnOff() {

        assertEquals(
            "[4, 0, -7, 0, 2, 18, 0, 0, -1, 18]",
            ValloxMessageHandler.generateWriteRequestBytesOnOff(0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 2, 18, 1, 0, 0, 19]",
            ValloxMessageHandler.generateWriteRequestBytesOnOff(1)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesFanSpeed() {

        assertEquals(
            "[4, 0, -7, 0, 27, 80, 0, 0, 24, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.AT_HOME, 0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 27, 80, 50, 0, 74, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.AT_HOME, 50)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 27, 80, 100, 0, 124, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.AT_HOME, 100)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 21, 80, 0, 0, 18, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.AWAY, 0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 21, 80, 50, 0, 68, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.AWAY, 50)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 21, 80, 100, 0, 118, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.AWAY, 100)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 33, 80, 0, 0, 30, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.BOOST, 0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 33, 80, 50, 0, 80, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.BOOST, 50)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 33, 80, 100, 0, -126, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.BOOST, 100)
                .asByteArrayString()
        )

        assertThrows<IllegalArgumentException> {
            ValloxMessageHandler.generateWriteRequestBytesFanSpeed(Profile.FIREPLACE, 0)
        }
    }

    @Test
    fun testGenerateWriteRequestBytesExtractFanBalanceBase() {

        assertEquals(
            "[4, 0, -7, 0, 5, 80, 0, 0, 2, 81]",
            ValloxMessageHandler.generateWriteRequestBytesExtractFanBalanceBase(0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 5, 80, 50, 0, 52, 81]",
            ValloxMessageHandler.generateWriteRequestBytesExtractFanBalanceBase(50)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 5, 80, 100, 0, 102, 81]",
            ValloxMessageHandler.generateWriteRequestBytesExtractFanBalanceBase(100)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesSupplyFanBalanceBase() {

        assertEquals(
            "[4, 0, -7, 0, 6, 80, 0, 0, 3, 81]",
            ValloxMessageHandler.generateWriteRequestBytesSupplyFanBalanceBase(0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 6, 80, 50, 0, 53, 81]",
            ValloxMessageHandler.generateWriteRequestBytesSupplyFanBalanceBase(50)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 6, 80, 100, 0, 103, 81]",
            ValloxMessageHandler.generateWriteRequestBytesSupplyFanBalanceBase(100)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesFireplaceExtractFanSpeed() {

        assertEquals(
            "[4, 0, -7, 0, 7, 80, 0, 0, 4, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceExtractFanSpeed(0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 7, 80, 50, 0, 54, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceExtractFanSpeed(50)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 7, 80, 100, 0, 104, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceExtractFanSpeed(100)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesFireplaceSupplyFanSpeed() {

        assertEquals(
            "[4, 0, -7, 0, 8, 80, 0, 0, 5, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceSupplyFanSpeed(0)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 8, 80, 50, 0, 55, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceSupplyFanSpeed(50)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 8, 80, 100, 0, 105, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceSupplyFanSpeed(100)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesTargetTemperature() {

        assertEquals(
            "[4, 0, -7, 0, 28, 80, 5, 0, 30, 81]",
            ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(Profile.AT_HOME, 5)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 28, 80, 25, 0, 50, 81]",
            ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(Profile.AT_HOME, 25)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 22, 80, 5, 0, 24, 81]",
            ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(Profile.AWAY, 5)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 22, 80, 25, 0, 44, 81]",
            ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(Profile.AWAY, 25)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 34, 80, 5, 0, 36, 81]",
            ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(Profile.BOOST, 5)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 34, 80, 25, 0, 56, 81]",
            ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(Profile.BOOST, 25)
                .asByteArrayString()
        )

        assertThrows<IllegalArgumentException> {
            ValloxMessageHandler.generateWriteRequestBytesTargetTemperature(Profile.FIREPLACE, 5)
        }
    }

    @Test
    fun testGenerateWriteRequestBytesBoostTime() {

        assertEquals(
            "[4, 0, -7, 0, 64, 80, 15, 0, 76, 81]",
            ValloxMessageHandler.generateWriteRequestBytesBoostTime(15)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 64, 80, 60, 0, 121, 81]",
            ValloxMessageHandler.generateWriteRequestBytesBoostTime(60)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesBoostTimerEnabled() {

        assertEquals(
            "[4, 0, -7, 0, 6, 85, 1, 0, 4, 86]",
            ValloxMessageHandler.generateWriteRequestBytesBoostTimerEnabled(true)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 6, 85, 0, 0, 3, 86]",
            ValloxMessageHandler.generateWriteRequestBytesBoostTimerEnabled(false)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesFireplaceTime() {

        assertEquals(
            "[4, 0, -7, 0, 65, 80, 15, 0, 77, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceTime(15)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 65, 80, 60, 0, 122, 81]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceTime(60)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesFireplaceTimerEnabled() {

        assertEquals(
            "[4, 0, -7, 0, 7, 85, 1, 0, 5, 86]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceTimerEnabled(true)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 7, 85, 0, 0, 4, 86]",
            ValloxMessageHandler.generateWriteRequestBytesFireplaceTimerEnabled(false)
                .asByteArrayString()
        )
    }

    @Test
    fun testGenerateWriteRequestBytesWeeklyTimerEnabled() {

        assertEquals(
            "[4, 0, -7, 0, 7, 18, 1, 0, 5, 19]",
            ValloxMessageHandler.generateWriteRequestBytesWeeklyTimerEnabled(true)
                .asByteArrayString()
        )

        assertEquals(
            "[4, 0, -7, 0, 7, 18, 0, 0, 4, 19]",
            ValloxMessageHandler.generateWriteRequestBytesWeeklyTimerEnabled(false)
                .asByteArrayString()
        )
    }

    @Test
    fun testReadMessage() {

        val inputStream = ValloxMessageHandler.javaClass.getResourceAsStream("test_message.txt")

        assertNotNull(inputStream)
        checkNotNull(inputStream)

        val inputAsString = inputStream.bufferedReader().use { it.readText() }

        assertNotNull(inputAsString)

        val inputAsStringTrimmed = inputAsString.replace("\r\n", "").trim()

        val bytes = inputAsStringTrimmed.split(", ").map { Integer.parseInt(it).toByte() }

        assertEquals(1410, bytes.size)

        val byteArray = bytes.toByteArray()

        val valloxStatus = ValloxMessageHandler.readMessage(ValloxDataMode.READ_TABLES, byteArray)

        assertNotNull(valloxStatus)
        checkNotNull(valloxStatus)

        /* Profile */
        assertEquals(0, valloxStatus.profile)

        /* Current Fan Speed */
        assertEquals(50, valloxStatus.fanSpeedPercentage)
        assertEquals(1572, valloxStatus.fanSpeedExtract)
        assertEquals(1522, valloxStatus.fanSpeedSupply)

        /* Current Temperatures */
        assertEquals(22.87, valloxStatus.tempInside)
        assertEquals(12.61, valloxStatus.tempExhaust)
        assertEquals(11.44, valloxStatus.tempOutside)
        assertEquals(18.71, valloxStatus.tempIncomingBeforeHeating)
        assertEquals(19.18, valloxStatus.tempIncoming)

        /* Humidity and Co2 */
        assertEquals(50, valloxStatus.humidity)
        assertEquals(0, valloxStatus.co2)

        /* Timer */
        assertEquals(0, valloxStatus.boostTimer)
        assertEquals(0, valloxStatus.fireplaceTimer)

        /* Uptime Status */
        assertEquals(0, valloxStatus.cellState)
        assertEquals(3, valloxStatus.uptimeYears)
        assertEquals(5478, valloxStatus.uptimeHours)
        assertEquals(6549, valloxStatus.uptimeHoursCurrent)

        /* Filter Status */
        assertEquals(0, valloxStatus.remainingTimeForFilter)
        assertEquals(1, valloxStatus.filterChangedDateDay)
        assertEquals(10, valloxStatus.filterChangedDateMonth)
        assertEquals(20, valloxStatus.filterChangedDateYear)

        /* Fan Balance Base */
        assertEquals(100, valloxStatus.extractFanBalanceBase)
        assertEquals(100, valloxStatus.supplyFanBalanceBase)

        /* Fan Speed Setting */
        assertEquals(50, valloxStatus.homeSpeedSetting)
        assertEquals(100, valloxStatus.awaySpeedSetting)
        assertEquals(100, valloxStatus.boostSpeedSetting)
        assertEquals(0, valloxStatus.fireplaceExtractFan)
        assertEquals(0, valloxStatus.fireplaceSupplyFan)

        /* Temperature Target */
        assertEquals(21.0, valloxStatus.homeAirTempTarget)
        assertEquals(19.0, valloxStatus.awayAirTempTarget)
        assertEquals(5.0, valloxStatus.boostAirTempTarget)

        /* Boost Timer */
        assertEquals(30, valloxStatus.boostTime)
        assertTrue(valloxStatus.boostTimerEnabled)

        /* Fireplace Timer */
        assertEquals(240, valloxStatus.fireplaceTime)
        assertTrue(valloxStatus.fireplaceTimerEnabled)

        /* Weekly Timer */
        assertFalse(valloxStatus.weeklyTimerEnabled)
    }
}
