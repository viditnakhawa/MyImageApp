# 📸 SnapSuite — Intelligent Screenshot Manager

**SnapSuite** is an intelligent, privacy-focused **screenshot management app** for Android, powered entirely by **on-device AI**.

It automatically organizes your screenshots, enriches them with **AI-generated titles, summaries, tags**, and helps you group them into **meaningful collections** — all with a sleek and dynamic UI built using Jetpack Compose.

---

## ✨ Core Features

- 🔍 **Automatic Screenshot Discovery**  
  Scans your device's screenshot folder and imports screenshots automatically.

- 🧠 **AI-Powered Image Analysis** (via [Gemma](https://developers.googleblog.com/en/introducing-gemma-3n/)):
  - 📌 Descriptive title
  - 📝 Concise summary
  - 🏷️ Relevant tags
  - 📱 Detected source app

- 🔡 **Intelligent OCR**  
  Extracts text using **ML Kit** and enhances it with **Gemma** for clean readability.

- 📂 **Organize with Collections**  
  Group screenshots into custom-named collections with ease.

- 🎨 **Modern & Dynamic UI**
  - Built with **Jetpack Compose**
  - Long-press a screenshot to **"peek"** at its AI-generated title
  - Analysis screen background adapts to the **dominant image color**

- 🔒 **On-Device Privacy**  
  No cloud processing — AI inference happens **entirely on your phone**. Your screenshots never leave your device.

---

## 🛠️ Tech Stack

| Layer              | Tools & Libraries                                           |
|--------------------|------------------------------------------------------------|
| **UI**             | `Jetpack Compose`, Material 3                              |
| **Architecture**   | `MVVM`, Repository pattern                                 |
| **Async**          | `Kotlin Coroutines`, `Flow`                                |
| **Database**       | `Room`                                                     |
| **AI / ML**        | `MediaPipe GenAI (Gemma)`, `ML Kit (OCR)`                  |
| **Image Loading**  | `Coil`                                                     |
| **Dependency Management** | `Gradle Version Catalogs`                        |

---

## 🚀 Getting Started

```bash
git clone https://github.com/your-username/snapsuite.git
