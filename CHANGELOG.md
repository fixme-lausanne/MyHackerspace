RELEASE NOTES
=============

2017-01-XX / 1.8.3
---

    - Change links from SpaceAPI.net to SpaceDirectory.org
    - Display all webcams

2016-07-02 / 1.8.2
---

    - Fix camera and stream url being displayed
    - Fix twitter link to the new url format

2016-05-06 / 1.8.1
---

    - Uses custom API directory end point (https://spaceapi.fixme.ch/directory.json)
    - Allow editing of the API directory end point and the current hackerspace API
    - Add Danish translation (thanks Mikkel)

2016-04-14 / 1.8
---

    - Supports invalid SSL certificates
    - Allow widget to be resized
    - Add Dutch translation
    - Fix http to https redirection
    - General fixes

2014-08-26 / 1.7.4.1
-------

    - Fix crash when there's no error message

2014-08-07 / 1.7.4
---

    - German translation (thanks to Lokke and Phervieux)
    - Better hs list with alphabetical index
    - Better errors messages
    - Caching for http requests (images, hs directory)
    - Add status message to the widget (thanks Fpletz)
    - Fix bugs: widget updates, ignore ext fields, click from widget

2013-10-25 / 1.7.3
-----

    - Fix regression with widget custom open/close logo
    - Fix order of hackerspaces with different cases

2013-09-09 / 1.7.2
-----

    - Better layout for sensors
    - Support more fields for sensors (machines, names, properties)

2013-09-06 / 1.7.1
-----

    - Faster http requests (Use DefaultHttpClient instead of HttpURLConnection)

2013-09-05 / 1.7
-----

    - Full support of SpaceAPI 0.13, drops mixed api definition: hackerspaces must comply to the level they declare!
    - Widget transparency preference added (by default transparency is deactivated)

2013-06-04 / 1.6.1
-----

    - French translation
    - Fix the widget's image not updating
    - Change to the new spaceapi url

2013-01-02 / 1.6
---

    - Better layout in general
    - Use Holo light theme for Android >=3
    - Refresh the current hackerspace
    - Default to 15mn for the Widget
    - Settings button to change the widget interval
    - Fix lat/lon link
    - Fix crash when maps/email app not found

2012-10-29 / 1.5.1
-----

    - Bug fixes
    - Add a spinner when loading image
    - Faster download

2012-05-19 / 1.5
---

    - Only download image if there is a change of state (better battery live and reduce network usage)

2012-05-15 / 1.4
---

    - Add Cam and Stream links if present
    - Link for adresses opening GMaps
    - Sort Hackerspaces by name
    - Accept untrusted SSL certificates
    - Better error reporting
    - BUGFIX: Theme shoud be correct on all devices/versions
    - BUGFIX: Should work after reboot correctly

2012-05-08 / 1.3
---

    - White theme by default (may break on samsung devices)
    - Check if network is enabled
    - Handle rotation correctly

2012-05-06 / 1.2
2012-05-04 / 1.1
2012-04-29 / 1.0
---

    - Initial release
