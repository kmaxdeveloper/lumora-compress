# Domain Layer
The `domain` layer contains the core business logic of the application. It is independent of any other layers (Android-agnostic).

## Sub-packages
- **model**: Business models used across the application.
- **repository**: Interfaces for data operations, to be implemented by the data layer.
- **usecase**: Specific business logic units that execute a single task.
- **validator**: Logic for validating business models and user input.
- **exception**: Custom business and domain-specific exceptions.
