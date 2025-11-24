# FlamAppAI – Android OpenCV Edge Viewer + Web Debug Viewer

This project implements a minimal real‑time edge detection viewer on Android using OpenCV in native C++, plus a small TypeScript‑based web viewer for inspecting processed frames.

## Tech Stack

- Android (Kotlin)
- OpenCV (C++ via NDK / JNI)
- CMake
- TypeScript + plain HTML/CSS

---

## 1. Android app

### Features

- Uses the device camera to capture frames.
- Sends each frame to native C++ via JNI.
- Applies Canny edge detection using OpenCV in native code.
- Displays the processed edge frames in real time on the device.

### Prerequisites

- Android Studio (Hedgehog / Jellyfish or newer)
- Android SDK and build‑tools installed
- NDK + CMake installed via SDK Manager
- OpenCV Android SDK added to the project (included in this repo under `opencv/`)

### Build & Run

1. Clone this repository:
git clone https://github.com/kartik123/flamapp-ai-assignment.git
cd flamapp-ai-assignment/FlamAppAI

2. Open the `FlamAppAI` folder in Android Studio.

3. Let Gradle sync and CMake/NDK configure.

4. Connect an Android device with USB debugging enabled.

5. Select the **app** configuration and click **Run**.

The main screen opens the camera preview and shows the Canny edge‑detected output in real time.

---

## 2. Native OpenCV processing

All image processing runs in C++ for performance:

- Frames from the camera are passed as `Mat` pointers via JNI.
- Native code converts to grayscale, applies a light blur, then Canny edge detection.
- The processed result is written back into the same `Mat` and displayed on screen.

Key files:

- `app/src/main/cpp/native-lib.cpp` – JNI bridge and Canny processing
- `app/src/main/cpp/CMakeLists.txt` – native configuration and OpenCV linkage
- `app/src/main/java/com/kartik/flamappai/NativeBridge.kt` – Kotlin JNI wrapper
- `app/src/main/java/com/kartik/flamappai/OpenCvCameraActivity.kt` – camera + preview activity

---

## 3. Web viewer (`web/`)

The `web` folder contains a minimal TypeScript/HTML viewer that shows sample processed frames exported from the Android app.

### Files

- `web/index.html` – simple page with a 2×2 image grid and a click‑to‑zoom overlay.
- `web/main.ts` / `web/main.js` – TypeScript source and compiled JavaScript.
- `web/frame1.png` … `web/frame4.png` – sample edge‑detected screenshots.

### How to build and open

1. Install TypeScript globally (if not already installed):
npm install -g typescript

This generates `main.js`.

3. Open `index.html` in any modern browser (double‑click the file or use “Open With”).

You will see four edge‑detected frames in a grid. Clicking any image opens it larger in a centered overlay; clicking the dark background closes the overlay. A small stats line shows the resolution and FPS information.

---

## 4. How to reproduce / extend

- To change edge behaviour, edit the Canny parameters in `native-lib.cpp` and rebuild.
- To update web samples, replace `frame1.png`–`frame4.png` with new screenshots from the Android app.
- To extend the web viewer, modify `main.ts` and recompile with `tsc`.

---

## 5. Notes

- The goal of this project is to demonstrate clean integration of **camera → JNI → OpenCV C++ → Android UI**, plus a small **TypeScript web debug viewer**, not to build a full production app.
- Commit history is kept modular with descriptive messages (for example, `feat: native Canny edges and TypeScript web viewer`).
