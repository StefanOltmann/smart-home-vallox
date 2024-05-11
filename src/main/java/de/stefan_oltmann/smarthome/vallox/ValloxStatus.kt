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

data class ValloxStatus(

    /* Profile */
    val profile: Int,

    /* Current Fan Speed */
    val fanSpeedPercentage: Int,
    val fanSpeedExtract: Int,
    val fanSpeedSupply: Int,

    /* Current Temperatures */
    val tempInside: Double,
    val tempExhaust: Double,
    val tempOutside: Double,
    val tempIncomingBeforeHeating: Double,
    val tempIncoming: Double,

    /* Humidity and Co2 */
    val humidity: Int,
    val co2: Int,

    /* Timer */
    val boostTimer: Int,
    val fireplaceTimer: Int,

    /* Uptime Status */
    val cellState: Int,
    val uptimeYears: Int,
    val uptimeHours: Int,
    val uptimeHoursCurrent: Int,

    /* Filter Status */
    val remainingTimeForFilter: Int,
    val filterChangedDateDay: Int,
    val filterChangedDateMonth: Int,
    val filterChangedDateYear: Int,

    /* Fan Balance Base */
    val extractFanBalanceBase: Int,
    val supplyFanBalanceBase: Int,

    /* Fan Speed Setting */
    val homeSpeedSetting: Int,
    val awaySpeedSetting: Int,
    val boostSpeedSetting: Int,
    val fireplaceExtractFan: Int,
    val fireplaceSupplyFan: Int,

    /* Temperature Target */
    val homeAirTempTarget: Double,
    val awayAirTempTarget: Double,
    val boostAirTempTarget: Double,

    /* Boost Timer */
    val boostTime: Int,
    val boostTimerEnabled: Boolean,

    /* Fireplace Timer */
    val fireplaceTime: Int,
    val fireplaceTimerEnabled: Boolean,

    /* Weekly Timer */
    val weeklyTimerEnabled: Boolean

) {

    @Suppress("Unused")
    fun toPrettyString() = """
        ### Vallox status
        Profile # : $profile
        Fan speed : $fanSpeedPercentage %
        Humidity  : $humidity %
        Inside    : $tempInside °C
        Outside   : $tempOutside °C
        Incoming  : $tempIncoming °C ($tempIncomingBeforeHeating °C before heating)
        Exhaust   : $tempExhaust °C
    """.trimIndent()
}
