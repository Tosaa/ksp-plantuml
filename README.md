# Kotlin PlantUML Generator

> A **Gradle plugin** that generates **PlantUML** code from **Kotlin source files** to help you create **accurate, up-to-date UML diagrams** for your project.

---

## 📦 Overview

This plugin is designed to automatically generate **PlantUML** code from your Kotlin codebase using the **Kotlin Symbol Processing API (KSP)**. The generated `.puml` files can be rendered using tools like `plantuml`, online editors, or IDE integrations.

The plugin is **open source**, **privacy-focused**, and **highly customizable**, allowing you to control what parts of your code are included in the diagrams.

---

## 🎯 Purpose

- Provide a **clear overview** of your Kotlin project structure.
- Help **API readers** quickly understand class relationships.
- Ensure **diagrams stay up to date** with your codebase.
- Avoid **data privacy issues** by not sending any user data to third parties.

---

## 🔧 Features

- ✅ **KSP-based symbol processing** for accurate Kotlin code analysis.
- ✅ **Customizable configuration** (e.g., visibility filters, name exclusions, package grouping, ...).
- ✅ **Integrated with Gradle** — runs on every build.
- ✅ **Privacy-first** — only generates `.puml` code, no data is sent to third parties.
- ✅ **Open source** — contribute and extend the plugin!

---

## 📌 Supported Kotlin Features

- Sealed classes
- Object declarations
- Suspend functions
- Visibility modifiers (public, internal, private)
- Custom filtering of elements by name or package
- ...

---

## 📦 Installation

Add the plugin to your `build.gradle.kts`:

```kotlin
plugins {
    id("com.example.kotlin-plantuml-generator") version "1.0.0"
}
```

> Replace `com.example.kotlin-plantuml-generator` with your actual plugin ID and version.

---

## 🚀 Getting Started

### 1. **Add the Plugin to Your Project**

Add the plugin to your `build.gradle.kts` file:

```kotlin
plugins {
    id("com.example.kotlin-plantuml-generator") version "1.0.0"
}
```

### 2. **Run the Gradle Task**

Run the following command to generate PlantUML code:

```bash
./gradlew generatePlantUml
```

> This will generate `.puml` files in the `build/plantuml/` directory by default.

### 3. **Render the Diagrams**

Use a tool like the [PlantUML CLI](https://plantuml.com/overview) or an online editor like [PlantUML Editor](https://www.plantuml.com/plantuml/uml) to render the `.puml` files.

---

## 🛠️ Configuration

You can customize the plugin's behavior using a `plantuml.gradle` file or via Gradle properties.

### Example Configuration

```kotlin
plantuml {
    showPrivate = false
    showInternal = true
    showPublic = true
    excludePackages = listOf("com.example.utils")
    excludeClasses = listOf("com.example.SomeClass")
    outputDirectory = "docs/plantuml"
}
```

---

## 📚 Configuration Guide

### Configuration Options

| Option              | Description                                                                 |
|--------------------|-----------------------------------------------------------------------------|
| `showPrivate`      | Whether to include private members in the diagram. Default: `false`        |
| `showInternal`     | Whether to include internal members in the diagram. Default: `true`        |
| `showPublic`       | Whether to include public members in the diagram. Default: `true`          |
| `excludePackages`  | List of package names to exclude from the diagram.                         |
| `excludeClasses`   | List of class names to exclude from the diagram.                           |
| `outputDirectory`  | The directory where the `.puml` files will be saved. Default: `build/plantuml` |

### Using Gradle Properties

You can also set these options via Gradle properties in your `gradle.properties` file:

```properties
plantuml.showPrivate=false
plantuml.excludePackages=com.example.utils
```

---

## 📥 Contributing

Contributions are welcome! Please:

1. Fork the repository.
2. Make your changes.
3. Submit a pull request.

---

## 📌 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

## 📧 Contact

If you have any questions, feature requests, or need help, feel free to open an issue or reach out to me via email or GitHub.

---
