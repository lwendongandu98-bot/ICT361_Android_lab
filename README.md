# ICT361 Android Lab Project

Welcome to the central repository for the ICT361 project. This monorepo contains both our Android application and our backend infrastructure.

## Repository Structure
* **Frontend/**: Android Studio project files.
* **Backend-server/**: Express REST API and database configurations.
* **CONTRIBUTION_LOG.md**: Mandatory audit log for all team members to record their tasks.

## Getting Started
1. Clone the repository: `git clone https://github.com/lwendongandu98-bot/ICT361_Android_lab.git`
2. Create your feature branch: `git checkout -b feature/your-name-task`
## Run the Android application

1. Open `Frontend/` in Android Studio (open the folder containing `settings.gradle.kts`).
2. Let Gradle sync, then run the `app` configuration on an emulator or Android device.
3. The default API URL in `Frontend/app/src/main/java/com/example/studentlabgroupmanagement/data/remote/RetrofitClient.java` is `http://10.0.2.2:3000/`, which is for the Android Emulator. For a physical phone, change it to the development computer's LAN IPv4 address (for example `http://192.168.1.20:3000/`); put both devices on the same Wi-Fi and allow port 3000 through the computer's firewall.

## Run the backend

1. Install Node.js and MySQL.
2. In MySQL, run `Backend-server/database/schema.sql`.
3. In `Backend-server/`, copy `.env.example` to `.env` and set `DB_PASSWORD` to your MySQL password. Keep `DB_NAME=student_lab_db` unless you also change the schema.
4. Run `npm install`, then `npm start`. The health check is `http://localhost:3000/`.

Note: Android build and live MySQL integration must be tested in an environment with the Android SDK/Gradle distribution, Node dependencies, and a configured MySQL server. This workspace cannot reach the Gradle distribution host, so a fresh Android build could not be confirmed here.
