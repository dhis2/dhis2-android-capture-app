# DHIS2 Android Capture App

[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=dhis2_dhis2-android-capture-app&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=dhis2_dhis2-android-capture-app)
[![Reliability Rating](https://sonarcloud.io/api/project_badges/measure?project=dhis2_dhis2-android-capture-app&metric=reliability_rating)](https://sonarcloud.io/summary/new_code?id=dhis2_dhis2-android-capture-app)

Check the [Wiki](https://github.com/dhis2/dhis2-android-capture-app/wiki) for information about how to build the project and its architecture **(WIP)**

## What is this repository for?

DHIS2 Android Capture App is a mobile application for Android that allows offline data capture and synchronization with DHIS2 servers. This project is currently migrating from a traditional Android app to a **Kotlin Multiplatform (KMP)** project with the goal of becoming a full Compose Multiplatform application supporting Android, iOS, and Desktop platforms.

## Tech Stack

### Core Technologies

- **[Kotlin](https://kotlinlang.org/)** `2.2.20` - Primary programming language
- **[Kotlin Multiplatform (KMP)](https://kotlinlang.org/docs/multiplatform.html)** - Cross-platform development framework (migration in progress)
- **[Gradle](https://gradle.org/)** `8.13.0` - Build system with Kotlin DSL

### UI Framework

- **[Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)** `1.9.0` - Declarative UI framework for all platforms
- **[DHIS2 Mobile Design System](https://ui.dhis2.nu/components)** `0.6.0-SNAPSHOT` - Custom design system based on Compose Multiplatform
- **[Jetpack Compose](https://developer.android.com/jetpack/compose)** `1.9.1` - Android UI toolkit
- **[Material Design 3](https://m3.material.io/)** `1.3.2` - Material Design components
- **[Lottie](https://airbnb.io/lottie/)** `6.6.9` - Animation library

### Data Layer

- **[DHIS2 Android SDK](https://docs.dhis2.org/en/develop/using-the-api/dhis-core-version-master/android-sdk.html)** `1.13.0-SNAPSHOT` - Handles data persistence, offline/online synchronization, and DHIS2 API communication
- **[DHIS2 Rule Engine](https://github.com/dhis2/dhis2-rule-engine)** `3.3.11` - Business rule execution engine
- **[OkHttp](https://square.github.io/okhttp/)** `4.12.0` - HTTP client
- **[Gson](https://github.com/google/gson)** `2.13.2` - JSON serialization/deserialization
- **[Kotlinx Serialization](https://github.com/Kotlin/kotlinx.serialization)** `1.9.0` - Kotlin serialization library

### Architecture & Patterns

- **MVVM Architecture** - Model-View-ViewModel pattern
- **Repository Pattern** - Data access abstraction layer
- **Use Cases** - Business logic encapsulation
- **Clean Architecture** - Separation of concerns with domain, data, and presentation layers

### Dependency Injection

- **[Koin](https://insert-koin.io/)** `4.1.1` - Dependency injection framework for Kotlin Multiplatform
- **[Dagger Hilt](https://dagger.dev/hilt/)** `2.57.1` - Dependency injection for Android (being migrated to Koin)

### Reactive Programming

- **[Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)** `1.10.2` - Asynchronous programming
- **[Kotlin Flow](https://kotlinlang.org/docs/flow.html)** - Reactive data streams
- **[RxJava 2](https://github.com/ReactiveX/RxJava)** `2.2.21` - Reactive extensions (being migrated to Coroutines/Flow)
- **[RxAndroid](https://github.com/ReactiveX/RxAndroid)** `2.1.1` - Android-specific RxJava bindings

### Maps & Location

- **[MapLibre Android SDK](https://github.com/maplibre/maplibre-native)** `11.13.5` - OpenStreetMap-based mapping library
- **[MapLibre GeoJSON](https://github.com/maplibre/maplibre-java)** `6.0.1` - GeoJSON support

### Analytics & Monitoring

- **[Matomo](https://matomo.org/)** `4.4` - Analytics tracking
- **[Sentry](https://sentry.io/)** `8.22.0` - Error tracking and monitoring
- **[Timber](https://github.com/JakeWharton/timber)** `5.0.1` - Logging library

### Testing

- **[JUnit 4](https://junit.org/junit4/)** `4.13.2` - Unit testing framework
- **[JUnit Jupiter (JUnit 5)](https://junit.org/junit5/)** `5.13.4` - Modern unit testing framework
- **[Mockito](https://site.mockito.org/)** `5.20.0` - Mocking framework
- **[Mockito Kotlin](https://github.com/mockito/mockito-kotlin)** `6.0.0` - Kotlin extensions for Mockito
- **[Espresso](https://developer.android.com/training/testing/espresso)** `3.7.0` - UI testing framework
- **[AndroidX Test](https://developer.android.com/testing)** `1.7.0` - Android testing library
- **[Truth](https://truth.dev/)** `1.4.5` - Fluent assertion library
- **[Turbine](https://github.com/cashapp/turbine)** `1.2.1` - Flow testing library

### Security

- **[Conscrypt](https://github.com/google/conscrypt)** `2.5.3` - Modern TLS/SSL provider
- **[AppAuth](https://github.com/openid/AppAuth-Android)** `0.11.1` - OAuth 2.0 and OpenID Connect client
- **[RootBeer](https://github.com/scottyab/rootbeer)** `0.1.1` - Root detection library
- **[Biometric](https://developer.android.com/jetpack/androidx/releases/biometric)** `1.1.0` - Biometric authentication

### Other Libraries

- **[AndroidX Libraries](https://developer.android.com/jetpack/androidx)** - Jetpack components (Core KTX, Lifecycle, WorkManager, Navigation, Paging, etc.)
- **[Guava](https://github.com/google/guava)** `33.5.0-android` - Core libraries for Java
- **[Joda-Time](https://www.joda.org/joda-time/)** `2.14.0` - Date and time library
- **[Glide](https://github.com/bumptech/glide)** `5.0.5` - Image loading and caching
- **[Coil](https://coil-kt.github.io/coil/)** `3.3.0` - Image loading for Compose
- **[ZXing](https://github.com/zxing/zxing)** `3.5.3` - Barcode scanning library

### Build & Development Tools

- **[ktlint](https://pinterest.github.io/ktlint/)** `13.1.0` - Kotlin linter
- **[SonarQube](https://www.sonarqube.org/)** `6.3.1.5724` - Code quality and security analysis
- **[Jacoco](https://www.jacoco.org/jacoco/)** `0.8.13` - Code coverage tool
- **[KSP (Kotlin Symbol Processing)](https://kotlinlang.org/docs/ksp-overview.html)** `2.2.20-2.0.3` - Annotation processing for Kotlin

## Documentation

For detailed documentation about building the project, architecture, and development guidelines, please visit:
- [Project Wiki](https://github.com/dhis2/dhis2-android-capture-app/wiki)
- [DHIS2 Android Documentation](https://github.com/dhis2-android-docs)
