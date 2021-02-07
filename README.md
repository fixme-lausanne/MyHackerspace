# My Hackerspace

<a href="https://play.google.com/store/apps/details?id=ch.fixme.status"><img width="200" src="https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png" alt="Get it on Google Play"></a>
<a href="https://f-droid.org/repository/browse/?fdid=ch.fixme.status"><img width="200" src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" alt="Get it on F-Droid"></a>

This is an Android app with the following featuress:

- Status of hackerspaces using the [SpaceAPI](https://spaceapi.io/)
- Show information about the space (contact, location, sensors, ...)
- Status widget, multiple widgets supported

The app was originally developed in 2012 by [@rorist] from [FIXME Lausanne]. In
2021, the app was transferred to the [SpaceAPI community repositories] and is
now mainly being developed by members of [Coredump].

[@rorist]: https://github.com/rorist
[FIXME Lausanne]: https://fixme.ch/
[SpaceAPI community repositories]: https://github.com/spaceapi-community/
[Coredump]: https://www.coredump.ch/

## How to Compile

First, get the sources.

    git clone --recursive https://github.com/spaceapi-community/my-hackerspace.git
    cd my-hackerspace

### Android Studio

With Android Studio, simply open the project directory and you should be set.

### Command Line

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

## Local Directory

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

## TODO

- Auto recognize field types in the API (array, obj, string, etc)
- Integrate woozzu library as 3rd party
