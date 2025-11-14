# Flamapp.AI Assignment

## Project Overview
This project is a Flamapp.AI technical assignment consisting of:
- An Android app that captures camera frames and processes them using OpenCV via JNI.
- A simple web viewer built with TypeScript to display processed frames and stats.

## Features
- Real-time edge detection on Android using OpenCV C code.
- JNI bridge between Android and native processing code.
- OpenGL ES 2.0 rendering of the processed output.
- Web-based viewer displaying frames with overlaid stats.

## Setup Instructions

### Android App
- Requires Android SDK & NDK.
- Build using Android Studio or Gradle CLI.
- OpenCV native dependencies configured in JNI.

### Web Viewer
- Requires Node.js installed.
- Run `npm install` and `npm start` in the web-viewer folder.

## Architecture Summary
- Android captures camera frames → sends frames via JNI → processed in native C using OpenCV → rendered by OpenGL.
- Web viewer fetches processed frame images and data → displays with overlay stats using React + TypeScript.
