# TRide - Cab Booking Platform

A scalable cab booking Android application built with modern Android development practices. TRide is live on the [Google Play Store](https://play.google.com/store/apps/details?id=com.tride.app).

## Features

- **Real-time Location Tracking** – Google Maps SDK and Directions API for ride routing
- **Role-based Experience** – Distinct flows for users, drivers, and admins
- **Secure Authentication** – Firebase Auth with phone OTP verification
- **Push Notifications** – Firebase Cloud Messaging (FCM) for ride updates and booking status
- **Ride Management** – Book rides, track in real-time, view history, and manage payments

## Tech Stack

| Category | Technologies |
|----------|--------------|
| **UI** | Jetpack Compose, Material 3 |
| **Architecture** | MVVM |
| **DI** | Hilt |
| **Networking** | Retrofit, Moshi |
| **Backend Services** | Firebase Auth, Cloud Firestore, FCM |
| **Maps & Location** | Google Maps SDK, Play Services Location |
| **Image Loading** | Coil |

## Project Structure

```
app/
├── core/           # Navigation, services, utils
├── data/           # API, models, repository
├── domain/         # Use cases, domain models
└── mainui/         # Feature screens (auth, home, ride booking, etc.)
```

## Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/Snehil208001/INVYU-CAB_PROJECT-.git
   cd INVYU-CAB_PROJECT-
   ```

2. **Configure `local.properties`**
   Add your Google Maps API key:
   ```properties
   MAPS_API_KEY=your_google_maps_api_key
   ```

3. **Add `google-services.json`**  
   Place your Firebase configuration file in `app/`.

4. **Build and run**
   ```bash
   ./gradlew assembleDebug
   ```

## Requirements

- Android Studio Hedgehog or newer
- JDK 11
- minSdk 24
- targetSdk 36

## Author

**Snehil**  
- LinkedIn: [SNEHIL](https://www.linkedin.com/in/snehil7542)  
- GitHub: [@Snehil208001](https://github.com/Snehil208001)  
- TRide on Play Store: [Download](https://play.google.com/store/apps/details?id=com.tride.app)

## License

This project was developed during an internship at Invyu Solution Technology.
