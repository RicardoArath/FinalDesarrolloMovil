# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**rentFlowMax** is an Android property management application (MVVM, Room, Navigation Component, ViewBinding). UI in Spanish, currency MXN. Manages properties, tenants, contracts, payments, and maintenance requests.

- **AGP**: 9.2.1 | **Gradle**: 9.4.1 | **Min SDK**: 35 | **Target SDK**: 36
- **Java**: 11 (source compatibility) | **Kotlin** via AGP built-in plugin
- **Build system**: Gradle with Kotlin DSL (`.kts` files)
- **Dependency management**: Version catalog (`gradle/libs.versions.toml`)

## Common Development Commands

### Building
```bash
JAVA_HOME=/home/umax/.jdks/jbr-17.0.14 ./gradlew assembleDebug   # debug APK
JAVA_HOME=/home/umax/.jdks/jbr-17.0.14 ./gradlew assembleRelease  # release APK
```
> `JAVA_HOME` must be set explicitly — the system PATH does not have `java` by default.

### Testing
```bash
JAVA_HOME=/home/umax/.jdks/jbr-17.0.14 ./gradlew test                    # unit tests
JAVA_HOME=/home/umax/.jdks/jbr-17.0.14 ./gradlew connectedAndroidTest    # instrumented tests
```

### Installation
```bash
JAVA_HOME=/home/umax/.jdks/jbr-17.0.14 ./gradlew installDebug    # install on device
JAVA_HOME=/home/umax/.jdks/jbr-17.0.14 ./gradlew uninstallDebug  # remove from device
```

## Project Structure

```
rentFlowMax/
├── app/src/main/
│   ├── java/com/example/rentflowmax/
│   │   ├── data/
│   │   │   ├── dao/           # Room DAOs (Property, Tenant, Contract, Payment, Maintenance)
│   │   │   ├── db/            # AppDatabase + Converters
│   │   │   ├── model/         # Entities + relation DTOs (ContractWithDetails, etc.)
│   │   │   └── repository/    # Repositories (one per entity)
│   │   ├── ui/
│   │   │   ├── common/        # ViewModelFactory (manual DI)
│   │   │   ├── dashboard/     # DashboardFragment + ViewModel
│   │   │   ├── properties/    # List / Detail / Form fragments + ViewModels
│   │   │   ├── tenants/       # List / Detail / Form fragments + ViewModels
│   │   │   ├── contracts/     # List / Detail / Form fragments + ViewModels
│   │   │   ├── payments/      # List / Form fragments + ViewModels
│   │   │   └── maintenance/   # List / Detail / Form fragments + ViewModels
│   │   ├── util/              # CurrencyUtils, DateUtils
│   │   └── RentFlowMaxApp.kt  # Application class (manual DI, creates repositories)
│   └── res/
│       ├── layout/            # Fragment layouts (fragment_*.xml)
│       ├── navigation/        # nav_graph.xml — single Navigation Component graph
│       └── values/            # strings.xml (all UI text in Spanish)
├── gradle/
│   └── libs.versions.toml     # Centralized dependency versions
└── app/build.gradle.kts       # App module config (Room KSP, Navigation, etc.)
```

## Architecture

- **Pattern**: MVVM with Repository layer
- **Navigation**: Single-activity, Navigation Component (`nav_graph.xml`)
- **Bottom nav tabs**: Dashboard, Propiedades, Inquilinos, Pagos, Mantenimiento
- **Contracts**: No bottom tab — accessed from Property detail or Tenant detail
- **DI**: Manual via `RentFlowMaxApp` (no Hilt/Dagger)
- **Database**: Room with KSP code generation
- **Async**: Kotlin Coroutines + StateFlow

## Key UX Pattern: Guided Navigation on Missing Dependencies

Form fragments check their required dependencies after the first data load and show a `MaterialAlertDialog` if any are missing, offering to navigate the user to register them:

