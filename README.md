# Stefans Smart Home Vallox Client

[![CI](https://github.com/StefanOltmann/smart-home-vallox/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/StefanOltmann/smart-home-vallox/actions/workflows/ci.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=smart-home-vallox&metric=alert_status)](https://sonarcloud.io/dashboard?id=smart-home-vallox)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=smart-home-vallox&metric=coverage)](https://sonarcloud.io/dashboard?id=smart-home-vallox)
[![EPL-2.0](https://img.shields.io/badge/license-EPL%202-green.svg)](https://opensource.org/licenses/EPL-2.0)

This is a client that controls Vallox MV ventilation units.

## Motivation

I own a **Vallox ValloPlus 270 MV** and wanted to connect it to my own [smart home system](https://github.com/StefanOltmann/smart-home-server).

For this use case I needed a simple and clean Java/Kotlin client.

## Credits

This project is based on the work of [Björn Brings](https://github.com/bjoernbrings) for the [OpenHab ValloxMV Binding](https://github.com/bjoernbrings/openhab-addons/tree/main/bundles/org.openhab.binding.valloxmv).

I extracted the communication logic, converted everything to Kotlin and refactored it to be usable without OpenHab.

## Supported devices

- Vallox 270 MV
- Vallox 350 MV
- Vallox 510 MV

## Usage

This is a sample how to getting started.

```
val uri = ValloxClient.createURI("192.168.0.31")

val valloxClient: ValloxClient = ValloxClientImpl(uri)

/* Set Profile to "At home" */
valloxClient.switchProfile(ValloxProfile.AT_HOME)

/* Set fan speed of "At home" profile to 50% */
valloxClient.setFanSpeed(ValloxProfile.AT_HOME, 50)

/* Set target temperature of "At home" profile to 21° C */
valloxClient.setTargetTemperature(ValloxProfile.AT_HOME, 21)

val valloxStatus = valloxClient.readStatus()

println("Returned Status: $valloxStatus")
```

The interface `ValloxClient` has many more methods.

## Dependencies

If you want to use it you need these additional dependencies on your classpath.

```
implementation "org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.10"
implementation "org.eclipse.jetty.websocket:websocket-jetty-client:11.0.3"
implementation "org.slf4j:slf4j-simple:1.7.30"
```
