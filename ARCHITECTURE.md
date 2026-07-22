# MedLog Android — Architecture Decisions

## Platform & Language
**Kotlin + Jetpack Compose.** Compose is the modern Android UI toolkit — it eliminates XML layout boilerplate, enables composable reusable components, and is Google's recommended path forward. Kotlin's null safety and coroutines integrate seamlessly with Compose and Room.

## Database
**Room over raw SQLite.** Room provides compile-time query verification, type-safe DAOs, and built-in coroutine support. Offline-first requirement means all data is local — Room is the natural choice. SQLite file lives on device storage; no server, no sync.

## Dependency Injection
**Manual dependency injection via an Application-scoped container.** Hilt/Dagger add complexity disproportionate to this app's scope. A simple `AppContainer` interface implemented in `MedLogApplication` holds all repository singletons. Activities/ViewModels receive dependencies via constructor. If the app grows to need scoping beyond application-singleton, Hilt can be retrofitted.

## Navigation
**Jetpack Navigation Compose.** Type-safe routes via sealed class, `NavController` managed by the composable host. Each screen is a `@Composable` function receiving a `NavController` for navigation events. No fragment-based navigation.

## Notification Scheduling
**WorkManager for periodic medication reminders, AlarmManager for exact-time appointment reminders.** WorkManager survives device restarts and respects battery optimization. Exact-time reminders (appointment at 2:30 PM sharp) require `setExactAndAllowWhileIdle` via AlarmManager. Both use `BroadcastReceiver` to show `NotificationCompat` notifications.

## File Storage
**Internal app storage (`context.filesDir`).** Files are private to the app — no shared storage permissions needed for the primary use case (lab results, prescriptions). A `FileHelper` utility manages directory creation, file naming (UUID-based to avoid collisions), and cleanup when attachments are deleted.

## State Management
**`StateFlow` in ViewModels, collected via `collectAsStateWithLifecycle()` in composables.** Room returns `Flow<T>` from DAO queries, which the ViewModel converts to `StateFlow` with an initial value. This gives Compose recomposition only when data actually changes. One-way data flow: UI → ViewModel → Repository → DAO → Room.

## Project Structure
Feature-based package organization. Each feature (medications, conditions, etc.) groups its screen composables, ViewModel, and any feature-specific utilities. Shared entities, DAOs, and repositories live under `data.local`. This keeps feature code co-located while sharing the data layer.