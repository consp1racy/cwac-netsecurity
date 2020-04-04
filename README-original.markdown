# CWAC-NetSecurity: Simplifying Secure Internet Access

This library contains a backport of
[the Android 7.0 network security configuration](https://developer.android.com/training/articles/security-config.html)
subsystem. In Android 7.0, this subsystem makes it easier for developers
to tie their app to particular certificate authorities or certificates,
support self-signed certificates, and handle other advanced SSL
certificate scenarios. This backport allows the same XML configuration
to be used, going back to API Level 17 (Android 4.2).

This library also offers a `TrustManagerBuilder` and related classes
to make it easier for developers to integrate the network security
configuration backport, particularly for
[OkHttp3](https://github.com/square/okhttp)
and `HttpURLConnection`.

This library also includes support for certificate memorization, where you
can elect to trust certificates discovered "in the wild", either automatically
or with user approval.

Note that OkHttp has its own `CertificatePinner`. If all you are looking to do is pin certificates, and you are usign OkHttp (and you should be!), use `CertificatePinner` and do not use CWAC-NetSecurity. Conversely, if there are [advanced features of CWAC-NetSecurity](https://github.com/commonsguy/cwac-netsecurity/blob/master/docs/ADVANCED_USAGE.markdown) that you wish to use, that's great... but do not also use `CertificatePinner`. Use one or the other, not both.

## Installation

The artifact for this library is distributed via the CWAC repository,
so you will need to configure that in your module's `build.gradle` file,
along with your `compile` statement:

```groovy
repositories {
    maven {
        url "https://s3.amazonaws.com/repo.commonsware.com"
    }
}

dependencies {
    implementation 'com.commonsware.cwac:netsecurity:0.5.0'
    implementation 'com.squareup.okhttp3:okhttp:3.9.1'
}
```

If you are using this library with OkHttp3, you also need to have
an `implementation` statement for a compatible OkHttp3 artifact, as shown
above. Right now, the most recent compatible OkHttp3 version is 3.9.1.

If you are using `HttpURLConnection`, or tying this code into some
other HTTP client stack, you can skip the OkHttp3 dependency.

## Basic Usage

Start by following
[Google's documentation for the Android 7.0 network security configuration](https://developer.android.com/training/articles/security-config.html).
Ideally, confirm that your configuration works using an Android 7.0+
device.

Next, add in this `<meta-data>` element to your manifest, as a child
of the `<application>` element:

```xml
<meta-data
  android:name="android.security.net.config"
  android:resource="@xml/net_security_config" />
```

The value for `android:resource` should be the same XML resource that
you used in the `android:networkSecurityConfig` attribute in the
`<application>` element.

Then, in your code where you want to set up your network communications,
create a `TrustManagerBuilder` and teach it to load the configuration
from the manifest:

```java
TrustManagerBuilder tmb=
  new TrustManagerBuilder().withManifestConfig(ctxt);
```

(where `ctxt` is some `Context`)

If you are using OkHttp3, create your basic `OkHttpClient.Builder`,
then call:

```java
OkHttp3Integrator.applyTo(tmb, okb);
```

(where `tmb` is the `TrustManagerBuilder` from before, and `okb`
is your `OkHttpClient.Builder`)

At this point, you can create your `OkHttpClient` from the `Builder`
and start using it.

If you are using `HttpURLConnection`, you can call `applyTo()` on
the `TrustManagerBuilder` itself, passing in the `HttpURLConnection`.
Afterwards, you can start using the `HttpURLConnection` to make your
HTTP request.

In either case, on Android 7.0+ devices, `withManifestConfig()` will
*not* use the backport. Instead, the platform-native implementation
of the network security configuration subsystem will be used. On
Android 4.2-6.0 devices, the backport will be used.

## Basic Limitations

If you use `HttpURLConnection`, you cannot use `<domain-config>`
elements in the network security configuration XML. Similarly,
you cannot use `cleartextTrafficPermitted` with `HttpURLConnection`.
If you have them in the XML, they will be ignored.

OkHttp3 should support the full range of network security configuration
XML features.

## Advanced Usage

If you want to employ certificate memorization or otherwise
do more sophisticated things with the network security
configuration backport and/or `TrustManagerBuilder`, there is a
[separate page of documentation](https://github.com/commonsguy/cwac-netsecurity/blob/master/docs/ADVANCED_USAGE.markdown)
for that.

## Notes for Upgraders

If you are upgrading to v0.3.0 or higher from v0.2.1 or older, and you
are using `<certificates src="user" />`, note that this is no longer supported
(see above).

## Compiling from Source and Running the Test Suites

The instrumentation tests in `androidTest/` are divided into two
sub-packages: `pub` and `priv`.

The `pub` tests hit publicly-available Web servers (mostly those
hosted by CommonsWare). As such, you should be able to run those
tests without issue.

The `priv` tests need additional configuration on your part. That
configuration is designed to be held in a `gradle.properties`
file that you need to add to your root directory of your copy
of the project code. Specifically, three values should reside there:

- `TEST_PRIVATE_HTTP_URL`: a URL to some Web server that you control
- `TEST_PRIVATE_HTTPS_URL`: a URL to some Web server that you control, where the communications are secured via SSL using a self-signed certificate
- `TEST_PRIVATE_HTTP_REDIR_URL`: a URL to some Web server that you control that, when requested, issues a server-side redirect to an SSL-secured page (such as the one from `TEST_PRIVATE_HTTPS_URL`)

The first two URLs should each return:

```json
{"Hello": "world"}
```

You will need to define those values in your `gradle.properties` file
even if you are just planning on modifying the code, as otherwise
the `build.gradle` files for the library modules will fail, as they expect
those values.

In addition, if you wish to run the `priv` tests, you will need to
replace the `androidTest/res/raw/selfsigned.crt` file in each library
module with the CRT file that matches your self-signed certificate that
`TEST_PRIVATE_HTTPS_URL` uses.

Note that right now the tests require Android 8.1 or *older*; some tests will
not work on Android 9.0 and higher.

## Dependencies

`netsecurity` has a `provided` dependency on OkHttp3. Version 0.5.0
of this library uses OkHttp version **3.9.1**. `netsecurity` presently is
not compatible with newer versions of OkHttp.

Otherwise, there are no external dependencies.

## Version

The current version is **0.5.0**.

## License

All of the code in this repository is licensed under the
Apache Software License 2.0. Look to the headers of the Java source
files to determine the actual copyright holder, as it is a mix of
the Android Open Source Project and CommonsWare, LLC.

## Questions

If you have questions regarding the use of this code, please post a question
on [Stack Overflow](http://stackoverflow.com/questions/ask) tagged with
`commonsware-cwac` and `android` after [searching to see if there already is an answer](https://stackoverflow.com/search?q=[commonsware-cwac]+camera). Be sure to indicate
what CWAC module you are having issues with, and be sure to include source code 
and stack traces if you are encountering crashes.

If you have encountered what is clearly a bug, or if you have a feature request,
please read [the contribution guidelines](.github/CONTRIBUTING.md), then
post an [issue](https://github.com/commonsguy/cwac-netsecurity/issues).
**Be certain to include complete steps for reproducing the issue.**
If you believe that the issue you have found represents a security bug,
please follow the instructions in
[the contribution guidelines](https://github.com/commonsguy/cwac-netsecurity/blob/master/.github/CONTRIBUTING.md#contributing-security-bug-reports).

You are also welcome to join
[the CommonsWare Community](https://community.commonsware.com/)
and post questions
and ideas to [the CWAC category](https://community.commonsware.com/c/cwac).

Do not ask for help via social media.

## AOSP Version Tracking and Release Notes

|Library Version|AOSP Code Base                                                                                          |Release Notes|
|:-------------:|:------------------------------------------------------------------------------------------------------:|-------------|
|v0.5.0         |Android 9.0 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|updated to OkHttp 3.9.1 and newer build instructions, [added methods to `CompositeTrustManager`](https://github.com/commonsguy/cwac-netsecurity/issues/18) |
|v0.4.5         |Android 8.0 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|updated to OkHttp 3.9.0 and newer Android Plugin for Gradle, Gradle|
|v0.4.4         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|updated to OkHttp 3.8.1 and fixed testing bug|
|v0.4.3         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|updated to OkHttp 3.8.0 and new test SSL certificate|
|v0.4.2         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|added single-item-chain filtering for memorization|
|v0.4.1         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|switched to OkHttp 3.6.0, add domain filtering for memorization|
|v0.4.0         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|added certificate memorization and NetCipher integration options|
|v0.3.1         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|bug fix per [issue #7](https://github.com/commonsguy/cwac-netsecurity/issues/7)|
|v0.3.0         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|`user` validation per [issue #5](https://github.com/commonsguy/cwac-netsecurity/issues/5)|
|v0.2.1         |Android 7.1 source code from the SDK, plus [the `android-7.1.0_r7` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.1.0_r7)|bug fix per [issue #3](https://github.com/commonsguy/cwac-netsecurity/issues/3)|
|v0.2.0         |Android 7.0 source code from the SDK, plus [the `android-7.0.0_r1` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.0.0_r1)|`HttpURLConnection` no longer requires `setHost()` call|
|v0.1.0         |Android 7.0 source code from the SDK, plus [the `android-7.0.0_r1` tagged edition of `conscrypt`](https://android.googlesource.com/platform/external/conscrypt/+/android-7.0.0_r1)|update for new version of Android|
|v0.0.1         |[`android-n-preview-4`](https://android.googlesource.com/platform/frameworks/base/+/android-n-preview-4)|initial release|
