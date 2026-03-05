<p align="center">
  <img src="https://img.shields.io/badge/TRide-Cab%20Booking-00C853?style=for-the-badge&logo=android&logoColor=white" alt="TRide" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat-square&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Firebase-FFCA28?style=flat-square&logo=firebase&logoColor=black" />
  <img src="https://img.shields.io/badge/Google%20Maps-4285F4?style=flat-square&logo=googlemaps&logoColor=white" />
</p>

<p align="center">
  <strong>Reliable city commuting — connect with nearby drivers for a smooth transportation experience</strong>
</p>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.tride.app">
    <img src="https://img.shields.io/badge/Google_Play-414141?style=for-the-badge&logo=google-play&logoColor=white" alt="Get it on Google Play" height="40"/>
  </a>
</p>

---

## 📖 Overview

**TRide** is a full-featured cab booking Android application built with modern Android development practices. It enables users to book rides, drivers to accept and complete trips, and admins to manage the platform — all within a single, scalable codebase.

The app is **live on the Google Play Store** and was developed during an internship at **Invyu Solution Technology**.

---

## ✨ Features

### For Riders

| Feature | Description |
|---------|-------------|
| **Location Search** | Enter pickup and drop-off points with Google Places Autocomplete |
| **Ride Selection** | Choose from taxi, courier, or other service types with upfront pricing |
| **Real-time Tracking** | See your driver's location on the map as they approach |
| **OTP Verification** | Secure ride handoff with OTP matching |
| **Trip Sharing** | Share journey status with trusted contacts |
| **Ride History** | View past trips and receipts |
| **Profile Management** | Edit profile, payment methods, and member preferences |

### For Drivers

| Feature | Description |
|---------|-------------|
| **Incoming Ride Alerts** | Receive push notifications for new ride requests |
| **Driver Registration** | Submit license, Aadhaar, and vehicle details |
| **Document Management** | Upload and manage verification documents |
| **Ride Navigation** | Navigate to pickup and drop-off with turn-by-turn directions |
| **Trip Completion** | Complete rides and generate fare bills |

### For Admins

| Feature | Description |
|---------|-------------|
| **Driver Management** | Approve, reject, or manage driver accounts |
| **Platform Oversight** | Monitor platform activity and user management |

### General

| Feature | Description |
|---------|-------------|
| **Phone Authentication** | Firebase Auth with OTP verification |
| **Role-based Access** | Separate flows for User, Driver, and Admin |
| **Push Notifications** | FCM for ride updates, booking status, and alerts |
| **Offline Support** | Cached data for smoother experience |

---

## 🏗️ Architecture

The app follows **MVVM (Model-View-ViewModel)** with a clean architecture approach:

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer (Compose)                    │
│  Screens: HomeScreen, RideBookingScreen, etc.                │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                    ViewModel Layer                           │
│  HomeViewModel, RideBookingViewModel, etc.                   │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                    Domain Layer                              │
│  UseCases: CreateRideUseCase, GetRidePricingUseCase, etc.     │
└─────────────────────────────┬───────────────────────────────┘
                              │
