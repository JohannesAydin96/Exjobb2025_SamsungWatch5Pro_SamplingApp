# Sample App – Exercise Data Collection

This Android application was developed in **Android Studio** as part of a thesis project.  
The app is designed to collect exercise data such as **heart rate** and **accelerometer data**, with optional **GPS tracking**.  

When an activity or data collection session is finished, the recorded data can be **exported as a CSV file** and sent via email. This allows further analysis of the raw exercise data outside of the app.

## Background
In recent years, wearable devices such as fitness trackers and smartwatches have become widely used for monitoring physical activity and health metrics. However, in research settings, particularly when working with adolescents with **neuropsychiatric conditions (NPF)**, there is a strong need for applications that provide both **simplicity for the user** and **researcher control over raw data collection**.  

This project was created to explore how developers can access raw data from wearables and design new user interfaces tailored to specific research needs. The goal was to build a prototype that enables controlled data collection, minimizes user effort, and provides researchers with accurate and exportable exercise data.

## Features
- Collects **heart rate data** from a wearable device.  
- Collects **accelerometer data** at configurable sampling rates.  
- Supports **GPS tracking** for location-based activities.  
- **Exports session data as CSV** for further analysis.  
- Designed for use in **exercise tests and research scenarios**.

## Requirements
- Tested with **Samsung Galaxy Watch 5 Pro**.  
- A Wear OS device with **Health Services** support, or a suitable **emulator**, is required to run the app.  

## Usage
1. Open the app on your Wear OS device or emulator.  
2. Start an exercise session – data will be continuously recorded.  
3. End the session when finished.  
4. Export the data as a **CSV file** and send it to the specified email address for analysis.  
