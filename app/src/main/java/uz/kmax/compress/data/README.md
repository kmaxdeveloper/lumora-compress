# Data Layer
The `data` layer is responsible for data retrieval and persistence. it implements the repository interfaces defined in the `domain` layer.

## Sub-packages
- **datasource**: Interfaces and implementations for remote and local data sources.
- **local**: Local persistence logic (e.g., Room, SharedPreferences, DataStore).
- **remote**: Networking logic and API service definitions (e.g., Retrofit).
- **repository**: Implementations of the domain repository interfaces.
- **mapper**: Utility classes to convert between data models and domain models.
- **model**: Data transfer objects (DTOs) and local entity models.
