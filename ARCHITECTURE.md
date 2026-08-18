# Lumora Compress Architecture

This project follows the **Clean Architecture** pattern to ensure a scalable, maintainable, and testable codebase.

## Layers

### 1. Domain Layer (Pure Kotlin)
Contains business logic, use cases, and repository interfaces. It is the innermost layer and has no dependencies on other layers.

### 2. Data Layer
Implements repository interfaces, handles networking, local persistence, and data mapping. It depends on the Domain layer.

### 3. Core Layer
Provides infrastructure support such as DI, global managers, and low-level utilities.

### 4. Feature Layer (Presentation)
Handles UI and user interactions using the MVVM pattern. It depends on the Domain and Core layers.

### 5. Common Layer
Contains shared UI components and utilities used across features.
