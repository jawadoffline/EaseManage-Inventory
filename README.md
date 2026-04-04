# EaseManage Inventory

A modern, enterprise-grade inventory management system built with Spring Boot and React.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.5, Java 25, Spring Security, Spring Data JPA |
| Frontend | React 18, TypeScript, Vite, Ant Design, Recharts |
| Database | PostgreSQL (production), H2 (development) |
| Auth | JWT (access + refresh tokens), BCrypt passwords |
| PDF | iText 8 |
| Testing | JUnit 5, Mockito, Vitest, Testing Library |
| DevOps | Docker, GitHub Actions CI/CD |

## Features

### Core Modules
- **Dashboard** - KPI cards, real-time charts, low stock alerts
- **Products** - Full CRUD, SKU auto-generation, image upload, barcode labels, CSV import/export
- **Categories** - Hierarchical (parent/child) category tree
- **Warehouses** - Multi-location inventory tracking with capacity management
- **Inventory** - Stock levels per warehouse, adjustments, low-stock notifications
- **Suppliers** - Supplier management with product linking
- **Customers** - Customer database with contact management
- **Purchase Orders** - Draft > Pending > Approved > Received workflow
- **Sales Orders** - Pending > Processing > Shipped > Delivered workflow
- **Stock Movements** - Transfer and adjustment history tracking

### Intelligence & Operations
- **Reports** - Inventory valuation, stock summary, order summary with CSV export
- **PDF Generation** - Downloadable PDF invoices for purchase and sales orders
- **Audit Trail** - Full activity log (who/what/when) for all entities
- **Notifications** - In-app alerts for low stock and order status changes
- **Global Search** - Cross-entity search (products, suppliers, orders)

### Security & Auth
- **JWT Authentication** - Access tokens (15min) + refresh tokens (7 days)
- **Role-Based Access** - 4 roles: Admin, Manager, Warehouse Staff, Viewer
- **Password Reset** - Token-based forgot password flow
- **Rate Limiting** - 20 requests/min on auth endpoints
- **Soft Deletes** - Products, suppliers, warehouses use logical deletion
- **Optimistic Locking** - Concurrent edit conflict detection

### UI/UX
- **Dark Mode** - Toggle with persistent preference
- **Responsive Layout** - Mobile drawer sidebar, responsive tables
- **Loading Skeletons** - Smooth loading states
- **Professional Design** - Custom SVG branding, polished icons

### Production Ready
- **83 Tests** - 70 backend (JUnit/Mockito) + 13 frontend (Vitest)
- **Spring Actuator** - Health checks, metrics, app info
- **Structured Logging** - JSON logs in production
- **Docker Compose** - Full stack (PostgreSQL + API + Frontend)
- **CI/CD Pipeline** - GitHub Actions for build, test, deploy
- **Flyway Migrations** - Version-controlled database schema
- **Spring Cache** - In-memory caching for frequent lookups
- **Email Service** - Async SMTP notifications (configurable)
- **Swagger/OpenAPI** - Interactive API documentation
- **Bulk Import** - CSV import for products, suppliers, customers

## Quick Start

### Prerequisites
- Java 21+ (Java 25 recommended)
- Node.js 20+
- Maven 3.9+

### Development Setup

```bash
# Clone the repository
git clone https://github.com/your-username/EaseManageInventory.git
cd EaseManageInventory

# Start the backend (uses H2 in-memory database)
cd backend
mvn spring-boot:run

# Start the frontend (in a new terminal)
cd frontend
npm install
npm run dev
```

### Access the App

| Service | URL |
|---------|-----|
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080 |
| Swagger Docs | http://localhost:8080/swagger-ui.html |
| Health Check | http://localhost:8080/actuator/health |

### Default Login

| Username | Password | Role |
|----------|----------|------|
| admin | admin123 | Admin |

## Docker Deployment

