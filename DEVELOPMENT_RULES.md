# Development Rules

## Coding Standards
- **Language**: Kotlin is the primary language.
- **Architecture**: Strict Clean Architecture + MVVM.
- **Fragments**: All fragments MUST extend `uz.kmax.base.fragment.BaseFragmentNV<VB : ViewBinding>`.
    - Do NOT generate separate BaseFragment classes.
    - Do NOT create custom ViewBinding implementations.
    - Do NOT override `onCreateView()` or `onDestroyView()`.
    - Implement `onViewCreated()` for logic.
    - Use the inherited `binding` property for view access.
    - Use the inherited `navController` property for navigation.
    - Usage Example:
      ```kotlin
      class ExampleFragment : BaseFragmentNV<FragmentExampleBinding>(FragmentExampleBinding::inflate) {
          override fun onViewCreated() {
              // Logic here
          }
      }
      ```
- **SOLID**: Follow SOLID principles for class design.
- **KDoc**: Document all public APIs and complex logic.

## Guidelines
- Use **Coroutines** for asynchronous operations.
- Use **Hilt** for Dependency Injection.
- Follow **Material 3** design guidelines.
- Maintain a clean separation between UI and business logic.
- Ensure all business logic is testable through UseCases.
