# API Security Monitoring Dashboard Frontend

A modern, professional cybersecurity/SOC dashboard frontend built with **React**, **TypeScript**, **Vite**, **Tailwind CSS**, **Recharts**, and **STOMP WebSockets** to monitor protected Spring Boot microservices and AI-detected API threats in real time.

---

## 🎨 Theme & Aesthetic Identity

- **Minimal Blue & White Theme**: Crisp slate-50/white canvas, deep navy blue headers/cards (`#0f172a`, `#1e293b`), and sky/brand blue accents (`#0270c7`, `#3b82f6`).
- **High Information Density**: Built specifically for Security Operations Center (SOC) analysts to quickly inspect live API traffic, high-severity threat alerts, model confidence scores, and detection evidence.
- **Accessibility & Contrast**: Explicit severity markers (`CRITICAL`, `HIGH`, `MEDIUM`, `LOW`) with distinct background hues, border accents, and badges.

---

## 🚀 Key Features & Pages

1. **Security Dashboard (`/`)**:
   - Top-level KPI cards consuming `/api/v1/dashboard/summary`.
   - Real-Time Live Threat Feed (`/topic/security/threats`) with instant toast alerts for high/critical incidents.
   - Real-Time API Traffic Stream (`/topic/security/events`).
   - Threat Type Distribution (Donut Chart) & Severity Breakdown (Bar Chart).

2. **Applications Management (`/applications` & `/applications/:id`)**:
   - Register new Spring Boot applications and securely copy one-time generated API Keys.
   - Detailed application view with app-specific live traffic feed (`/topic/security/applications/{id}`).

3. **API Traffic Monitoring (`/traffic`)**:
   - Historical & live API traffic table supporting filters by Application ID, HTTP Method, and Endpoint.
   - Server-side pagination.

4. **Threat Incident Management (`/threats` & `/threats/:id`)**:
   - Filterable, paginated threat table.
   - Deep-dive investigation view with **Risk vs Confidence Gauges**, AI Detection Evidence (Reason Codes), and Recommended Mitigation Actions.
   - Real-time status update workflows (`OPEN`, `ACKNOWLEDGED`, `RESOLVED`) targeting `PATCH /api/v1/threats/{id}/status`.

---

## 🛠️ Development & Running Locally

### 1. Prerequisites
- Node.js 18+ and `npm`.
- `api-security-platform` backend running on `http://localhost:8080`.

### 2. Installation
```bash
cd api-security-dashboard
npm install
```

### 3. Start Development Server
```bash
npm run dev
```
The application will start at `http://localhost:3000` (proxying `/api` and `/ws` to `localhost:8080`).

### 4. Build for Production
```bash
npm run build
```
Creates an optimized production build in `dist/`.

---

## 🧪 Testing
```bash
npx vitest run
```
