# Task 1 — QR Code Generator Android App

**Technology:** Kotlin, Jetpack Compose, Material 3, ZXing, MediaStore, FileProvider

| Assignment step | Implementation |
|---|---|
| 1. Text/URL input UI | `OutlinedTextField` in Compose (`MainActivity.kt`) |
| 2. Integrate QR library | `com.google.zxing:core:3.5.4` in Gradle; `QrCodeGenerator.kt` |
| 3. Show generated code | Compose `Image` bitmap preview in `MainActivity.kt` |
| 4. Save image | `QrImageStorage.saveToGallery()`, PNG in `Pictures/QR Studio` |
| 5. Share QR | `QrImageStorage.createShareUri()` + `share()` with Android Sharesheet |

**Manual QA**

- [ ] Empty text cannot generate a code.
- [ ] Enter text → generate → scan with another phone; contents match.
- [ ] Enter HTTPS URL → generate → scan with another phone; URL matches.
- [ ] Save PNG → check gallery in `Pictures/QR Studio`.
- [ ] Share → select messaging/email application → PNG is attached and readable.
- [ ] Change input → old QR image disappears.
- [ ] Rotate device → input persists.

**Limitations:** Android 10+ due to permission-free gallery saving; no APK included because this environment has no installed Android SDK/Gradle build tools to compile it.
