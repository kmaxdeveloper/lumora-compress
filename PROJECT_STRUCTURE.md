# Project Structure

Detailed overview of the package hierarchy in `uz.kmax.compress`:

- `core/`: Infrastructure and cross-cutting concerns.
    - `di/`: Dependency Injection modules.
    - `compressor/`: Core compression engines.
    - `manager/`: Application managers.
    - `permission/`: Permission handling logic.
    - `storage/`: File and data storage utilities.
    - `navigation/`: Routing and navigation logic.
    - `dispatcher/`: Coroutine dispatchers.

- `common/`: Shared UI and helpers.
    - `extension/`: Kotlin extensions.
    - `util/`: Helper utilities.
    - `widget/`: Reusable UI components.
    - `dialog/`: Custom dialogs.
    - `bottomsheet/`: Reusable bottom sheets.
    - `listener/`: Shared callback interfaces.

- `data/`: Data management.
    - `datasource/`: Data source definitions.
    - `local/`: Local database and cache.
    - `remote/`: Network API services.
    - `repository/`: Repository implementations.
    - `mapper/`: Data-to-Domain mappers.
    - `model/`: DTOs and database entities.

- `domain/`: Business logic.
    - `model/`: Domain entities.
    - `repository/`: Repository interfaces.
    - `usecase/`: Business use cases.
    - `validator/`: Input validation.
    - `exception/`: Domain exceptions.

- `feature/`: UI modules organized by feature.
    - Each feature includes: `fragment`, `viewmodel`, `adapter`, `model`, `state`, `event`.
