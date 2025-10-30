🚖 INVYU (CAB_PROJECT)

INVYU is a modern, feature-rich cab booking application built for Android 🚀
Developed entirely in Kotlin, it leverages Jetpack Compose for a smooth declarative UI and follows a Clean MVVM architecture for scalability and maintainability.

The app provides a complete ride-booking experience — from authentication and live location tracking to ride selection and route visualization.

✨ Features
🧭 Onboarding Flow

Multi-step, welcoming onboarding screens for new users.

Introduces key features and app benefits before sign-up.

🔐 Complete Authentication

📱 Phone (OTP) Sign-Up & Sign-In — Register/login using mobile number with Firebase OTP verification.

🔑 Google Sign-In — One-tap sign-in/sign-up with Google credentials (via Firebase).

🧍‍♂️ Profile Setup — After registration, users can add name and email easily.

🏠 Home Dashboard

“Where are you going?” search bar to initiate ride booking.

🛺 List of services like Auto, Bike, and Cabs under an “Explore” section.

🕒 Displays recently visited locations for quick booking.

📍 Location Search

Dedicated search screen to find drop-off locations.

⚡ Integrated with Google Places Autocomplete API for real-time suggestions.

🗺️ Live Map & Ride Selection

Fetches current location automatically as pickup.

Retrieves drop-off coordinates using Google Places Details API.

Draws the optimal route with Google Directions API and smooth polyline animation.

Adds custom pickup/drop-off markers on the map.

🚗 Bottom sheet shows available rides with estimated prices and ETAs.

👤 Comprehensive Profile Management

📄 Main Profile — View name, phone, and basic info.

✏️ Edit Profile — Update name, email, gender, and birthday.

💳 Payment Methods — Manage cash, UPI, and card payments.

🏅 Member Levels — Track membership tier (Bronze, Silver, Gold).

🧭 Multi-Screen Navigation

Clean tab-based navigation for main sections:
🚕 Ride | 🧰 All Services | 🌍 Travel | 👤 Profile

🛠️ Tech Stack & Architecture
Component	Technology Used
Language	Kotlin
UI	Jetpack Compose
Architecture	MVVM (Model–View–ViewModel)
DI Framework	Hilt (Dagger-Hilt)
Navigation	Jetpack Navigation for Compose
Asynchronous	Kotlin Coroutines & Flow
Networking	Retrofit 2 + OkHttp 3
JSON Parsing	Moshi
Backend	Firebase Authentication (Phone & Google)
Maps & APIs	Google Maps SDK, Places API, Directions API
Location Services	FusedLocationProviderClient
⚙️ Setup Instructions
1️⃣ Clone the Repository
git clone https://github.com/your-username/INVYU-CAB_PROJECT.git
cd INVYU-CAB_PROJECT

2️⃣ Add Google Maps API Key

Create a file named local.properties in the root directory:

MAPS_API_KEY="YOUR_GOOGLE_MAPS_API_KEY"


🗝️ Make sure your key has access to:

Maps SDK for Android

Places API

Directions API

The project automatically reads this key in AndroidManifest.xml and BuildConfig.

3️⃣ Configure Firebase

Go to Firebase Console
 → Create a new Android project.

Register the app with package name:

com.example.invyucab_project


Enable Phone Number and Google Sign-In in Firebase Authentication.

Download the google-services.json file and place it inside:

INVYU-CAB_PROJECT/app/google-services.json

4️⃣ Build & Run 🧩

Open the project in Android Studio.

Let Gradle sync dependencies.

Click ▶️ Run on your emulator or physical device.

💡 Future Enhancements

🚕 Real-time driver tracking with Firebase Realtime Database.

💬 In-app chat between driver and rider.

🌎 Multi-language support (English, Hindi, etc.).

💸 Promo codes and referral system.

🧑‍💻 Contributing

Pull requests are welcome! Feel free to open issues for bugs, ideas, or improvements.
Make sure to follow Kotlin & Jetpack Compose best practices.
