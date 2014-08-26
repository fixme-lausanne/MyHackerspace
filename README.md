My Hackerspace
==============

- Status of hackerspaces using the [SpaceAPI](http://spaceapi.net)
- Show information about the space (contact, location, sensors, ...)
- Status widget, multiple widgets supported
- Available on [f-droid](https://f-droid.org/repository/browse/?fdid=ch.fixme.status) and [play store](https://play.google.com/store/apps/details?id=ch.fixme.status)

TODO
====

- Auto recognize field types in the API (array, obj, string, etc)

RELEASE NOTES
=============

1.7.4.1
-------

- Fix crash when there's no error message

1.7.4
---

- German translation (thanks to Lokke and Phervieux)
- Better hs list with alphabetical index
- Better errors messages
- Caching for http requests (images, hs directory)
- Add status message to the widget (thanks Fpletz)
- Fix bugs: widget updates, ignore ext fields, click from widget

1.7.3
-----

- Fix regression with widget custom open/close logo
- Fix order of hackerspaces with different cases

1.7.2
-----

- Better layout for sensors
- Support more fields for sensors (machines, names, properties)

1.7.1
-----

- Faster http requests (Use DefaultHttpClient instead of HttpURLConnection)

1.7
-----

- Full support of SpaceAPI 0.13, drops mixed api definition: hackerspaces must comply to the level they declare!
- Widget transparency preference added (by default transparency is deactivated)

1.6.1
-----

- French translation
- Fix the widget's image not updating
- Change to the new spaceapi url

1.6
---

- Better layout in general
- Use Holo light theme for Android >=3
- Refresh the current hackerspace
- Default to 15mn for the Widget
- Settings button to change the widget interval
- Fix lat/lon link
- Fix crash when maps/email app not found

1.5.1
-----

- Bug fixes
- Add a spinner when loading image
- Faster download

1.5
---

- Only download image if there is a change of state (better battery live and reduce network usage)

1.4
---

- Add Cam and Stream links if present
- Link for adresses opening GMaps
- Sort Hackerspaces by name
- Accept untrusted SSL certificates
- Better error reporting
- BUGFIX: Theme shoud be correct on all devices/versions
- BUGFIX: Should work after reboot correctly

1.3
---

- White theme by default (may break on samsung devices)
- Check if network is enabled
- Handle rotation correctly

