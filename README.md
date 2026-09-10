# ⚡ SHRED (v0.1.0)
> **Brutalist Native Android Media Cleaner with Tinder-Style Swipe Gestures**

```
   _____ _    _ _____  ______ _____  
  / ____| |  | |  __ \|  ____|  __ \ 
 | (___ | |__| | |__) | |__  | |  | |
  \___ \|  __  |  _  /|  __| | |  | |
  ____) | |  | | | \ \| |____| |__| |
 |_____/|_|  |_|_|  \_\______|_____/ 
```

**SHRED** is a native Android application built in Kotlin and Jetpack Compose for Android 14+ (API 34+). It turns the tedious chore of purging unwanted photos, videos, and audio files across your entire device into a tactile, Tinder-style swiping experience wrapped in a monochrome brutalist aesthetic.

---

## 🎯 Key Features

### 🗄️ Deep Filesystem Discovery
- **All-Directory Scanner**: Recursively crawls external storage and app data directories (`/sdcard`, WhatsApp `.Shared` / `.Statuses`, Telegram media, downloads, hidden dot-folders).
- **Format Coverage**:
  - 🖼️ **Photos**: JPG, PNG, GIF, BMP, WEBP, HEIC, HEIF, SVG, TIFF, RAW, CR2, NEF, ARW, DNG
  - 🎥 **Videos**: MP4, MKV, AVI, MOV, WMV, FLV, WEBM, 3GP, M4V, MPEG, MPG, TS
  - 🎧 **Audio**: MP3, AAC, FLAC, WAV, OGG, M4A, WMA, OPUS, AMR, AIFF

### 🃏 Tinder-Style Gesture Deck
- **Swipe LEFT** ➔ **DELETE** (Soft delete moved to hidden `.PhoneCleaner/trash/` directory)
- **Swipe RIGHT** ➔ **KEEP** (Marked as reviewed, excluded until next reset cycle)
- **Swipe UP** ➔ **FAVORITE** (Saved to the permanent Favorites Vault)
- **Swipe DOWN** ➔ **FLIP CARD** (3D vertical flip revealing all 7 file metadata attributes)
- **Tap** ➔ Play / Pause (video & audio)
- **Double-Tap** ➔ Fullscreen Scrubber (video)
- **Long-Press Toggle** ➔ Zoom Mode (pan & pinch zoom for photos and video frames)
- **Single Undo** ➔ Instant corner revert for accidental swipes

### 📂 Smart Folder Grouping
- Recognizes dense media folders (e.g. Call Recordings, WhatsApp subfolders)
- Displays a distinct **Folder Header Card** allowing batch delete, batch keep, or tapping to expand individual files.

### 🏭 Animated Shredder Machine Home Screen
- Interactive bottom shredder with mechanical cutting blade animations
- **Floating Category Blocks** (Photos, Videos, Audio) drifting with physics
- Category blocks exhibit **flee / scared evasion** when near the shredder
- Drag and flick any block into the shredder to launch cleaning

### 🎬 Cinematic Loading Sequence
- Synchronized filesystem scan line
- Blank screen transition
- User-configurable customizable text pool with typewriter / fade animations
- Fade-to-black transition into the workspace

### ⚙️ Exhaustive System Customization
1. **Trash Retention**: 1 hour to 60 days auto-purge
2. **Sort Order**: Oldest first, Newest first, Largest first, Smallest first, Random
3. **Theme Switcher**: Monochrome Brutalism, Neon Brutalism, Neo-Brutalism
4. **Progress Bar**: Minimal Counter, Storage Freed, Ratio Bar, Numbers Only, Hidden
5. **Loading Phrases**: Add / Remove custom phrase strings
6. **Animation Styles**: Typewriter, Fade-in, Slide-in
7. **Transition Styles**: Fade-to-black, Direct Cut, Cross-Dissolve
8. **Haptic Engine**: Tactile vibrations with configurable intensity
9. **Sound FX**: Procedural audio synthesizer (shredder crunch, click, chime)
10. **Auto-Reset**: 7 to 30 days timer for kept files
11. **Favorites Vault**: Dedicated swipe reviewer

---

## 🛠️ Tech Stack & Architecture

- **Language**: Kotlin 2.1.10
- **UI Framework**: Jetpack Compose with Material 3
- **Local Database**: Room 2.6.1 (SQLite)
- **Async & Reactive**: Kotlin Coroutines & StateFlow
- **Image Pipeline**: Coil 2.7.0 (Image, Video frame, SVG, GIF support)
- **Media Engine**: AndroidX Media3 / ExoPlayer 1.5.1
- **Background Tasks**: WorkManager 2.10.0
- **Audio Synthesis**: Android AudioTrack PCM generation
- **Haptics**: Android VibrationEffect API

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- Android SDK Platform 34+
- Java JDK 17+

### Sideload via Android Studio
1. Open the project in Android Studio.
2. Ensure your Android device (Android 14+) has **Developer Options** and **USB Debugging** enabled.
3. Click **Run 'app'** or run:
```bash
./gradlew installDebug
```
4. On first launch, tap **[ GRANT ALL FILES ACCESS ]** to enable recursive storage scanning.

---

## 📜 License
Personal Use Application. Created by Aaron.
