# Smart Home App

A modern Android application for controlling and monitoring smart home devices. This application provides an intuitive interface for managing various smart home devices and automations.

## Features

- Smart device control and monitoring
- Real-time device status updates
- User authentication and authorization
- Device automation capabilities
- Intuitive and modern user interface

## Project Structure

```
SmartHomeAppFinal/
├── app/                    # Main application module
│   ├── src/               # Source code
│   │   ├── main/          # Main source set
│   │   │   ├── java/      # Kotlin/Java source files
│   │   │   └── res/       # Resources (layouts, drawables, etc.)
│   ├── build.gradle.kts   # App-level build configuration
│   └── google-services.json # Firebase configuration
├── gradle/                 # Gradle wrapper directory
├── build.gradle.kts       # Project-level build configuration
└── settings.gradle.kts    # Project settings
```

## Prerequisites

- Android Studio Arctic Fox or later
- JDK 11 or later
- Android SDK with minimum API level as specified in build.gradle.kts
- Gradle 7.0 or later

## Setup Instructions

1. Clone the repository:
   ```bash
   git clone https://github.com/Venomous-pie/home_security_final.git
   ```

2. Open the project in Android Studio

3. Sync the project with Gradle files

4. Configure your Firebase project:
   - Create a new Firebase project
   - Add your Android app to the Firebase project
   - Download the `google-services.json` file and place it in the `app/` directory

5. Build and run the application

## Building the Project

To build the project, you can either:

- Use Android Studio's build button
- Run the following command in the terminal:
  ```bash
  ./gradlew assembleDebug
  ```

## Testing

The project includes both unit tests and instrumentation tests. To run the tests:

- Unit Tests:
  ```bash
  ./gradlew test
  ```
- Instrumentation Tests:
  ```bash
  ./gradlew connectedAndroidTest
  ```

## Dependencies

The project uses several key dependencies:

- AndroidX libraries for UI components
- Firebase for backend services
- Kotlin Coroutines for asynchronous programming
- Material Design components for modern UI

For a complete list of dependencies, please refer to the `build.gradle.kts` files.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For any queries or support, please contact the development team.

---

**Note**: This documentation is a living document and will be updated as the project evolves. 
