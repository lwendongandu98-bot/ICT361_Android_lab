# Repair notes and verification status

## Changes made
- Fixed the Gradle project structure so the Android application plugin is applied to `:app`, not to both the root project and the app module.
- Consolidated Android dependencies and retained dependencies used by the existing Java/XML source (Retrofit, Gson, Room, WorkManager, lifecycle, Material, CardView, and CircleImageView).
- Changed the default Retrofit host to `10.0.2.2`, the Android Emulator's route to the host computer. For a physical phone, replace this with the computer's LAN IPv4 address in `Frontend/app/src/main/java/com/example/studentlabgroupmanagement/data/remote/RetrofitClient.java`.
- Made the backend port configurable with `PORT` while keeping the default at port 3000, matching the Android client's default.
- Aligned `.env.example` with the schema's default database name (`student_lab_db`) and port.
- Adjusted the lab-group creation response to return database field names expected by the Android entity.
- Added backend lecturer profile read/update endpoints to match the API paths declared by the Android client.
- Repaired the queued offline-sync worker so student registration/group operations are actually sent through Retrofit instead of remaining permanently queued; `SyncHelper` now schedules both record sync and submission sync.
- Resolved lab-group names to MySQL foreign-key IDs when student records sync.
- Updated the lecturer profile repository to call the lecturer-profile endpoint rather than accidentally creating a student record.
- Allowed the backend login controller to validate both raw-password bcrypt hashes and the SHA-256 password payload format used by the offline registration queue.
- Added setup instructions for Android Emulator vs. physical-device networking and backend configuration.

## Checks completed
- All backend JavaScript files passed `node --check`.
- All Android XML files parsed successfully.
- Manifest activity names were checked against Java source files.
- XML layout references and Java `R.id` references were checked for missing resources.

## Important verification limitation
A fresh Android build could not be run in this environment because the Gradle wrapper distribution could not be downloaded (`services.gradle.org` was unreachable). A live MySQL server and Android runtime/device are also unavailable here. Therefore this package contains source/configuration repairs and static checks, but it is **not honest to claim every screen and server interaction has been runtime-tested**. Please open `Frontend/` in Android Studio, allow Gradle sync, then run the app and backend using the README instructions.
