# passfx
PassFx is a password manager written in [Kotlin](https://github.com/JetBrains/kotlin) and [TornadoFX](https://github.com/edvin/tornadofx).
You can use this program to store account credentials for a variety of services you may use.
It's open-source, cross-platform, and can be used without an internet connection.

## About
PassFx was originally a fork of the [UPM project](https://github.com/adrian/upm-swing).
Although most of the code has been rewritten, some of the original classes remain under the "net.upm" package.
The intentions for this program are solely educational.

## Building
This project uses Gradle as its build tool. In order to build and run, you can follow the following steps.<br>
1. Run "gradlew.bat"
2. Run the command "./gradlew run"

## Screenshots
<img src="https://i.imgur.com/V5jTpd4.png" width="500">
<br/>

<img src="https://i.imgur.com/DeT5Adf.png" width="450">
<br/>

<img src="https://i.imgur.com/qVmvhIY.png" width="450">
<br/>

<img src="https://i.imgur.com/afBMESq.png" width="450">
<br/>

<img src="https://i.imgur.com/r37U5Ur.png" width="450">
<br/>

## TODO
Some of the app's functionality is missing or the code can be improved in the following ways.<br>
1. Dark theme
2. Release stable version as executable
3. Documentation

## Bugs
Some known bugs:<br>
1. Enter on notes field closes view when editing
2. clearProgress() throws an exception when a task finishes after MainView has been closed