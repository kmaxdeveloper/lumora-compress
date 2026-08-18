# Feature Layer
The `feature` package contains the presentation logic organized by functional modules. Each feature follows the MVVM pattern.

## Feature Structure
Every feature sub-package contains:
- **fragment**: UI controllers (Fragments) for the feature.
- **viewmodel**: Logic for managing UI state and interacting with use cases.
- **adapter**: View adapters for lists and collections.
- **model**: UI-specific data models.
- **state**: Data classes representing the UI state.
- **event**: Sealed classes or interfaces for UI events and actions.
