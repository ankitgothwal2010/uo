# AnkitG Family Tools

**Developed by:** Ankit Gothwal  
**Copyright:** Copyright ©️ 2026 Ankit Gothwal. All rights reserved.  
**Package:** `com.ankit.familytools`  
**License:** MIT License (App code) • Permissive open-source dependencies (Apache 2.0)

An all-in-one, privacy-first, 100% offline native Android utility app designed for everyday family use. Family members can complete common PDF, photo, and video tasks in one single app without needing third-party utilities or website uploads.

---

## 🌟 Key Highlights

- **100% Offline & Private:** Documents, photos, signatures, and videos never leave the device. No servers, no tracking, and zero ads.
- **Family-Friendly UI:** Designed with large readable typography, accessible touch targets, and simple bilingual labels in English and Hindi (e.g. *Sign PDF / दस्तावेज़ हस्ताक्षर*, *Compress Video / वीडियो कंप्रेस करें*).
- **Interactive PDF Signing (Top Priority):** Render actual PDF pages, draw signatures with finger gestures, drag/resize/rotate signature overlays, sign multiple pages, add date/name stamps, and visibly embed onto the PDF at exact coordinates.
- **Hardware-Accelerated Video Compression:** Built on Android Media3 Transformer with presets (*Small File*, *Balanced*, *High Quality*), real progress tracking, cancellation support, and output validation.
- **Cloud APK Builds via GitHub Actions:** Fully independent of Android Studio on local PCs—builds, tests, and delivers installable APKs directly through GitHub Actions.
- **Optimized for Budget Phones:** Tailored to run smoothly on devices with 6 GB RAM or less (such as the Infinix Hot 10) using streaming 64 KB buffers, thumbnail caching, and background coroutines.

---

## 📱 Feature Matrix

| Category | Tool | Status | Description & Capabilities |
| :--- | :--- | :---: | :--- |
| **PDF** | **Sign PDF** | ✅ Implemented | Visual page previews, finger-drawing signature canvas, saved signature bank, draggable & resizable overlay, multi-page signing, date/name stamps, coordinate mapping, and PDFBox embedding. |
| **PDF** | **Merge PDF** | ✅ Implemented | Combine multiple PDF files with visual drag-and-drop reordering. |
| **PDF** | **Split PDF** | ✅ Implemented | Split PDF by page ranges into separate files. |
| **PDF** | **Scan & OCR** | ✅ Implemented | On-device text recognition with Google ML Kit. Extract text from photos and export to PDF. |
| **PDF** | **Compress PDF** | ✅ Implemented | Optimize stream objects and compress embedded images. |
| **PDF** | **Images to PDF** | ✅ Implemented | Convert multiple gallery photos into a paginated A4 PDF. |
| **PDF** | **Text to PDF** | ✅ Implemented | Convert typed notes or `.txt` content into formatted PDF. |
| **PDF** | **Extract Text** | ✅ Implemented | Extract selectable text from documents via `PDFTextStripper`. |
| **PDF** | **Page Numbers** | ✅ Implemented | Add formatted page numbers (*Page X of Y*) at bottom center. |
| **PDF** | **Watermark** | ✅ Implemented | Semi-transparent diagonal watermark across document pages. |
| **PDF** | **Password Lock / Unlock** | ✅ Implemented | 128-bit AES encryption protection and password removal for known passwords. |
| **PDF** | **Reorder & Rotate** | ✅ Implemented | Visual page reordering, 90°/180° rotation, and page deletion. |
| **Image** | **Crop Image** | ✅ Implemented | Preset aspect ratios (1:1 Square, 4:3, 16:9) and freeform cropping. |
| **Image** | **Rotate & Flip** | ✅ Implemented | 90° clockwise/counter-clockwise rotation and horizontal/vertical flip. |
| **Image** | **Resize Image** | ✅ Implemented | Scale by percentage (25%, 50%, 75%) or custom dimensions. |
| **Image** | **Compress Image** | ✅ Implemented | Quality slider (1-100%), live size estimation, and format conversion. |
| **Image** | **Adjust Colors** | ✅ Implemented | Adjust brightness, contrast, and saturation via `ColorMatrix`. |
| **Image** | **Remove EXIF Metadata** | ✅ Implemented | Strip GPS coordinates, camera model, and personal tags for safe sharing. |
| **Image** | **Sign on Photo** | ✅ Implemented | Stamp saved family signatures directly onto photos and forms. |
| **Image** | **Photo Collage** | ✅ Implemented | Combine 2 to 6 photos into clean grid collages with custom borders. |
| **Video** | **Compress Video** | ✅ Implemented | Media3 Transformer transcoding with *Small*, *Balanced*, *High Quality* presets, live progress, and % space saved badge. |
| **Video** | **Trim Video** | ✅ Implemented | Fast start/end timestamp clipping via Media3 clipping configurations. |
| **Video** | **Extract Audio** | ✅ Implemented | Export soundtrack as standalone M4A / AAC audio file. |
| **Video** | **Mute Video** | ✅ Implemented | Strip audio track while preserving video stream quality. |
| **Video** | **Extract Frame** | ✅ Implemented | Save high-resolution still image from any video timestamp. |
| **General** | **Processing History** | ✅ Implemented | Persistent log of output files with quick Open, Share, and Delete options. |
| **General** | **Favorites & Search** | ✅ Implemented | Pin favorite tools and instantly search tools in English or Hindi. |
| **General** | **Cache Management** | ✅ Implemented | Check temporary file storage and clean old cache with one tap. |

