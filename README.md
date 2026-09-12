# ⚡ SHRED (v1.5.0)
> **Native Android Media Cleaner with Hot Air Balloon Buoyancy Physics, Tinder-Style Gestures, and Multi-Theme Design System**

```
   _____ _    _ _____  ______ _____  
  / ____| |  | |  __ \|  ____|  __ \ 
 | (___ | |__| | |__) | |__  | |  | |
  \___ \|  __  |  _  /|  __| | |  | |
 ____) | |  | | | \ \| |____| |__| |
|_____/|_|  |_|_|  \_\______|_____/ 
```

**SHRED** is a native Android application built in Kotlin and Jetpack Compose for Android 14+ (API 34+). It turns the tedious chore of purging unwanted photos, videos, and audio files across your device into an engaging experience powered by 2D rigid-body collision physics, custom Hot Air Balloon category components, and high-performance media players.

---

## 🎯 Key Features

### 🎈 Hot Air Balloon Playground & Physics Engine
- **Accurate Reference Silhouette**: Custom vector canvas rendering hot air balloons with a rounded dome top tapering down through straight diagonal side gores to a narrow neck and carriage basket.
- **Dynamic Buoyancy Physics**: Altitude ($Y$-position) and vertical float velocity ($V_y$) are dynamically calculated based on category **Average File Size** relative to overall storage ($\text{Average Size} = \text{Total Size} / \text{File Count}$).
- **2D Rigid-Body Physics**: Custom engine supporting Separating Axis Theorem (SAT) collision detection, wall reflections, drag, angular momentum, and pointer impulse physics.
- **Clean Carriage Basket**: Payload text strictly formatted to display `[Category]` and `$count FILES • $sizeFormatted` without clutter.

### 🎨 3-Tier Design System & Night-and-Day Themes
- 🗿 **Brutalism**: High-contrast stark monochrome layout with thick 3dp solid borders, hard drop shadows, and high-energy accent colors.
- 🧘 **Minimalism**: Clean ultra-light aesthetic with subtle hairline borders, generous white space, soft neutral pastel cards, and refined typography.
- 🪞 **Glassmorphism**: Translucent frosted glass effect using multi-layered acrylic blurs, vibrant dark gradients, and glowing borders.

### 🗄️ Robust Dual-Engine Scanner
- **ContentResolver MediaStore Indexing**: Instant queries across `MediaStore.Images`, `MediaStore.Video`, and `MediaStore.Audio` for immediate file discovery.
- **Recursive Storage Crawler**: Scans root directories (`DCIM`, `Pictures`, `Movies`, `Music`, `Downloads`, `Documents`, `WhatsApp`, `Telegram`, and hidden storage locations).
- **Extensive Format Coverage**:
  - 🖼️ **Photos**: JPG, PNG, GIF, BMP, WEBP, HEIC, HEIF, SVG, TIFF, RAW, CR2, NEF, ARW, DNG
  - 🎥 **Videos**: MP4, MKV, AVI, MOV, WMV, FLV, WEBM, 3GP, M4V, MPEG, MPG, TS
  - 🎧 **Audio**: MP3, AAC, FLAC, WAV, OGG, M4A, WMA, OPUS, AMR, AIFF

### 🃏 Tinder-Style Gesture Deck & Media Viewers
- **Swipe LEFT** ➔ **DELETE** (Soft delete moved to hidden `.PhoneCleaner/trash/` directory)
- **Swipe RIGHT** ➔ **KEEP** (Marked as reviewed, excluded until next reset cycle)
- **Swipe UP** ➔ **FAVORITE** (Saved to permanent Favorites Vault)
- **Swipe DOWN** ➔ **FLIP CARD** (3D vertical flip revealing all 7 file metadata attributes)
- 🎧 **Live Audio Visualizer**: Real-time reactive frequency waveform visualizer with tap-to-toggle Play/Pause and a sleek bottom progress scrubbing bar.
- 🖼️ **Dark Backdrop Zoom Mode**: Panning and pinch-zoom in photos against a dark background, hiding white borders for an immersive view.
- **Single Revert**: Instant corner action to undo accidental swipes.

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.1.10
- **UI Framework**: Jetpack Compose with Material 3 & Custom Canvas Graphics
- **Local Database**: Room 2.6.1 (SQLite)
- **Async & Reactive**: Kotlin Coroutines & StateFlow
- **Image Pipeline**: Coil 2.7.0 (Image, Video frame, SVG, GIF support)
- **Media Engine**: AndroidX Media3 / ExoPlayer 1.5.1
- **Background Service**: WorkManager 2.10.0
- **Haptic Engine**: Android VibrationEffect API

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- Android SDK Platform 34+
- Java JDK 17+

### Sideload via Android Studio / CLI
1. Clone the repository and open in Android Studio.
2. Ensure your Android device (Android 14+) has **Developer Options** and **USB Debugging** enabled.
3. Build and install:
```bash
./gradlew installDebug
```
4. On first launch, grant **All Files Access** (`MANAGE_EXTERNAL_STORAGE`) and **Media Permissions** to launch recursive storage scanning.

---

## 📜 License
Personal Use Application. Created by Aaron.

