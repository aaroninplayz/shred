# 📋 SHRED Release Changelog

All notable changes to the **SHRED** project are documented in this file.

---

## [v1.5.0] - Hot Air Balloon Buoyancy, Night-and-Day Themes & MediaStore Scanner

### 🎈 Hot Air Balloon Playground & Physics
- Replaced category blocks with vector-rendered **Hot Air Balloons** matching reference silhouette (rounded dome top, 3D stripe gores, straight diagonal side tapered neck, and clean carriage basket).
- Implemented **Average-Size Buoyancy Physics** ($\text{Average Size} = \text{Total Size} / \text{Count}$) driving vertical float velocity and altitude.
- Integrated 2D rigid-body Separating Axis Theorem (SAT) collision detection, wall reflections, pointer flick impulses, and drag physics.
- Formatted carriage basket text strictly to `[Category]` and `$count FILES • $sizeFormatted`.

### 🎨 Design System & Night-and-Day Themes
- Rebuilt UI theme engine with 3 distinct styles:
  - 🗿 **Brutalism**: High-contrast monochrome layout with solid 3dp borders, hard shadows, and high-energy accent buttons.
  - 🧘 **Minimalism**: Clean ultra-light aesthetic with subtle hairline borders, soft neutral cards, and generous white space.
  - 🪞 **Glassmorphism**: Translucent frosted glass visuals, gradient glow effects, and multi-layered acrylic depth.

### 🗄️ MediaScanner & Media Store Fallback
- Added `scanMediaStore()` ContentResolver indexing fallback across `Images`, `Video`, and `Audio` query URIs.
- Configured multi-root directory seeding (`DCIM`, `Pictures`, `Movies`, `Music`, `Downloads`, `Documents`, `WhatsApp`, `Telegram`) to guarantee instant file discovery on startup.

### 🎧 Audio Visualizer & Dark Zoom Backdrop
- Replaced play/pause buttons with real-time reactive frequency audio visualizer bars, tap-to-toggle playback, and an interactive bottom progress scrubber line.
- Redesigned photo/video zoom mode to use a full black backdrop hiding white borders and framing controls.

---

## [v1.0.0] - Production Release

### 🚀 Highlights
- Production-ready native Android release for Android 14+ (API 34+).
- Tinder-style swipe gestures with arc rotation physics and color-coded feedback overlays.
- Deep recursive filesystem crawler discovering all media formats, hidden dot-folders, and messenger app silos.
- 3D card flip revealing all 7 metadata properties.
- Interactive brutalist shredder home screen with floating evasion physics.
- Cinematic startup loading sequence with typewriter text animation.
- Exhaustive system settings console (all 12 controls).
- Procedural audio synthesizer (shredder crunch, keep click, favorite chime).
- Multi-tier haptic vibration engine.
- Background trash auto-purge service via WorkManager.

---

## [v0.6.0] - Polish & Test Hardening

### Added
- Proguard optimization rules for release builds in `app/proguard-rules.pro`.
- Hardened corrupted video/audio metadata exception handling in `MediaScanner`.
- Waveform sampler unit tests for fallback generation.
- Folder group aggregation unit tests.

---

## [v0.5.0] - System Settings & Customization

### Added
- Interactive Excluded Folders Manager (add / remove path exclusions).
- Minimum File Size threshold filter (0 MB to 200 MB).
- Scan Mode selector (Every Launch, Incremental, Manual Only).
- Theme engine with 3 flavors: Monochrome Brutalism, Neon Brutalism, Neo-Brutalism.
- Progress bar styles: Minimal Counter, Storage Freed, Ratio Bar, Numbers Only, Hidden.
- Customizable startup loading phrases pool.

---

## [v0.4.0] - Animated Shredder Machine & Cinematic Loading

### Added
- Mechanical shredder machine canvas with animated cutting blades.
- Floating category blocks (Photos, Videos, Audio) with scared/flee repulsion physics.
- Drag & flick interaction to feed media blocks into the shredder.
- Cinematic numberless scan progress bar with customizable typewriter phrase display.

---

## [v0.3.0] - Tinder-Style Swipe Deck & Media Players

### Added
- Card stack deck with progressive tilt and blur.
- Gesture physics with bottom-center arc rotation.
- Color tint overlays (Red for Delete, Green for Keep, Gold for Favorite).
- 3D vertical card flip exposing 7 file metadata attributes.
- Image zoom mode with pan and pinch zoom.
- ExoPlayer video playback with fullscreen timeline scrubber.
- Waveform visualization with interactive play/pause for audio tracks.
- Folder Header Cards for dense directories.
- Instant single undo corner action.

---

## [v0.2.0] - Recursive Filesystem Scanner

### Added
- Recursive filesystem crawler covering all media formats (Photos, Videos, Audio).
- EXIF metadata extraction (`ExifInterface`) for photo dimensions and timestamps.
- `MediaMetadataRetriever` extraction for video/audio dimensions and durations.
- Support for dot-prefixed hidden directories and app-data folders.

---

## [v0.1.0] - Initial Scaffold & Architecture

### Added
- Kotlin + Jetpack Compose project structure targeting API 34+.
- Room database with `MediaFileDao` and `AppSettingsDao`.
- Repository pattern and StateFlow state management.
- Base Monochrome Brutalist design system with custom typography and hard borders.
- Soft delete trash management in `.PhoneCleaner/trash/`.
