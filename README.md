# ⚡ SHRED (v2.0)
> **High-Performance Android Media Curator with GTA-Style Gesture Controls, Volumetric 3D Card Flips, Staged Vaults, and Industrial Aesthetics.**

```
   _____ _    _ _____  ______ _____  
  / ____| |  | |  __ \|  ____|  __ \ 
 | (___ | |__| | |__) | |__  | |  | |
  \___ \|  __  |  _  /|  __| | |  | |
 ____) | |  | | | \ \| |____| |__| |
|_____/|_|  |_|_|  \_\______|_____/ 
```

**SHRED** is an opinionated, high-velocity native Android media cleaner built with Kotlin and Jetpack Compose for Android 14+ (API 34+). Designed to replace clunky gallery managers, SHRED lets you blitz through thousands of photos, videos, and audio tracks with ruthless efficiency using directional gesture locks, tactile velocity fling triggers, staged deletion buffers, and industrial-grade typography.

---

## ⚡ What Makes SHRED Different?

Most gallery cleaners either delete files instantly (leading to panic) or trap you in slow multi-step confirmation dialogues. SHRED combines fluid card physics with staged auto-purge vaults, GTA-inspired knockout action overlays, and true 3D spatial card rotations.

---

## 🎮 Gestures & Interaction Physics

```
                    ▲ [UP] FAVORITE
                    │ (Golden Overlay)
                    │
   [LEFT] WASTED   ◀┼▶   [RIGHT] SAVED
 (Crimson Overlay)  │   (Neon Lime Overlay)
                    │
                    ▼ [DOWN] METADATA FLIP
                    (Electric Cyan Overlay)
```

- **Directional Axis Locking**: Intelligent 24dp center tolerance zone. Once you commit to horizontal or vertical movement, the orthogonal axis is hard-locked to prevent accidental diagonal mis-swipes.
- **Flick & Velocity Commitment**: Fast flicks ($> 950\text{dp/s}$) trigger instant commits without needing a full screen drag. A strict displacement buffer ($\ge 45\text{dp}$) guarantees quick taps or jittery micro-drags never trigger an accidental deletion.
- **GTA-Inspired Knockout Overlays**:
  - 🛑 **WASTED** (Crimson `#FF0033`): Staged to the Trash Vault.
  - 🟢 **SAVED** (Neon Lime `#00E676`): Kept in your library and marked as reviewed.
  - ⭐ **FAVORITE** (Gold `#FFD700`): Pinned to the permanent Favorites Vault.
  - 🔍 **METADATA** (Electric Cyan `#00E5FF`): Smoothly flips to the file telemetry view.
- **3D Volumetric Card Thickness**: During vertical flips, a dynamic 3D rim slab extrudes along the flip axis. The card never collapses into an invisible 1-pixel flatline.
- **Double-Tap Quick Inspect**: Double-tap any photo card to instantly zoom to $2.6\times$ with smooth boundary-constrained 1-finger / 2-finger panning. Double-tap again to reset cleanly back to stack mode.
- **Aspect Ratio Parity**: Background deck layers share strict `ContentScale.Fit` letterboxing with the top card, eliminating preview zoom-jumps when discarding media.

---

## 🛡️ Vault Architecture & Staged Auto-Purge

Never accidentally obliterate memories again:

- 🗑️ **Staging Trash Vault (`/trash`)**: Media swiped left is moved to a private staging vault with a customizable grace period (1 hour default, configurable in Settings). Items can be reviewed, looped, or restored before the auto-purge executes.
- 📁 **Kept Vault (`/kept`)**: Dedicated review deck for all preserved files.
- ⭐ **Favorites Vault (`/favorites`)**: Fast-access collection for starred media.
- 🤏 **Pinch-to-Zoom Responsive Grids**: Fluid pinch gestures inside vaults allow you to dynamically scale between single-card stacks and multi-column grid overviews.
- 🧠 **Persistent Folder Memory**: Folder and category queues remember your exact browsing index and resume where you left off.
- ⚡ **Skeleton Shimmer Engine**: Asynchronous media load states with shimmer skeleton placeholders to eliminate layout shifts and stutter.

---

## 🎨 Design Systems & Themes

SHRED supports three switchable design languages:

1. **Brutalism**: High-contrast monochrome palettes, bold monospace typography, sharp 3dp solid borders, and zero rounded fluff.
2. **Minimalism**: Subtle hairline borders, generous negative space, refined typography, and neutral pastel undertones.
3. **Glassmorphism**: Translucent frosted glass layers, multi-stop acrylic blurs, and vibrant ambient rim glows.

---

## 🗄️ Media Engine & Scanning Coverage

- **Dual-Engine Deduplicating Scanner**: Combines `ContentResolver MediaStore` indices with deep recursive filesystem crawling across root storage (`DCIM`, `Pictures`, `Movies`, `Music`, `Downloads`, `Documents`, `WhatsApp`, `Telegram`, and hidden directories).
- **Supported Media Formats**:
  - 🖼️ **Photos**: JPG, PNG, GIF, BMP, WEBP, HEIC, HEIF, SVG, TIFF, DNG, RAW, CR2, NEF, ARW
  - 🎥 **Videos**: MP4, MKV, AVI, MOV, WMV, FLV, WEBM, 3GP, M4V, MPEG, MPG, TS
  - 🎧 **Audio**: MP3, AAC, FLAC, WAV, OGG, M4A, WMA, OPUS, AMR, AIFF (features a live reactive frequency waveform visualizer)

---

## 🛠️ Architecture & Tech Stack

```
com.shred.app
 ├── data
 │    ├── db            # Room Database (MediaFileDao, Converters, AppDatabase)
 │    ├── model         # MediaFile, AppSettings, Enums, Filters
 │    └── repository    # MediaRepository, SettingsRepository
 ├── scanner            # Dual-engine recursive crawler & MediaStore synchronizer
 ├── ui
 │    ├── components    # VaultMediaCard, SkeletonLoading, WaveformVisualizer
 │    ├── navigation    # Jetpack Compose Navigation & Deep Link Routes
 │    ├── screens       # Swipe, Kept, Trash, Favorites, Folders, Settings
 │    └── theme         # Brutalist, Minimalist, Glassmorphic Theme Engine
 └── worker             # WorkManager staged auto-purge background services
```

- **Framework**: Jetpack Compose with Material 3 & Compose Canvas
- **Language**: Kotlin 2.1.10
- **Database**: Room 2.6.1 (SQLite with WAL mode)
- **Concurrency**: Coroutines & Reactive StateFlow
- **Image Pipeline**: Coil 2.7.0 (Frame decoder, SVG, GIF, Bitmap pooling)
- **Audio/Video Playback**: AndroidX Media3 / ExoPlayer 1.5.1
- **Background Tasks**: AndroidX WorkManager 2.10.0

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- Android SDK Platform 34+ (Android 14+)
- JDK 17+ or JDK 21

### Build & Install via CLI
```bash
# Clone the repository
git clone https://github.com/aaroninplayz/shred.git
cd shred

# Compile debug APK
./gradlew assembleDebug

# Install directly to attached test device
./gradlew installDebug
```

### Required Device Permissions
On first launch, grant:
- **All Files Access** (`MANAGE_EXTERNAL_STORAGE`) — Required to stage and manage storage files directly.
- **Media Permissions** (`READ_MEDIA_IMAGES`, `READ_MEDIA_VIDEO`, `READ_MEDIA_AUDIO`).

---

## 📄 License
Personal Use Application. Built by [Aaron](https://github.com/aaroninplayz).
