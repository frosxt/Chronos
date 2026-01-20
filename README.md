# Chronos

Chronos is a lightweight, high-performance Java scheduling library designed for modern applications.

Standard Java scheduling solutions often force a choice between simplicity and capability. `ScheduledExecutorService` is simple but lacks essential features like Cron support and persistence hooks. Quartz is powerful but heavy, complex, and brings in numerous dependencies. Chronos fills that gap in the middle.

## Features

- **Zero Dependencies**: The library depends only on the Java 21 standard library.
- **Flexible Scheduling**: Support for standard patterns including Cron (timezone-aware), Fixed Rate, Fixed Delay, and One-Shot execution.
- **Resilience**: Built-in support for execution policies, including exponential backoff retries and deadline handling.
- **Jitter Support**: Prevents thundering herd problems by adding configurable randomness (Uniform, Gaussian) to execution schedules.
- **Observability**: First-class support for `TaskListener` and comprehensive metrics snapshots to monitor scheduler health and task performance.
- **High Performance**: Zero-allocation hot paths for recurring tasks and efficient, lock-free state management where possible.

## Installation

This library is available via JitPack.

### Gradle (Groovy)

```gradle
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.frosxt:Chronos:v1.0.0'
}
```

### Gradle (Kotlin)

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}

dependencies {
    implementation("com.github.frosxt:Chronos:v1.0.0")
}
```

### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.frosxt</groupId>
    <artifactId>Chronos</artifactId>
    <version>v1.0.0</version>
</dependency>
```

## Requirements

- Java 21 or newer
- Gradle or Maven

## Documentation

Documentation is available on the [Wiki](https://github.com/frosxt/Chronos/wiki).

## License

MIT License. See [LICENSE](LICENSE).