| Form | Missing dependency | Dialog action |
|---|---|---|
| `ContractFormFragment` | No properties | → `PropertyFormFragment` |
| `ContractFormFragment` | No tenants (props OK) | → `TenantFormFragment` |
| `PaymentFormFragment` | No active contracts | → `ContractListFragment` |
| `MaintenanceFormFragment` | No properties | → `PropertyFormFragment` |

Implementation pattern (flags `dependencyChecked`, `propertiesLoaded`, `tenantsLoaded` in fragments):
- Check fires only once per fragment instance (flag guard)
- Dialog is `setCancelable(false)` — negative button navigates up (back to list)
- After creating the missing entity, back-stack returns to the form and the ViewModel auto-refreshes via Flow

## Key Pattern: StateFlow Loading State with Nullable Initial Value

Form ViewModels that load lists from Room (`properties`, `tenants`, `activeContracts`) use **`StateFlow<List<T>?>` with `null` initial value** — not `emptyList()`. The synthetic empty list emitted by `stateIn` before Room responds would otherwise look identical to a real "empty database" result and trigger false dependency dialogs.

```kotlin
val properties: StateFlow<List<Property>?> =
    propertyRepository.getAllProperties()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
```

Fragments must skip the loading state with `?: return@collect`:
```kotlin
viewModel.properties.collect { props ->
    props ?: return@collect  // ignore loading state
    // ... safe to evaluate emptiness here
}
```

## Key Pattern: Selection Race Condition in Edit Forms

When a form is opened in edit mode, two Flows race: the entity being edited (e.g. `viewModel.maintenance`) and the dropdown options (e.g. `viewModel.properties`). If the entity arrives first, the dropdown list is still empty and the selection silently fails to apply.

Solution: a `pendingXxxIdToSelect: Long` field. Each collector applies what it can and stashes the rest:

```kotlin
// In the options collector — try pending first, then preselected arg
val idToSelect = when {
    pendingPropertyIdToSelect != -1L -> pendingPropertyIdToSelect
    preselectedPropertyId != -1L -> preselectedPropertyId
    else -> -1L
}
if (idToSelect != -1L) {
    props.firstOrNull { it.id == idToSelect }?.let {
        binding.acvProperty.setText(it.name, false)
        pendingPropertyIdToSelect = -1L
    }
}

// In the entity collector — stash if list not loaded yet
if (propertyList.any { it.id == m.propertyId }) {
    binding.acvProperty.setText(prop.name, false)
} else {
    pendingPropertyIdToSelect = m.propertyId
}
```

## Validation & Save Patterns

- **Date queries treat expired as inactive**: contracts with `endDate < now` are excluded from "active" queries via `WHERE isActive = 1 AND endDate >= :now`. Apply the same filter in in-memory relation getters (`PropertyWithActiveContract.activeContract`, `TenantWithContracts.hasActiveContract`).
- **Pre-save uniqueness checks** (no double active contracts, no duplicate payments per period) live in the ViewModel `save()` with an `onConflict`/`onDuplicate` callback. The fragment shows the error on the relevant `TextInputLayout` and re-enables `btnSave`.
- **Disable `btnSave` before calling `viewModel.save()`** to prevent double-tap duplicates. Re-enable only in conflict callbacks; on success the fragment navigates away.
- **FK `RESTRICT` (Contract → Property/Tenant)**: detail fragments check `contractHistory.value.size` / `contracts.value.size` before allowing delete and surface a `delete_blocked_*` dialog if any exist.
- **FK `CASCADE` (Payment → Contract, Maintenance → Property)**: delete dialogs append a warning ("Esto eliminará N pagos/mantenimientos asociados") when relations exist.
- **Amount validation**: `> 0` for required amounts (rent, payment), `>= 0` for optional (deposit, costs). Always check `toDoubleOrNull()` before the sign check.
- **Year range for payment period**: `2000..(currentYear + 1)`.
- **Payment date**: must be `<= now`.
- **Email**: use `android.util.Patterns.EMAIL_ADDRESS` when non-empty (email is optional in `TenantForm`).
- **Locale**: use `Locale.forLanguageTag("es-MX")` — never `Locale("es", "MX")` (deprecated in Java 21).

