# QR Studio — Android QR Code Generator

A single-screen Android app built in **Kotlin + Jetpack Compose**. Enter arbitrary text or a URL, generate a QR code on-device, save it as a PNG in the gallery, or share the PNG through the Android Sharesheet.

## Deliverables

- **Input**: text and URL support, blank-input validation, editing clears stale results.
- **Generate**: ZXing QR encoder, UTF-8, error correction level M, quiet zone, 1024×1024 PNG bitmap.
- **Preview**: instant QR display in the app.
- **Save PNG**: Android MediaStore, `Pictures/QR Studio`, automatic gallery visibility.
- **Share PNG**: temporary cache image, FileProvider content URI, `ACTION_SEND`, read permission grant.
- **Automated test**: an Android instrumentation test decodes generated images to verify round-trip contents.

## Requirements

- Android Studio supporting Android Gradle Plugin **9.2.1**
- JDK **17**
- Android SDK **37** installed
- Android device or emulator **API 29+** (Android 10+)
- Internet connection **only for first-time dependency sync**; QR generation itself works offline.

## Open in Android Studio

1. Extract this ZIP into a folder.
2. Open Android Studio → **Open** → select the extracted `QRStudioAndroid` directory.
3. Install requested SDK packages, sync Gradle dependencies, then run `app` on an Android 10+ device or emulator.
4. Type `https://example.com` or any text → **Generate QR Code** → **Save PNG** or **Share**.

### Gradle Wrapper note

The project contains `gradle/wrapper/gradle-wrapper.properties` and build files. Because the wrapper launcher JAR and scripts were not available in the authoring environment, they are **not bundled**. If Android Studio cannot sync without them, on a machine with Gradle already installed, run:

```sh
gradle wrapper --gradle-version 9.4.1
```

This generates `gradlew`, `gradlew.bat`, and `gradle/wrapper/gradle-wrapper.jar`; reopen the project and sync. Alternatively, create an **Empty Activity (Compose)** project in your Android Studio and copy the supplied `app/src/main` sources and the Gradle dependencies into it.

## Architecture and files

```
app/src/main/java/com/example/qrstudio/
  MainActivity.kt       Compose UI and async actions
  QrCodeGenerator.kt    ZXing Bitmap encoder
  QrImageStorage.kt     MediaStore and FileProvider image operations
app/src/main/res/xml/file_paths.xml
app/src/main/AndroidManifest.xml
app/src/androidTest/.../QrCodeGeneratorTest.kt
```

## Technical notes

- Targeted minimum API 29 avoids legacy external storage permission handling. No storage or network permissions are required.
- Gallery save uses `IS_PENDING` so other apps do not see partially written PNG files.
- Shares use `content://` URIs; no insecure `file://` URI is exposed.
- Background dispatchers prevent QR generation and image I/O from blocking the UI.
- Text is encoded locally; URLs are **not uploaded or visited**.
- The max QR capacity is determined by ZXing / the QR standard. Long text may fail gracefully with a snackbar.

## Run test

Run the instrumentation test `QrCodeGeneratorTest` on a connected emulator/device with Android Studio, or execute `./gradlew connectedAndroidTest` after generating the wrapper.

## Core dependencies

- AGP 9.2.1 + Gradle 9.4.1
- Kotlin Compose compiler plugin 2.3.10
- Compose BOM 2026.09.00
- Activity Compose 1.13.0
- ZXing Core 3.5.4

## Scope

This submission implements the four requested features. It does not include scanning QR codes or a QR history/database because they are outside Task 1.
