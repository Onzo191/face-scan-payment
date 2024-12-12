# Face Scan Payment

Onzo &nbsp; [![GitHub](https://img.shields.io/badge/GitHub-000000?logo=github&logoColor=white)](https://github.com/Onzo191), Trieu1212 &nbsp; [![GitHub](https://img.shields.io/badge/GitHub-000000?logo=github&logoColor=white)](https://github.com/trieu1212)

Face authentication for payment using Facenet512 and Mediapipe face detector.

## Introduction

**Face Scan Payment** is an Android application that uses face recognition technology to authenticate payment transactions. The app extracts face *embeddings* from the user's face, then sends this data to the server to authenticate or compare with stored data in the database.

The application uses two main technologies:

1. **Facenet512**: A deep learning neural network trained to extract face features (face embeddings), enabling high-accuracy face recognition.
2. **Mediapipe Face Detector**: A Google library that detects and tracks faces in real-time, serving as the face detection part of the process.

## Features

- **Face Detection**: Uses Mediapipe to detect the user's face.
- **Embeddings Extraction**: Facenet512 is used to extract face *embeddings* from the face image, providing a unique feature vector for each user.
- **Payment Authentication**: After extracting the *embeddings*, the app sends this data to the server to compare and authenticate the payment.
- **Storage & Comparison**: The server stores *embeddings* and compares them with existing records to identify or authenticate the user.

## Project Structure

- **Android Client**: The Android application that uses Mediapipe for face detection and Facenet512 for embeddings extraction. The data is sent to the server via an API.
- **Server**: The server receives and stores *embeddings*, then compares them for authentication. The *embeddings* can be stored in a database for future comparisons.  
  You can access the server repository here: [Server Repo Link](https://github.com/trieu1212/flask-api).
