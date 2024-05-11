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

interface ValloxClient {

    suspend fun readStatus(): ValloxStatus?

    suspend fun turnOn()

    suspend fun turnOff()

    suspend fun switchProfile(profile: Profile)

    suspend fun setFanSpeed(profile: Profile, fanSpeed: Int)

    suspend fun setExtractFanBalanceBase(fanSpeed: Int)

    suspend fun setSupplyFanBalanceBase(fanSpeed: Int)

    suspend fun setFireplaceExtractFanSpeed(fanSpeed: Int)

    suspend fun setFireplaceSupplyFanSpeed(fanSpeed: Int)

    suspend fun setTargetTemperature(profile: Profile, targetTemperature: Int)

    suspend fun setBytesBoostTime(boostTimeInMinutes: Int)

    suspend fun setBoostTimerEnabled(enabled: Boolean)

    suspend fun setFireplaceTime(fireplaceTimeInMinutes: Int)

    suspend fun setFireplaceTimerEnabled(enabled: Boolean)

    suspend fun setWeeklyTimerEnabled(enabled: Boolean)

}