---

## 🏗️ Architecture & Technology Stack

The app adheres to modern native Android Clean Architecture:

- **Language:** 100% Kotlin
- **UI Toolkit:** Jetpack Compose with Material 3 design system
- **State Management:** Reactive Kotlin Coroutines and StateFlow
- **PDF Engine:** Apache PDFBox-Android (`com.tom-roush:pdfbox-android:2.0.27.0`)
- **Video Engine:** AndroidX Media3 Transformer (`androidx.media3:media3-transformer:1.2.1`)
- **OCR Engine:** Google ML Kit Text Recognition (`com.google.android.gms:play-services-mlkit-text-recognition:19.0.0`)
- **Image Pipeline:** Android SDK `Bitmap`, `Canvas`, `ColorMatrix`, and `ExifInterface`
- **Storage:** Android Storage Access Framework (SAF) and secure `FileProvider`

---

## ⚡ Low-End Device Optimizations (e.g. Infinix Hot 10 6GB)

1. **Streaming Stream Buffers:** All file copying operations stream data in fixed 64 KB buffers, preventing large files from blowing up Java heap space.
2. **Native Thumbnail Generation:** Page previews use Android's native `PdfRenderer` bounded to a maximum 1200 px dimension with `Bitmap.Config.ARGB_8888`.
3. **Decoupled Bitmaps:** Bitmaps are actively recycled (`bitmap.recycle()`) after image processing operations.
4. **Hardware-Accelerated Codecs:** Media3 automatically prioritizes device hardware video encoders (`MediaCodec`) with safe fallback to software encoding.
5. **Auto-Purge Cache:** Cache cleaner purges temporary processing artifacts older than 24 hours to keep internal flash memory free.

---

## ☁️ Cloud APK Builds with GitHub Actions (No Android Studio Needed!)

You do not need Android Studio installed on your low-end Windows 10 PC. You can build and download installable APKs directly through GitHub Actions:

### Step 1: Create a GitHub Repository
1. Log into your GitHub account.
2. Create a new repository named `ankitg-family-tools` (set it to Public or Private).
3. Push this project to GitHub:
   ```bash
   git init
   git add .
   git commit -m "Initial commit of AnkitG Family Tools"
   git branch -M main
   git remote add origin https://github.com/<your-username>/ankitg-family-tools.git
   git push -u origin main
   ```

### Step 2: Cloud Build Automatically Runs
1. Open your repository on GitHub and click on the **Actions** tab.
2. You will see the **Build AnkitG Family Tools APK** workflow running.
3. The workflow automatically:
   - Sets up Temurin JDK 17
   - Configures Gradle 8.4
   - Executes all automated unit tests (`./gradlew testDebugUnitTest`)
   - Builds the debug APK (`./gradlew assembleDebug`)
   - Packages and uploads `ankitg-family-tools-debug-apk` as a downloadable artifact.

### Step 3: Download the APK
1. Once the workflow completes (green checkmark), click on the workflow run.
2. Scroll down to the **Artifacts** section at the bottom of the page.
3. Click on **ankitg-family-tools-debug-apk** to download the ZIP file containing `app-debug.apk`.

---

## 📲 How to Install on Your Android Phone (Sideloading)

1. Unzip the downloaded file on your computer or phone to get `app-debug.apk`.
2. Transfer `app-debug.apk` to your phone via USB cable, WhatsApp, or Google Drive.
3. Tap on `app-debug.apk` in your phone's File Manager.
4. If prompted with *"For your security, your phone is not allowed to install unknown apps from this source"*:
   - Tap **Settings**.
   - Enable **Allow from this source**.
   - Tap **Back** and tap **Install**.
5. Open **AnkitG Family Tools** and start using!

---

## 🧪 Automated Testing

Automated unit tests cover coordinate transformations, video estimation, compression ratios, and tool integrity:

```bash
# Run tests locally (or verified automatically in GitHub Actions CI):
./gradlew testDebugUnitTest
```

### Verified Test Suites:
- `PdfCoordinateTest`: Verifies 0°, 90°, 180°, and 270° screen-to-PDF coordinate mappings, bottom-left origin inversions, and out-of-bounds coordinate clamping.
- `ImageProcessingTest`: Tests aspect ratio crop calculations and compression estimation bounds.
- `VideoAndFileProcessingTest`: Tests duration string formatting, bitrate estimation algorithms, and percentage saved calculations.
- `ToolCatalogTest`: Validates that all tools possess unique IDs, valid bilingual titles, non-null vector icons, and proper navigation routes.

---

## 📄 Legal & Copyright

**AnkitG Family Tools**  
Developed by **Ankit Gothwal**  
Copyright ©️ 2026 Ankit Gothwal. All rights reserved.

Licensed under the [MIT License](LICENSE). Third-party dependencies are licensed under their respective permissive open-source licenses (primarily Apache License 2.0). See [NOTICE.md](NOTICE.md) for detailed acknowledgements.
