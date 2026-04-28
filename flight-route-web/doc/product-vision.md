# Introduction

Aim of the project is to implement the front-end (web) experience of the Flight Route platform for the aviation industry. The web application is a responsive Single Page Application (SPA) that allows agencies to discover all valid travel routes between two locations on a selected date, and allows admins to maintain the underlying location and transportation catalog that powers route discovery.

The front-end consumes the Spring Boot REST API exposed by `flight-route-api` and presents its capabilities through a role-aware UI.

The requirements are the followings:

- A responsive SPA must be implemented in **React** (with TypeScript).
- The application must have a top **header** and a left **side bar menu**.
- The application must integrate with the existing REST API for authentication, location, transportation and route endpoints.
- The application must enforce role-based access on the client side (Admin vs. Agency) in addition to the server-side checks.
- All endpoint calls must include the JWT/Authorization header obtained from the login flow.
- The UI must remain usable on desktop, tablet and mobile breakpoints (responsive layout).

# Navigation Requirements

The side bar must contain 3 navigation entries:

## Locations

- Visible **only for Admins**.
- Provides CRUD operations on the location catalog (name, country, city, location code).
- Hidden / not rendered for Agency users.

## Transportations

- Visible **only for Admins**.
- Provides CRUD operations on transportation entries (origin, destination, type, operating days).
- Hidden / not rendered for Agency users.

## Routes

- Visible for **all authenticated user types** (Admin and Agency).
- Default landing page after login for Agency users.
- Provides the route search experience described below.

# Page Requirements

## Login Page

- Public page (no header, no sidebar) where the user enters username and password.
- On success, the access token is stored client-side and the user is redirected to the appropriate landing page based on role.
- On failure, a generic error message is shown without disclosing whether the username or the password was wrong.

## Locations Page (Admin only)

- Lists existing locations in a paginated/searchable table.
- Allows the admin to:
  - Create a new location through a form (Name, Country, City, Location Code).
  - View a single location's details.
  - Update an existing location.
  - Delete a location.
- Surfaces validation errors coming from the API (e.g. duplicate location code, missing required fields).
- Disallows deletion when the API rejects it because the location is referenced by an active transportation.

## Transportations Page (Admin only)

- Lists existing transportations in a table that shows origin, destination, transportation type and operating days in a human-readable form.
- Allows the admin to:
  - Create a new transportation by selecting origin and destination from the existing locations, choosing a transportation type (FLIGHT, BUS, SUBWAY, UBER) and selecting one or more operating days (1–7).
  - Update an existing transportation.
  - Delete a transportation.
- Validates client-side that origin and destination are not the same and that at least one operating day is selected, before sending the request.

## Routes Page (All authenticated users)

This is the central user-facing page of the application.

- The user selects:
  - **Origin** location from a dropdown / searchable select.
  - **Destination** location from a dropdown / searchable select.
  - **Trip date** from a date picker.
- A **Search** button triggers the route listing call.
- The result panel shows a list of all valid routes returned by the API.
  - Each route summary highlights the flight leg (e.g. "Via İstanbul Airport (IST)").
  - Route shapes supported on the UI follow the API definition: `FLIGHT`, `before ➡ FLIGHT`, `FLIGHT ➡ after`, `before ➡ FLIGHT ➡ after`.
- Clicking a route opens a **side panel** (or equivalent overlay) showing the full leg-by-leg detail of the route:
  - Each leg displays the from/to locations and the transportation type.
  - The flight leg is visually distinguished from before/after transfer legs.
- An empty state is shown when the API returns no valid routes for the given selection.

## Route Details on a Map (Nice-to-Have)

- When the route detail panel is open, the legs of the selected route can additionally be visualized on a map (lines/arrows between locations).
- This is optional and follows the same design freedom mentioned in the case study.

# Authentication & Authorization Requirements

- The front-end must implement a login flow that uses the API's authentication endpoint and stores the resulting token securely on the client.
- Every request to a protected endpoint must include the `Authorization` header.
- The UI must react to API authorization responses:
  - **HTTP 401** → token is invalid / expired → user is redirected to the login page.
  - **HTTP 403** → user is shown a "not authorized" message and is not allowed to navigate to the restricted page.
- The sidebar and routing layer must hide Locations and Transportations links for Agency users; deep-linking to those routes for an Agency user must redirect them to the Routes page (or render a not-authorized state).
- Admins can access all pages.

# Non-Functional Requirements

- **Framework:** React + TypeScript SPA, built with Vite.
- **Responsiveness:** Layout must adapt to common breakpoints (mobile, tablet, desktop). The sidebar collapses to a drawer on small screens.
- **Internationalization:** The codebase already includes an i18n layer (`src/locales`); user-facing strings must go through it rather than being hard-coded.
- **State management:** API state is stored in the existing client store; route search results may be cached client-side per (origin, destination, date) tuple to avoid unnecessary re-fetches.
- **Error handling:** All API error responses surface a user-friendly message; raw stack traces or backend error payloads are not shown to the user.
- **Form validation:** Forms perform client-side validation that mirrors the API's constraints (required fields, format of the location code, valid operating days, etc.).
- **Accessibility:** Forms and interactive controls are keyboard-navigable and use semantic markup.
- **Build & Deploy:** The project is buildable to a static bundle and deployable as a static site (Vercel configuration is already present).
