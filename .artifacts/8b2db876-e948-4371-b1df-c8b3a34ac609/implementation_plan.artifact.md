# Implementation Plan - Final Release Hardening

## Goal
Harden the application for production release (Gold Master), focusing on Billing safety, Binder safety, Memory/OOM protection, and Android 16 compatibility.

## User Review Required
> [!IMPORTANT]
> A real Google Play Base64 public key is required to enable production billing verification. Current implementation uses "Safe Failure" (denies Pro entitlement when key is missing).

## Proposed Changes

### P0 — Memory & Binder Safety
#### [MODIFY] [BatchViewModel.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/feature/batch/viewmodel/BatchViewModel.kt)
- Optimize `updateItemState` to avoid full list copying for every progress update.
- Ensure large lists (20k images) are handled efficiently.

#### [MODIFY] [SelectedImagesRepository.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/core/storage/SelectedImagesRepository.kt)
- Add a timeout or maximum batch count to prevent memory leaks from abandoned batches.

### P0 — Architecture & Robustness
#### [MODIFY] [CompressionService.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/core/service/CompressionService.kt)
- Refactor to drive the batch compression loop within the service instead of the `ViewModel`.
- Ensure the service accurately represents the workload and survives process death.

#### [MODIFY] [BatchViewModel.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/feature/batch/viewmodel/BatchViewModel.kt)
- Delegate the actual compression loop to `CompressionService`.
- Observe service state for UI updates.

### P0 — Storage & Cleanup
#### [MODIFY] [StorageManager.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/core/storage/StorageManager.kt)
- Add `deleteFile(uri: Uri)` method.

#### [MODIFY] [CompressionEngineImpl.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/core/compressor/impl/CompressionEngineImpl.kt)
- Ensure temporary files from failed iterations or intermediate steps are cleaned up.

### P1 — Production Configuration
#### [MODIFY] [AdsManager.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/main/java/uz/kmax/compress/core/monetization/AdsManager.kt)
- Replace sample AdMob ID with production ID or a configurable field.

#### [MODIFY] [CompressionEngineTest.kt](file:///C:/Users/User/AndroidStudioProjects/LumoraCompress/app/src/test/java/uz/kmax/compress/core/compressor/impl/CompressionEngineTest.kt)
- Fix timeout issue by using `UnconfinedTestDispatcher` or proper clock advancement.

## Verification Plan

### Automated Tests
- `./gradlew testDebugUnitTest`
- New unit tests for:
    - `BatchViewModel` list update performance (simulated large list).
    - `CompressionService` batch lifecycle.
    - `StorageManager` cleanup.

### Manual Verification
- **Stress Test**: Select 10,000+ images in Gallery, navigate to Batch, ensure no crash.
- **Process Death**: Start batch compression, move app to background, kill process, verify notification stays and work continues (if possible) or service restarts correctly.
- **Billing**: Verify "Safe Failure" remains active until the real key is provided.
