# IntelliMatch-X
## Automated Bi-Directional Internship Matching System

## Overview
IntelliMatch-X is a JavaFX desktop application for internship matching between candidates and recruiters.  
The current codebase includes:
- Candidate and recruiter login/signup portals
- Role-based dashboards
- A hidden admin analytics dashboard
- A weighted + exact matching engine with explainable results
- Local file-backed persistence in `data/*.tsv`

## Core Features
- **Bi-directional matching** across candidates and recruiter postings
- **Candidate skill-to-company search** with exact + similar skill matches
- **Recruiter candidate discovery** with filters (min score, background, availability, exact matches)
- **Shortlisting flow** for recruiter-selected candidates
- **Analytics dashboard** with:
  - signed-up users
  - active users
  - shortlisted candidates
  - top candidate skills
  - market demand skills
  - work mode demand
- **Embedded explainability UI** (WebView HTML/JS) in the legacy/main matching screen

## Admin Panel Unlock Sequence
The secret key to see the admin panel is:

**right click recruiter, right click applicant, right click recruiter, right click applicant**

> In code this sequence is `RARA` and only counts right-clicks on the landing page role buttons.

## Architecture Highlights
- **Strategy Pattern**: `MatchingStrategy` with `WeightedSkillGraphStrategy` and `ExactMatchStrategy`
- **Factory Pattern**: `PersonFactory` for applicant/recruiter construction
- **Observer Pattern**: `MatchEventBus` + `NotificationLogger`
- **Service Layer**:
  - `DatabaseService` for persistence, auth checks, analytics, and profile updates
  - `MatchingService` as facade over matching engine + observer integration
  - `AuthService` for candidate/recruiter login and signup operations
- **Session Handling**: `SessionManager` stores current signed-in user role context

## Persistence Model
The app stores state under `data/`:
- `users.tsv`
- `candidates.tsv`
- `recruiters.tsv`
- `user_stats.tsv`
- `shortlists.tsv`

On first run (or malformed/missing data), seed data is auto-created by `DatabaseService` + `DataSeedService`.

## Tech Stack
- Java 21
- JavaFX 21 (FXML-based UI)
- BootstrapFX
- Maven
- JUnit 5

## Build, Test, Run
From repository root:

```bash
mvn clean package
mvn test
mvn javafx:run
```

## Seed Login Notes
- Candidate example: `candidate1@maildemo.com`
- Recruiter example: `talent1@google.com`
- Default seed password: `Password@123`

## Key UI Entry Points
- Landing page: `/fxml/landing.fxml`
- Candidate login: `/fxml/applicant-login.fxml`
- Recruiter login: `/fxml/recruiter-login.fxml`
- Candidate dashboard: `/fxml/candidate-dashboard.fxml`
- Recruiter dashboard: `/fxml/recruiter-dashboard.fxml`
- Admin dashboard: `/fxml/admin-dashboard.fxml`

## License
MIT License