```bash
# Production deployment with PostgreSQL
docker compose up -d

# Access at http://localhost (frontend) and http://localhost:8080 (API)
```

## Environment Variables

Copy `backend/.env.example` and configure:

```env
DB_URL=jdbc:postgresql://localhost:5432/easemanage_inventory
DB_USERNAME=postgres
DB_PASSWORD=changeme
JWT_SECRET=your-64-char-secret-key
MAIL_ENABLED=false
MAIL_HOST=smtp.gmail.com
```

## Running Tests

```bash
# Backend tests (70 tests)
cd backend && mvn test

# Frontend tests (13 tests)
cd frontend && npm test
```

## Project Structure

```
EaseManageInventory/
├── backend/                    # Spring Boot API (126 Java files)
│   ├── src/main/java/com/easemanage/
│   │   ├── auth/              # JWT authentication & password reset
│   │   ├── user/              # User management
│   │   ├── product/           # Products + image upload + CSV export
│   │   ├── category/          # Hierarchical categories
│   │   ├── warehouse/         # Warehouse management
│   │   ├── inventory/         # Stock tracking
│   │   ├── supplier/          # Suppliers + supplier-products
│   │   ├── customer/          # Customer management
│   │   ├── order/             # Purchase & sales orders + PDF
│   │   ├── stockmovement/     # Stock movement history
│   │   ├── notification/      # Notifications + email
│   │   ├── audit/             # Audit trail
│   │   ├── report/            # Reports & analytics
│   │   ├── config/            # Security, CORS, cache, rate limit
│   │   └── common/            # Shared DTOs, exceptions, search
│   └── src/test/              # 15 test files, 70 tests
├── frontend/                   # React + TypeScript (52 files)
│   ├── src/
│   │   ├── pages/             # 16 page components
│   │   ├── components/        # 6 reusable components
│   │   ├── api/               # 15 API clients
│   │   ├── store/             # Zustand stores (auth, theme)
│   │   ├── auth/              # Route guards
│   │   ├── layouts/           # App layout with sidebar
│   │   └── utils/             # CSV export utility
│   └── src/test/              # 4 test files, 13 tests
├── docker-compose.yml          # Full stack deployment
├── .github/workflows/ci.yml   # CI/CD pipeline
└── PLAN.md                    # Architecture & design document
```

## API Overview

| Module | Endpoints |
|--------|-----------|
| Auth | POST /auth/login, /register, /refresh, /logout, /forgot-password, /reset-password |
| Users | GET/POST/PUT/DELETE /users, GET /users/me, PUT /users/me, POST /users/me/change-password |
| Products | GET/POST/PUT/DELETE /products, POST /products/{id}/image, GET /products/export/csv |
| Categories | GET/POST/PUT/DELETE /categories, GET /categories/tree, /categories/all |
| Warehouses | GET/POST/PUT/DELETE /warehouses, GET /warehouses/active |
| Inventory | GET /inventory, GET /inventory/low-stock, POST /inventory/adjust |
| Suppliers | GET/POST/PUT/DELETE /suppliers, GET/POST/DELETE /suppliers/{id}/products |
| Customers | GET/POST/PUT/DELETE /customers |
| Purchase Orders | GET/POST/DELETE /purchase-orders, PATCH /{id}/status, GET /{id}/pdf |
| Sales Orders | GET/POST/DELETE /sales-orders, PATCH /{id}/status, GET /{id}/pdf |
| Stock Movements | GET/POST /stock-movements |
| Reports | GET /reports/inventory-valuation, /stock-summary, /order-summary |
| Dashboard | GET /dashboard/stats, /dashboard/charts |
| Audit | GET /audit |
| Notifications | GET /notifications, GET /unread-count, PATCH /{id}/read, POST /mark-all-read |
| Search | GET /search?q= |
| Import | POST /import/products, /import/suppliers, /import/customers |
| Health | GET /actuator/health, /actuator/info, /actuator/metrics |

## License

MIT
