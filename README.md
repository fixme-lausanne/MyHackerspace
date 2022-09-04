My Hackerspace
==============

>  **Discontinued** The current development takes place at [spaceapi-community/my-hackerspace](https://github.com/spaceapi-community/my-hackerspace).

- Status of hackerspaces using the [SpaceAPI](https://spaceapi.io/)
- Show information about the space (contact, location, sensors, ...)
- Status widget, multiple widgets supported
- Available on [f-droid](https://f-droid.org/repository/browse/?fdid=ch.fixme.status) and [play store](https://play.google.com/store/apps/details?id=ch.fixme.status)

Master branch: [![Build Status](https://travis-ci.org/fixme-lausanne/MyHackerspace.svg?branch=master)](https://travis-ci.org/fixme-lausanne/MyHackerspace)

HOW TO COMPILE
=============

First, get the sources.

    git clone --recursive https://github.com/fixme-lausanne/MyHackerspace.git
    cd MyHackerspace

Get the 3rd party librairies

    git submodule init
    git submodule update

Android Studio
--------------

With Android Studio, simply open the project directory and you should be set.

Command Line
------------

You can build the project using Gradle.

You'll first need the Android SDK, and install build tools 21.1.1 which is considered obsolete.
You can find this version by ticking obsolete in the Android SDK Manager.

The following examples use the gradle wrapper script which will automatically
download gradle to your local directory. If you want to use your own
system-wide installation instead, simply replace `./gradlew` commands with
`gradle`.

First, copy `local.properties.example` to `local.properties` and adjust the
path to your Android SDK installation.

To build a debug APK:

    ./gradlew assembleDebug

You will find your APK file in the ` app/build/outputs/apk/` directory.

You can also build and directly install the file to your connected smartphone:

    ./gradlew assembleDebug installDebug

To see other tasks that gradle offers, run

    ./gradlew tasks

LOCAL DIRECTORY
===============

For testing purposes you can run a local directory using this technique:

* Create a new Android AVD called for instance "android6"
* Start the AVD from the command line:
    `emulator -avd android6 -shared-net-id 16`
* Make sure a network interface on your host computer is reachable by this IP:
    `sudo ifconfig eth0 10.0.2.3`
* On the host, go to the test directory and run the serv.py script:
    `./serv.py`
* Go in the app preferences and set the OpenSpaceDirectory URL to the following:
    `https://10.0.2.3:8443/directory.json`

TODO
====

- Auto recognize field types in the API (array, obj, string, etc)
- Integrate woozzu library as 3rd party

