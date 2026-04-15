# COMP3130SEF-Project
COMP3130SEF Project

## Project Members

| Name | Student ID |
|------|-----------|
| Lau Chun Kit | 13897100 |
| Chong Tsz Ho | 13990354 |

## Running the App

### Prerequisites

- **Java Development Kit (JDK)**: Java 17 or higher
- **Android SDK**: API level 34 or compatible
- **Android Studio** (recommended) or command-line tools
- **Gradle**: Version 8.0 or higher (included in the project)

### Setup

1. **Clone or Download the project** to your local machine

2. **Open the project** in Android Studio:
   - File → Open → Select the project folder

3. **Sync Gradle files**:
   - Android Studio will automatically prompt you, or go to File → Sync Now

### Building and Running

#### Option 1: Using Android Studio (Recommended)

1. Connect an Android device via USB or start an Android Emulator
2. Click the **Run** button (green play icon) in the toolbar
3. Select your device/emulator from the dialog
4. The app will build and launch automatically

#### Option 2: Using Command Line

```bash
# Build the APK
./gradlew build

# Install and run on connected device/emulator
./gradlew installDebug

# Or use this shortcut to build and run directly
./gradlew runDebug
```

On Windows, use `gradlew.bat` instead of `./gradlew`

### Emulator Alternative

If you don't have a physical device:
1. Open **Android Device Manager** in Android Studio
2. Create or start an emulator with API level 24 or higher
3. Run the app using the steps above

### Minimum Requirements

- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)
- **Java Version**: 17
