[![PIA logo][pia-image]][pia-url]

# Private Internet Access
Private Internet Access is the world's leading consumer VPN service. At Private Internet Access we believe in unfettered access for all, and as a firm supporter of the open source ecosystem we have made the decision to open source our VPN clients. For more information about the PIA service, please visit our website [privateinternetaccess.com](https://privateinternetaccess.com).

## Installation

### Requirements
 - Git (latest)
 - Android Studio (Stable channel)
 - Gradle (latest)
 - ADB installed
 - NDK (latest)
 - Android 4.1+


Please use these instructions to install Git on your computer if it is not already installed: [Installing Git](https://gist.github.com/derhuerst/1b15ff4652a867391f03)

Please use these instructions to install and download Android Studio on your computer if it is not already installed: [Android Studio Download Link](https://developer.android.com/studio/index.html)

#### Download Codebase
Using the terminal:

`git clone https://github.com/pia-foss/mobile-android.git`

type in what folder you want to put in without the ** or use a graphical interface like Android Studio's to clone the repo.

This will pull the main repository as well as the required submodules and initialize them.

Once the initial pull is done, update the submodules recursively:

`git submodule sync --recursive`

`git submodule update --init --recursive`

Alternatively, you can use our gowrapper around the git commands to sync the submodules.

`go run pia.go --sync`

#### Building

Once the project is cloned and submodules are updated, the project will build once opened in Android Studio. This will require building the binaries for the underlying modules and configurations. This can take a while for certain computers and is only done on complete clean and rebuilds. You can see progress in the gradle console. Once completed, the app will be able to be run on a device connected to the computer or an emulator running on your computer.

*Please note that Android 4.x emulators are broken with VPNs and will not connect.*

## Documentation

#### Architecture

`com.privateinternetaccess.android` is the package name for the PIA section of the app.

`de.blinki.openvpn` contains all of PIA VPN's OpenVPN implementation.

All similar UI elements and model classes are keep together. All activities and fragments are keep together by the area of the app they are in.

Code structure via packages:

* `ui` - application UI classes and elements
* `model` - application model elements including handlers, interfaces, events and POJOs.
* `pia` - API, handlers, interfaces, model and tasks relating to Private Internet Access account, server and connection information
* `tunnel` - Bridging elements with OpenVPN.
* `receivers` - Broadcast receivers used in PIA VPN.

#### Coding Style

PIA VPN is built using a similar style to MVI (Model View Intent) using an Event bus to communicate between different layers of the app. We use [GreenRobot's Eventbus](https://github.com/greenrobot/EventBus) in order to communicate events throughout the app and [OkHttp](http://square.github.io/okhttp/) for web calls. Our naming structure puts the class type or topic a class is in at the end of the name.

### Contributing

By contributing to this project you are agreeing to the terms stated in the Contributor License Agreement (CLA) [here](/CLA.rst).

For more details please see [CONTRIBUTING](/CONTRIBUTING.md).

Issues and Pull Requests should use these templates: [ISSUE](/.github/ISSUE_TEMPLATE.md) and [PULL REQUEST](/.github/PULL_REQUEST_TEMPLATE.md).

### Testing

In order to run the instrumented test we need to set environment variables containing valid credentials.
They need to have the format `PIA_VALID_USERNAME`, `PIA_VALID_PASSWORD` and `PIA_VALID_DIP_TOKEN`.

### License

This project is licensed under the [MIT (Expat) license](https://choosealicense.com/licenses/mit/), which can be found [here](/LICENSE).

### Acknowledgements

This product includes software developed by the OpenSSL Project for use in the OpenSSL Toolkit. (https://www.openssl.org/)

© 2002-2017 OpenVPN Inc. - OpenVPN is a registered trademark of OpenVPN Inc.


<!-- Markdown link & img dfn's -->
[pia-image]: https://assets-cms.privateinternetaccess.com/img/frontend/pia_menu_logo_light.svg
[pia-url]: https://www.privateinternetaccess.com/
[wiki]: https://en.wikipedia.org/wiki/Private_Internet_Access
