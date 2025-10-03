# PatchSync ⚡ – Automated Deployment Manager

[![Java](https://img.shields.io/badge/Java-17-blue)](https://www.oracle.com/java/technologies/javase-jdk17-downloads.html)  
[![Gradle](https://img.shields.io/badge/Gradle-8.3-green)](https://gradle.org/)  

A modular deployment and synchronization system for automating artifact updates across multiple services that are running on the same machine, with snapshot-based rollback and a plugin API for custom integration.

---

## ✨ Features

- 🔄 **Automatic Update Handling** — listens to repository changes and updates artifacts.  
- 💾 **Snapshots & Rollback** — create timestamped backups before each deployment.
- 🔌 **Plugin System** — developers can extend functionality to support different services or environments.
- 🔐 **Secure Repository Access** — supports private repositories via credentials.
- 🧩 **Modular Design** — separated into api, core, and PluginExample modules for clean architecture.

---

## 📦 Project Structure

The project is modularized into three submodules:

```
PatchSync/
├── api/           # API definitions for plugins
├── core/          # Core system (deployment, backup, repo manager, config)
├── PluginExample/ # Sample plugin demonstrating integration with PatchSync
└── README.md
```

## 🚀 How It Works

### 1. Repository Monitoring
- PatchSync fetches and pulls repositories when new commits are pushed.
- Artifacts are resolved via a ``repo-config.json`` descriptor inside each repository.

### 2. Deployment Flow
- On update, a snapshot is created (timestamped backup).
- Artifacts are deployed to their configured destinations.
- External systems can be notified (via plugins).

### 3. Rollback Flow
- Snapshots allow rolling back artifacts or entire repositories.
- Restored artifacts are re-deployed safely. 

---

## ⚙️ Installation

1. **Clone the repository:**

```bash
git clone https://github.com/rolandbartha/PatchSync.git
cd patchsync
```

2. **Configure `config.json`** in the project root (example):

```json
{
  "artifactRepos": {
    "MainRepo": {
      "repo": "github.com/username/MainRepo",
      "branch": "main",
      "credentials": { "username": "GITHUB_USER", "token": "GITHUB_TOKEN" }
    },
    "SecondaryRepo": {
      "repo": "github.com/username/SecondaryRepo",
      "branch": "main"
    }
  },
  "targets": {
    "serviceA": {
      "path": "absolute/path/to/serviceA",
      "plugins": ["Core", "Notifier"]
    },
    "serviceB": {
      "path": "absolute/path/to/serviceB",
      "plugins": ["Core", "Commons"]
    }
  },
  "snapshot": {
    "keep_days": 30,
    "max_backups": 20
  },
  "githubConfig": {
	"port": 8080,
    "endpoint": "github-update",
	"token": "GITHUB_NOTIFICATION_TOKEN"
  }
}
```

> The app will generate a copy of this JSON in its folder if it does not exist.

3. **Configure `repo-config.json`** in the repository root (example):

**MainRepo** contains sub-modules ``Core`` and ``Commons``:
```json
{
  "artifacts": [
    {
      "name": "Core",
      "path": "Core/core.jar"
    },
    {
      "name": "Commons",
      "path": "Commons/commons.jar"
    }
  ]
}
```
**SecondaryRepo** contains sub-modules ``Notifier`` and ``Storage``:
```json
{
  "artifacts": [
    {
      "name": "Notifier",
      "path": "Notifier/service.jar"
    },
    {
      "name": "Storage",
      "path": "Storage/storage.jar"
    },
  ]
}
```

4. **Build the project using Gradle:**

```bash
./gradlew clean build
```

5. **Run the application:**

```bash
java -jar build/libs/patchsync-1.0-SNAPSHOT.jar
```

---

## ✈️ Usage

PatchSync supports **CLI commands** for manual operations:

```text
# List all snapshots
rollback list

# Rollback a full snapshot
rollback <snapshotId>

# Force update check for all repos
check

# Exit the application
exit
```

Updates can also be triggered automatically via GitHub webhook notifications.

---

## 💎 GitHub Workflow Integration

PatchSync can automatically update plugins after a GitHub push or release using a workflow:

```yaml
name: Sync

on:
  push:
    branches:
      - main
  release:
    types: [published]

jobs:
  notify:
    runs-on: ubuntu-latest
    steps:
      - name: Send HTTP notification to PatchSync
        uses: fjogeleit/http-request-action@v1
        with:
          url: ${{ secrets.PATCHSYNC_URL }}
          method: POST
          contentType: 'application/json'
          data: |
            {
              "repoName": "${{ secrets.REPOSITORY_NAME }}",
              "commit": "${{ github.sha }}",
              "actor": "${{ github.actor }}",
              "timestamp": "${{ github.event.head_commit.timestamp }}"
            }
          customHeaders: '{"X-Auth-Token":"${{ secrets.PATCHSYNC_TOKEN }}"}'
```

- `REPOSITORY_NAME` → the name of the repository
- `PATCHSYNC_URL` → the app’s HTTP endpoint (e.g., `http://<IP>:8080/github-update`)  
- `PATCHSYNC_TOKEN` → secret token to validate webhook requests  

---

## 📚 Backup & Rollback

PatchSync keeps **timestamped snapshots** of every update:

```
snapshot/
└── 20251001_153045/
    ├── Commons.jar
    ├── Core.jar
    ├── Notifier.jar
    └── Storage.jar
```
Upon rollback files are restored safely.  

---

## 🔌 Plugin Development

PatchSync is **plugin-driven**: instead of hardcoding deployment behaviors (e.g., restarting Docker containers, reloading configs, or notifying Slack), you can build plugins that hook into update and rollback events.

## 📂 Plugin Structure

A plugin is simply a **separate module or JAR** with:
- A ``plugin.json`` file defining metadata.
- An implementation of the PatchSync Plugin API.
- Optional logging using the plugin logger.

Example ``plugin.json``:
```json
name: ExamplePlugin
version: 1.0
author: YourName
mainClass: com.example.ExamplePlugin
```

## 🛠 Plugin API

Each plugin extends the SyncPlugin abstract class from the api module:
```java
public class ExamplePlugin extends SyncPlugin {

    @Override
    public String getName() {
        return "ExamplePlugin";
    }

    @Override
    public void onLoad() {
        log("called onLoad()"); // Called when the plugin is being loaded
    }

    @Override
    public void onUnload() {
        log("called onUnload()"); // Called when the plugin is being unloaded
    }

    @Override
    public void onEvent(Event event) { // Triggered upon Deploy or Rollback
        if (event instanceof DeployUpdateEvent e) {
            log("called onEvent() for DeployEvent with " + e.getArtifacts().size() +
                    " artifacts and reason: " + e.getSnapshot().getReason());
        } else if (event instanceof RollbackUpdateEvent e) {
            log("called onEvent() for RollbackEvent id: " + e.getSnapshotId() + " with " +
                    e.getArtifacts().size() + " artifacts and reason: " + e.getReason());
        }
    }
}
```
## 🔧 Plugin Lifecycle

- Plugins are loaded at runtime.
- Events are dispatched through the core system.
- Developers can extend PatchSync to fit any environment:
  - Restart microservices
  - Reload Kubernetes deployments
  - Send Slack/Discord notifications
  - Update configuration files

## 🔒 Security

- Supports **private repositories** via GitHub username & token (`credentials` in `config.json`).  
- Webhook endpoint validates a **secret token** to allow only authorized GitHub repository updates.  
- Backup management keeps only a configurable number of snapshots and auto-purges old ones.  

---

## 💻 Tech Stack

- **Java 17**  
- **Gradle 8**  
- **Lombok** for boilerplate reduction  
- **Jackson** for JSON parsing  
- **SLF4J + Logback** for logging  
- **JGit** for Git operations  
- **Java HTTPServer** for webhook endpoint  

---

## 📜 License

Apache 2.0 License © Roland-Mark Bartha

---