┌─────────────────────────────▼───────────────────────────────┐
│                    Data Layer                                │
│  Repository → API (Retrofit) + Firebase (Auth, Firestore)   │
└─────────────────────────────────────────────────────────────┘
```

- **UI**: Jetpack Compose screens with `@Composable` functions
- **ViewModel**: Hilt-injected ViewModels with `StateFlow` for UI state
- **Domain**: Use cases encapsulating business logic
- **Data**: `AppRepository` for API calls, Firestore, and local preferences

---

## 🛠️ Tech Stack

| Category | Technology | Purpose |
|----------|------------|---------|
| **Language** | Kotlin 2.0 | Primary language |
| **UI** | Jetpack Compose, Material 3 | Declarative UI |
| **Architecture** | MVVM | Separation of concerns |
| **DI** | Hilt 2.51 | Dependency injection |
| **Navigation** | Navigation Compose 2.7 | Type-safe routing |
| **Networking** | Retrofit 2.9, Moshi | REST API, JSON parsing |
| **Auth** | Firebase Auth, Credentials API | Phone OTP, Google Sign-In |
| **Database** | Cloud Firestore | Real-time data sync |
| **Push** | Firebase Cloud Messaging | Ride notifications |
| **Maps** | Google Maps SDK, Maps Compose | Map display, location |
| **Location** | Play Services Location | GPS, permissions |
| **Images** | Coil | Async image loading |
| **Permissions** | Accompanist Permissions | Runtime permission handling |

---

## 📁 Project Structure

```
app/src/main/java/com/example/invyucab_project/
├── core/                          # Shared infrastructure
│   ├── base/                      # BaseViewModel
│   ├── common/                    # Resource, RideNotificationObserver
│   ├── di/                        # Hilt modules (Network, Firebase, Location)
│   ├── navigations/               # NavGraph, Screen routes
│   ├── network/                   # API configuration
│   ├── service/                   # FirebaseMessagingService
│   ├── theme/                     # Theme, colors
│   └── utils/                     # NotificationUtils, etc.
│
├── data/                          # Data layer
│   ├── api/                       # CustomApiService, GoogleMapsApiService
│   ├── models/                    # RideBookingModels, UserApiModels, etc.
│   ├── preferences/               # UserPreferencesRepository
│   └── repository/                # AppRepository
│
├── domain/                        # Business logic
│   ├── model/                     # AuthUiState, HomeUiState, etc.
│   └── usecase/                   # CreateRideUseCase, GetRidePricingUseCase, etc.
│
└── mainui/                        # Feature screens
    ├── authscreen/                # Auth, OTP
    ├── onboardingscreen/
    ├── roleselectionscreen/       # User / Driver / Admin
    ├── userdetailsscreen/
    ├── driverdetailsscreen/
    ├── homescreen/
    ├── travelscreen/              # Location search, ride selection
    ├── ridebookingscreen/         # Ride booking flow
    ├── bookingdetailscreen/       # Driver details, OTP
    ├── ridetrackingscreen/        # Live tracking
    ├── rideinprogressscreen/      # During ride
    ├── billscreen/
    ├── ridehistoryscreen/
    ├── courierscreen/             # Courier service
    ├── profilescreen/             # Profile, edit, payment, member level
    ├── driverscreen/
    ├── driverprofilescreen/
    ├── driverdocument/
    ├── adminscreen/
    └── managedriversscreen/
```

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Hedgehog (2023.1.1) or newer
- **JDK 11**
- **Android SDK** with compileSdk 36

### Clone the Repository

```bash
git clone https://github.com/Snehil208001/INVYU-CAB_PROJECT-.git
cd INVYU-CAB_PROJECT-
```

### Configuration

#### 1. Google Maps API Key

Create or use an existing key from [Google Cloud Console](https://console.cloud.google.com/) with:
- Maps SDK for Android
- Places API
- Directions API

Add to `local.properties`:

```properties
MAPS_API_KEY=your_google_maps_api_key_here
```

#### 2. Firebase Setup

1. Create a project in [Firebase Console](https://console.firebase.google.com/)
2. Enable **Authentication** (Phone, Google)
3. Enable **Cloud Firestore**
4. Enable **Cloud Messaging**
5. Add an Android app with package `com.tride.app`
6. Download `google-services.json` and place it in `app/`

### Build & Run

```bash
# Debug build
./gradlew assembleDebug

# Run on connected device
./gradlew installDebug
```

Or open the project in Android Studio and run the **app** configuration.

---

## 📱 App Flow

```
Onboarding → Auth (Phone) → Role Selection → [User | Driver | Admin]
     │
     ├── User: Home → Travel → Ride Selection → Ride Booking → Tracking → Bill
     │
     ├── Driver: Driver Screen → Incoming Rides → Ride In Progress → Bill
     │
     └── Admin: Admin Screen → Manage Drivers
```

---

## 🔧 Configuration Summary

| Config | Location | Purpose |
|--------|----------|---------|
| `MAPS_API_KEY` | `local.properties` | Google Maps, Places, Directions |
| `google-services.json` | `app/` | Firebase project config |
| `applicationId` | `app/build.gradle.kts` | `com.tride.app` |
| `minSdk` | `app/build.gradle.kts` | 24 |
| `targetSdk` | `app/build.gradle.kts` | 36 |

---

## 📄 License

This project was developed during an internship at **Invyu Solution Technology**.

---

## 👤 Author

**Snehil**

| Link | URL |
|------|-----|
| LinkedIn | [linkedin.com/in/snehil7542](https://www.linkedin.com/in/snehil7542) |
| GitHub | [@Snehil208001](https://github.com/Snehil208001) |
| TRide on Play Store | [Download](https://play.google.com/store/apps/details?id=com.tride.app) |
