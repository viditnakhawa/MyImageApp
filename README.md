# 📸 SnapSuite: The Intelligent Screenshot Manager

**SnapSuite** is a modern, privacy-first Android app that redefines how screenshots are handled. It’s more than just a gallery—SnapSuite uses on-device AI to **understand**, **tag**, and **organize** your screenshots automatically, making them **instantly searchable**, contextual, and useful.

Built with Jetpack Compose and a scalable architecture, SnapSuite is the perfect showcase of clean Android development and on-device intelligence.

---

## ✨ Features at a Glance

| Feature | Description |
|--------|-------------|
| 📷 **Automatic Screenshot Detection** | Seamlessly detects new screenshots and adds them to your library—no manual steps. |
| 🧠 **On-Device AI Analysis** | Powered by **Google’s Gemma** models for secure, local processing. It extracts: <ul><li>📌 Titles</li><li>📝 Summaries</li><li>🏷️ Tags</li><li>🔤 Text (OCR)</li></ul> |
| 🔒 **100% Private** | All processing happens **on your device**—no screenshots ever leave your phone. |
| 📂 **Smart Collections** | Group screenshots into meaningful categories: travel, work, memes—you name it. |
| ✍️ **Notes & Context** | Add personal notes to remember why a screenshot was taken. |
| 🔍 **Powerful Search** | Search by content, tags, or your custom notes to find exactly what you need. |
| 🎨 **Dynamic Theming** | Fully supports **Material You** for a personalized experience (Android 12+). |
| 🚀 **Smooth Onboarding** | Friendly and informative welcome screens guide new users. |
| 🧩 **Model Management** | Download, update, or delete AI models right from the settings. |

---

## 🛠️ Tech Stack & Architecture

SnapSuite is built using the latest in Android development, following best practices for **scalability**, **testability**, and **maintainability**.

### 🧱 Core Architecture

- **UI Framework**: 100% [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Architecture Pattern**: MVVM + Repository (based on Google’s official [Guide to App Architecture](https://developer.android.com/topic/architecture))
- **Asynchronous Programming**: [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Dependency Injection**: [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Database**: [Room](https://developer.android.com/jetpack/androidx/releases/room) for local metadata and analysis storage
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/) (Compose-optimized image loader)
- **Navigation**: [Jetpack Compose Navigation](https://developer.android.com/jetpack/compose/navigation)

### 🧠 AI & Machine Learning

- **Gemma by Google AI**: On-device LLMs for titles, summaries, and tag generation
- **ML Kit**: OCR (text extraction) and fallback analysis
- **WorkManager**: Handles deferrable, robust background work like AI analysis and model downloads

---

## 🚀 Getting Started

### ✅ Requirements

- Android 8.0 (API 26) or higher

### 📦 Installation

1. [Download SnapSuite.apk](#) to your Android device.
2. Locate the APK in your **file manager** and tap to install.
3. If prompted, enable **Install from Unknown Sources** for your browser or file manager.
4. Follow the on-screen instructions to complete installation.

---

## 🙌 Why SnapSuite?

SnapSuite is perfect for:
- Researchers collecting references
- Shoppers tracking prices or ideas
- Students saving slides or notes
- Memers managing reaction folders
- Anyone who uses screenshots as memory

All with **zero cloud dependency**.

---

## 📅 Last Updated

**June 27, 2025**
"""