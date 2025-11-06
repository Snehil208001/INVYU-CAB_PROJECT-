🚖 INVYU Cab Project
INVYU is a modern, feature-rich cab booking application for Android, built entirely with Kotlin and Jetpack Compose. It demonstrates a complete, end-to-end user flow, from multi-role authentication to live route-drawing and dynamic pricing from a custom backend.

This repository showcases a clean MVVM architecture, extensive use of Google's location-based services, and a custom API-driven system for user and ride management.

✨ Features
Welcome Onboarding: A skippable, multi-step onboarding flow for first-time users.

Multi-Role Authentication:

Phone (OTP) Sign-Up & Sign-In: Secure registration and login using Firebase Phone Authentication.

Custom User Backend: Verifies user existence (checkUser) and creates new users (createUser) via a custom backend API.

Role-Based System: Users can register as a Rider, Driver, or Admin, each with a distinct flow.

Driver-Specific Onboarding: A separate registration path for drivers to collect license, vehicle, and Aadhaar information.

Complete Ride-Booking Flow:

Home Screen: A clean dashboard prompting the user to select a destination.

Location Search: A dedicated search screen using the Google Places Autocomplete API for finding pickup and drop-off locations.

Live Map & Routing:

Automatically fetches the user's current location as the default pickup point using FusedLocationProviderClient.

Fetches coordinates for named locations using the Google Places Details API.

Draws the optimal route on Google Maps using the Google Directions API.

Dynamic Ride Selection:

A bottom sheet displays available ride types (e.g., Bike, Auto, Cab Economy, Cab Premium).

Fetches dynamic pricing, ETA, and distance for all ride types from a custom backend API (getPricing).

Comprehensive Profile Management:

User Profile: View and edit user details (name, email, gender, DOB).

Member Levels: A screen displaying the user's current membership tier (e.g., Gold Member).

Payment Methods: A UI to view and manage payment options.

Persistent Login: Remembers the user's logged-in state (active) using SharedPreferences, guiding them directly to the app or the auth screen.

Clean Navigation:

A persistent bottom navigation bar for main sections: Ride, All Services, Travel, and Profile.

Uses Jetpack Navigation for Compose to manage all screen transitions.

🛠️ Tech Stack & Architecture
Language: 100% Kotlin

UI: Jetpack Compose

Architecture: MVVM (Model-View-ViewModel)

Dependency Injection: Hilt

Navigation: Jetpack Navigation for Compose

Asynchronous: Kotlin Coroutines & Flow

Networking: Retrofit 2 & OkHttp 3

JSON Parsing: Moshi

Authentication: Firebase Authentication (Phone OTP)

Location & Maps:

Google Maps SDK for Android

Google Places API (Autocomplete & Details)

Google Directions API

FusedLocationProviderClient (for current location)

Local Storage: SharedPreferences (for user session)

Backend:

Firebase (for OTP verification)

Custom AWS API (for user/ride management & pricing)

⚙️ Setup Instructions
To build and run this project, you will need to configure Firebase and Google Maps.

1. Clone the Repository
Bash

git clone https://github.com/your-username/invyu-cab_project.git
cd invyu-cab_project/INVYUCAB_PROJECT
2. Configure Firebase
Go to the Firebase Console and create a new project.

Add an Android app with the package name: com.example.invyucab_project.

Go to the Authentication section and enable the Phone Number sign-in provider.

Download the google-services.json file provided by Firebase.

Place the google-services.json file in the INVYUCAB_PROJECT/app/ directory.

3. Configure Google Maps API Key
Go to the Google Cloud Console and get an API key.

Make sure your key has the following APIs enabled:

Maps SDK for Android

Places API

Directions API

Create a file named local.properties in the root directory of the INVYUCAB_PROJECT (the same directory as settings.gradle.kts).

Add your API key to the local.properties file:

Properties

MAPS_API_KEY="YOUR_GOOGLE_MAPS_API_KEY_HERE"
4. Custom Backend Note
This project is configured to work with a specific custom backend API hosted at https://ovlo8ek40d.execute-api.us-east-1.amazonaws.com/. This backend handles:

Checking if a user exists (checkUser)

Creating new users (createUser)

Updating user status (updateUserStatus)

Calculating ride prices (getPricing)

Without access to this backend, the authentication and ride-pricing features will not function.

5. Build & Run
Open the INVYUCAB_PROJECT directory in Android Studio.

Let Gradle sync and build the project.

Run the app on an emulator or a physical device.