## Adapter Pattern: Label/Name Maps via ViewModel

Adapters that display related entity names (e.g. `PaymentAdapter` needs the property+tenant label for each `contractId`) expose a mutable `Map<Long, String>` and an `updateXxx()` method that calls `notifyItemRangeChanged(0, itemCount)`. The ViewModel collects from a repository and provides the map via a `StateFlow<Map<Long, String>>`:

```kotlin
// ViewModel
val contractLabels: StateFlow<Map<Long, String>> =
    contractRepository.getAllContractsWithDetails()
        .map { it.associate { c -> c.contract.id to "${c.property.name} - ${c.tenant.fullName}" } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

// Adapter
private var contractLabels: Map<Long, String> = emptyMap()
fun updateContractLabels(labels: Map<Long, String>) {
    contractLabels = labels
    if (itemCount > 0) notifyItemRangeChanged(0, itemCount)
}
```

Never collect repositories directly from fragments and never use `notifyDataSetChanged()`.

## Dashboard Multi-Flow Combination

When a piece of UI depends on multiple StateFlows (e.g. "available = total − rented"), use `combine` — not nested collectors reading `.value`, which race:

```kotlin
combine(viewModel.totalPropertyCount, viewModel.rentedPropertyCount) { t, r -> t to r }
    .collect { (total, rented) ->
        binding.tvAvailableCount.text = (total - rented).coerceAtLeast(0).toString()
    }
```

## Process Death Survival

Form fragments persist non-EditText state (selected date `Long`s) via `onSaveInstanceState`. `EditText`/`AutoCompleteTextView` content is restored automatically by Android. See `ContractFormFragment` / `PaymentFormFragment` for `KEY_START_DATE`, `KEY_END_DATE`, `KEY_PAYMENT_DATE` examples.

## Destructive Action UX

The Payment list uses **long-press to delete** (no detail screen). A small hint TextView `tv_long_press_hint` ("Toca para editar · Mantén pulsado para eliminar") is shown above the list whenever payments are present.

## Dependencies

Core libraries (see `gradle/libs.versions.toml` for versions):
- `androidx.room` + KSP — local database
- `androidx.navigation` — single-activity navigation
- `androidx.lifecycle` (ViewModel + Coroutines) — MVVM + async
- `com.google.android.material` — Material Design 3 components (including `MaterialAlertDialogBuilder`)
- `kotlinx.coroutines` — async/Flow
- `androidx.appcompat`, `androidx.core-ktx` — base Android support

## Key Build Configuration

- **Namespace / Application ID**: `com.example.rentflowmax`
- **Minification**: disabled in debug, enabled in release
- **KSP**: used for Room annotation processing
- **Test runner**: `androidx.test.runner.AndroidJUnitRunner`

## Tips for Development

- Always set `JAVA_HOME=/home/umax/.jdks/jbr-17.0.14` when running Gradle — the system PATH has no `java`.
- All UI strings live in `res/values/strings.xml` (Spanish only).
- When adding a new form that depends on another entity, follow the guided-navigation pattern above.
- When adding navigation actions, declare them in `nav_graph.xml` on the **source** fragment node.
- When adding dependencies, update `gradle/libs.versions.toml` first, then reference the alias in `app/build.gradle.kts`.
- When loading lists from Room into a form, use `StateFlow<List<T>?>` with `null` initial — see the loading state pattern.
- When binding labels from a related entity, expose a `Map<Long, String>` from the ViewModel and use `updateXxx()` + `notifyItemRangeChanged` on the adapter — never collect a repository from a fragment.
- When validating dates: `endDate > startDate` for periods; `paymentDate <= now` for events; expired contracts must be filtered both in DAO queries (`endDate >= :now`) and in-memory getters.
- When adding a `btnSave`, always disable it before `viewModel.save()` and re-enable in conflict callbacks.
- For icon-only buttons (FAB, IconButton), set `android:contentDescription` with a dedicated string for accessibility.
