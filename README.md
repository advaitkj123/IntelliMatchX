# IntelliMatch-X
## Automated Bi-Directional Internship Matching System

---

## Overview

IntelliMatch-X is a desktop application built with **Java 17+** and **JavaFX** that implements a
symmetrical, bi-directional internship matching engine.  It matches applicants to recruiter postings
(and vice versa) using a **Weighted Skill-Graph algorithm**, provides symmetrical
**Match Justification** for both sides, and includes an embedded **HTML/JS Explainability Dashboard**.

---

## Architecture & Design Patterns

| Pattern | Where Used |
|---|---|
| **Strategy** | `MatchingStrategy` interface → `WeightedSkillGraphStrategy`, `ExactMatchStrategy` — swappable at runtime |
| **Factory** | `PersonFactory` — centralises creation of `Applicant`, `Recruiter`, and `Skill` objects |
| **Observer** | `MatchEventBus` (publisher) + `MatchObserver` (interface) + `NotificationLogger` (concrete subscriber) — real-time match notifications |

---

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 17 |
| UI Framework | JavaFX 21 (FXML) |
| UI Styling | BootstrapFX 0.4.0 |
| Embedded Dashboard | WebView (HTML5 / JavaScript / Canvas) |
| Build System | Apache Maven |

---

## Project Structure

```
IntelliMatchX/
├── pom.xml                                      ← Maven build file (JavaFX + BootstrapFX)
├── README.md
└── src/main/
    ├── java/
    │   ├── module-info.java                     ← Java 9+ module descriptor
    │   └── com/intellimatch/
    │       ├── App.java                         ← JavaFX Application entry point
    │       ├── model/
    │       │   ├── Skill.java
    │       │   ├── Person.java                  ← Abstract base entity
    │       │   ├── Applicant.java
    │       │   ├── Recruiter.java
    │       │   └── MatchResult.java             ← Bi-directional match output
    │       ├── factory/
    │       │   └── PersonFactory.java           ← Factory Pattern
    │       ├── observer/
    │       │   ├── MatchObserver.java           ← Observer interface
    │       │   ├── MatchEventBus.java           ← Singleton event bus
    │       │   └── NotificationLogger.java     ← Concrete observer
    │       ├── strategy/
    │       │   ├── MatchingStrategy.java        ← Strategy interface
    │       │   ├── WeightedSkillGraphStrategy.java  ← Primary algorithm
    │       │   └── ExactMatchStrategy.java      ← Alternate algorithm
    │       ├── engine/
    │       │   └── MatchingEngine.java          ← Core bi-directional engine
    │       ├── service/
    │       │   ├── MatchingService.java         ← Application service facade
    │       │   └── DataSeedService.java         ← Sample data provider
    │       └── ui/controller/
    │           └── MainController.java          ← FXML controller
    └── resources/
        ├── fxml/
        │   └── main.fxml                        ← FXML layout (3-tab UI)
        └── styles/
            └── main.css                         ← Dark-theme stylesheet
```

---

## Features

### 1. Bi-Directional Matching Engine
- **Full matrix mode** — evaluates every applicant against every recruiter posting and ranks all pairs by score.
- **Applicant mode** — given one applicant, ranks all available postings (recommendations).
- **Recruiter mode** — given one posting, ranks all applicants (talent discovery).

### 2. Weighted Skill-Graph Algorithm
- Each required skill has a configurable weight (0.0 – 1.0).
- Score = `Σ(matched skill weights) / Σ(all required skill weights)`.
- Bonus (+5%) applied when applicant availability ≥ 80% of internship duration.
- Falls back to `ExactMatchStrategy` (simple ratio) when selected.

### 3. Symmetrical Match Justification (Explainability)
Every `MatchResult` carries two independent natural-language justifications:
- **Applicant view** — "Why this opportunity is a match for you."
- **Recruiter view** — "Why this candidate fits your posting."

### 4. HTML/JS Explainability Dashboard (WebView)
Embedded inside a JavaFX `WebView`, a self-contained HTML5/JS page renders:
- Score ring with animated progress bar
- Pair details card (applicant, company, role, matched/missing counts)
- Colour-coded skill chips (green = matched, red = gap)
- Dynamic bar chart (Canvas API) — skill-graph visualisation
- Recommendation verdict with colour-coded icon

### 5. Real-Time Observer Notifications
After every match computation, the `MatchEventBus` broadcasts the event and the
`NotificationLogger` generates two log lines (one for each side) that appear live
in the **Real-Time Notifications** tab.

---

## Prerequisites

| Requirement | Version |
|---|---|
| Java JDK | 17 or higher |
| Apache Maven | 3.8+ |
| Internet connection (first build) | Maven downloads dependencies |

---

## Building & Running

### 1. Clone / Unzip the project
```bash
unzip IntelliMatchX_Project.zip
cd IntelliMatchX
```

### 2. Build with Maven
```bash
mvn clean package
```

### 3. Run the application (via JavaFX Maven Plugin)
```bash
mvn javafx:run
```

**Alternative — Run the shaded JAR** (Java 17+, JavaFX must be on module path):
```bash
java --module-path /path/to/javafx/lib \
     --add-modules javafx.controls,javafx.fxml,javafx.web \
     -jar target/intellimatch-x-1.0.0-shaded.jar
```

> Tip: The easiest way is `mvn javafx:run` — it handles the module path automatically.

---

## Usage Guide

1. **Launch the app** → the main window opens with 3 tabs.
2. **Select an Algorithm** from the top-right combo box (Weighted or Exact).
3. **Tab 1 — Bi-Directional Matching:**
   - Click **"Run Full Bi-Directional Match"** to evaluate all pairs.
   - Or select an applicant and click **"Get Recommendations"**.
   - Or select a recruiter and click **"Discover Candidates"**.
   - Click any row in the results table to see the symmetrical justifications below.
4. **Tab 2 — Explainability Dashboard:**
   - Select a result row in Tab 1 first; the WebView auto-updates with the visual explanation.
5. **Tab 3 — Real-Time Notifications:**
   - Populated automatically after each match run, showing one alert per side.

---

## Extending the System

### Add a new Matching Strategy
1. Implement `com.intellimatch.strategy.MatchingStrategy`.
2. Add it to `MatchingService.setCustomStrategy(yourStrategy)`.
3. Wire it into the UI combo box in `MainController`.

### Add a new Observer
1. Implement `com.intellimatch.observer.MatchObserver`.
2. Register it: `MatchEventBus.getInstance().subscribe(yourObserver)`.

### Add new Applicants / Recruiters
Edit `DataSeedService` or call `MatchingService.addApplicant()` / `addRecruiter()` programmatically.

---

## License

MIT License — free to use, modify, and distribute.
