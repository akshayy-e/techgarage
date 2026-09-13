# Changes in this update

## Bug fixes

### 1. Admin "Verify" button appeared to do nothing
`GET /api/admin/users` (and `/clients`, `/freelancers`) returned raw `User` entities, which have
no `verified` field — that lives on `FreelancerProfile`. Clicking **Verify** *did* update the
database, but the Users table had no way to reflect it, so every freelancer looked permanently
un-actioned no matter how many times an admin clicked Verify.

- **Backend:** added `dto/admin/AdminUserResponse.java`, which includes the freelancer's real
  `verified` flag. Updated `AdminService`, `AdminServiceImpl`, and `AdminController` accordingly.
- **Frontend:** `AdminUsers.jsx` now shows a "✓ Verified" / "Unverified" badge next to each
  freelancer's name, and disables the Verify button once a freelancer is verified.

### 2. Suspending a user broke their next login with a raw HTTP 500
Spring Security correctly refuses to authenticate a disabled (`enabled=false`) user by throwing a
`DisabledException` — but the app had no handler for it, so it fell through to the generic
`Exception` handler and returned "An unexpected error occurred: ..." instead of a clear message.

- **Backend:** `GlobalExceptionHandler` now handles `DisabledException` and returns
  `403 "This account has been suspended. Please contact TechGarage support."`

### 3. "AI/ML" category was unselectable on Post Problem
The backend `ProblemCategory` enum and the app's own label map (`format.js`) both already support
an `AI_ML` category (it's even advertised on the landing page), but the dropdown on the Post
Problem form never listed it.

- **Frontend:** `AI_ML` added to the `CATEGORIES` list in `PostProblem.jsx`.

## New: real AI integration

The app shipped with a placeholder `AIClassificationService` — pure keyword matching, explicitly
built (per the README) so a real LLM could be dropped in later. This update does that, using
Anthropic's Claude API, entirely optionally:

- **`AnthropicClient`** (`service/impl/AnthropicClient.java`) — a small wrapper around Claude's
  Messages API. If `ANTHROPIC_API_KEY` isn't set, or a call fails/times out/returns something
  unparsable, every caller falls back to the original rule-based logic. The app always works,
  with or without a key.
- **Real AI classification** — `AIClassificationServiceImpl` now tries Claude first (when
  configured) to classify a new problem's category/technology/priority, falling back to the
  keyword rules otherwise. Behavior for existing users with no API key is unchanged.
- **New: "✨ Analyze with AI" on Post Problem** — a client can now preview an AI-generated
  category, technology, priority, a fair USD budget range, a cleaned-up one-line summary, and a
  few clarifying questions freelancers will likely ask — before submitting the form. New endpoint:
  `POST /api/problems/ai-suggest` (client-only), backed by `AIAssistantService`.

### Setup
Copy `.env.example` and fill in (all optional):
```
ANTHROPIC_API_KEY=sk-ant-...
ANTHROPIC_MODEL=claude-haiku-4-5-20251001   # or claude-sonnet-5 for higher quality
AI_FEATURES_ENABLED=true
```
Leave `ANTHROPIC_API_KEY` blank and the app behaves exactly as before (rule-based only).

## Troubleshooting "cannot resolve import" errors
If your IDE shows red/unresolved imports right after opening this project, it's almost always one
of these two setup steps rather than a code problem — this project (including the original code,
before any of the changes above) uses Lombok-generated getters/setters/builders everywhere:

1. **Maven hasn't downloaded dependencies / indexed the project yet.** Run `mvn clean install`
   from `backend/`, or in your IDE: right-click the project → *Reload Maven Project* (IntelliJ) or
   *Update Project* (Eclipse) after a normal Maven import.
2. **The Lombok IDE plugin isn't installed / annotation processing isn't enabled.** Without it,
   your IDE can't see any Lombok-generated method (getters, setters, `.builder()`, etc.) and will
   show unresolved-symbol/import errors throughout the whole codebase, not just the new files.
   - IntelliJ: install the "Lombok" plugin, then enable *Settings → Build → Compiler → Annotation
     Processors → Enable annotation processing*.
   - Eclipse/STS: run `java -jar lombok.jar` against your Eclipse install (see projectlombok.org).

If you've done both and still see a specific error, please share the exact error text/filename —
that'll let us pinpoint it precisely rather than guessing further.

## Verification notes
- Frontend: `npm install && npm run build` completes cleanly with no errors.
- Backend: this sandbox has no access to Maven Central, so the Java changes could not be
  compiled here. Every new/edited file was reviewed by hand against the existing entity/DTO
  signatures for type correctness. Please run `mvn clean install` (or your IDE's build) as a
  first step after pulling this down.


## Profile + Notifications Enhancement
- Added authenticated `/api/users/me` GET/PUT endpoints for shared account details (name and phone).
- Added editable client profile with account details.
- Expanded freelancer profile with account details and skill CRUD-style controls (add, edit, delete skill tags) while preserving the existing comma-separated database field for deployment safety.
- Freelancer public profiles continue to expose professional profile information to clients.
- Added notification `Mark read`, `Mark all read`, and `Clear all` actions.
- Secured notification read updates so a user cannot modify another user's notification.
- Added authenticated notification deletion by current user.
- Made the navbar avatar open the appropriate stakeholder profile.


## Phase 2 — Freelancer marketplace

- Added searchable/filterable freelancer directory.
- Added verified and availability filters.
- Added persistent client-to-freelancer invitations with acceptance/decline states.
- Added invitation notifications.
- Added client “Find Mechanics” navigation.
- Added freelancer invitation cards/actions to the freelancer dashboard.
- Added database uniqueness protection for duplicate invitations.
