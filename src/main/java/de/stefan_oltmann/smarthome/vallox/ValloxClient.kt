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

import java.net.URI

interface ValloxClient {

    fun readStatus(): ValloxStatus

    fun turnOn()

    fun turnOff()

    fun switchProfile(profile: ValloxProfile)

    fun setFanSpeed(profile: ValloxProfile, fanSpeed: Int)

    fun setExtractFanBalanceBase(fanSpeed: Int)

    fun setSupplyFanBalanceBase(fanSpeed: Int)

    fun setFireplaceExtractFanSpeed(fanSpeed: Int)

    fun setFireplaceSupplyFanSpeed(fanSpeed: Int)

    fun setTargetTemperature(profile: ValloxProfile, targetTemperature: Int)

    fun setBytesBoostTime(boostTimeInMinutes: Int)

    fun setBoostTimerEnabled(enabled: Boolean)

    fun setFireplaceTime(fireplaceTimeInMinutes: Int)

    fun setFireplaceTimerEnabled(enabled: Boolean)

    fun setWeeklyTimerEnabled(enabled: Boolean)

    companion object {

        fun createURI(ip: String) = URI("ws://$ip:80")

    }
}
