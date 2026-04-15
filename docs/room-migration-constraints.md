# Room Migration Constraints

## Goal

Avoid repeat startup crashes caused by Room schema changes landing without a matching database version change.

## Current Policy

- The app currently uses `fallbackToDestructiveMigration(dropAllTables = true)` for `MemoryDatabase`.
- This is acceptable for the current local-first runtime because the database stores rebuildable local state rather than user-synced canonical cloud data.
- Because of that choice, schema evolution is allowed to be destructive for now, but it must still be explicit.

## Required Rule

When any Room-managed schema changes, the `MemoryDatabase` version must be bumped in the same change.

Schema changes include:

- adding, removing, or renaming an entity field
- changing an entity type or nullability
- adding, removing, or renaming a table
- changing Room indices, primary keys, foreign keys, or converters in a way that affects stored shape
- changing embedded/value objects that alter the stored table layout

## Practical Checklist

Before merging any Room-related change:

1. Check whether any `@Entity`, `@Database`, or storage-affecting `@TypeConverter` changed.
2. If yes, bump `MemoryDatabase.version`.
3. Rebuild the app with `./gradlew :app:compileDebugKotlin`.
4. Verify packaging with `./gradlew :app:assembleDebug`.
5. If the change is user-visible or touches startup paths, do one local launch verification on a device or emulator that already has an older app install.

## Why Version Bump Is Still Required

Even with destructive migration enabled, Room only applies that fallback when it detects a version change.

If the schema changes but the version number does not, Room treats the on-device database as corrupted relative to the generated schema identity hash and throws an integrity exception on open.

## Future Upgrade Path

When the project starts storing non-rebuildable local state, this policy should tighten to:

- keep `exportSchema = true`
- add explicit `Migration` objects
- test upgrade paths between adjacent versions
- use destructive migration only for clearly disposable caches

Until then, the minimum safe rule is:

`schema change => version bump in the same patch`
