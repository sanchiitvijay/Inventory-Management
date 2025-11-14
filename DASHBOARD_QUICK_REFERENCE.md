# Dashboard Quick Reference

## 🎯 What Was Built

A **server-side rendered Thymeleaf dashboard** at `GET /dashboard` in **order-service**.

---

## 📋 Dashboard Sections

### 1️⃣ Orders by Status
```
Data: Total orders grouped by status
Source: Order Service Database
Display: Table with color-coded badges
```

**Example:**
- CREATED: 45 orders (blue badge)
- PAID: 120 orders (green badge)
- FULFILLED: 98 orders (purple badge)
- CANCELLED: 12 orders (red badge)

---

### 2️⃣ Top 10 Products by Orders
```
Data: Most ordered products with quantities
Source: Order Service Database (order_items)
Display: Ranked table with metrics
```

**Columns:**
- Rank (#)
- Product SKU
- Number of Orders (distinct orders containing this product)
- Total Quantity Sold (sum across all orders)

---

### 3️⃣ Low Stock Items ⚠️
```
Data: Products below threshold
Source: Inventory Service API
Endpoint: http://inventory-service/api/inventory/low-stock
Display: Alert table with timestamps
```

**Columns:**
- Product SKU
- Available quantity (red highlight)
- Threshold level
- Last updated timestamp

---

## 🗂️ Files Created

```
order-service/
├── pom.xml                                     [UPDATED - added Thymeleaf]
├── src/main/java/com/microservices/order/
│   ├── controller/
│   │   └── DashboardController.java           [NEW]
│   ├── dto/
│   │   ├── DashboardData.java                 [NEW]
│   │   ├── OrderStatusCount.java              [NEW]
│   │   ├── ProductOrderCount.java             [NEW]
│   │   └── LowStockItem.java                  [NEW]
│   ├── repository/
│   │   └── OrderRepository.java               [UPDATED - added 2 queries]
│   └── service/
│       └── DashboardService.java              [NEW]
└── src/main/resources/templates/
    └── dashboard.html                          [NEW]
```

---

## 🔧 Technical Stack

| Component | Technology |
|-----------|------------|
| **View Engine** | Thymeleaf 3.x |
| **Styling** | Embedded CSS (no frameworks) |
| **Data Queries** | Spring Data JPA (JPQL) |
| **Service Client** | WebClient (reactive) |
| **Service Discovery** | Eureka (service names) |
| **Retry Logic** | Reactor Retry (2 attempts, 300ms) |

---

## 🚀 Quick Start

### 1. Access Dashboard
```bash
# Default order-service port
http://localhost:8082/dashboard

# Or your configured port
http://localhost:<YOUR_PORT>/dashboard
```

### 2. Prerequisites
- ✅ Order Service running
- ✅ Eureka Server running
- ✅ Inventory Service running (for low-stock data)
- ✅ Database with order data (for meaningful display)

### 3. Test with Sample Data
```bash
# Create a sample order via API
curl -X POST http://localhost:8082/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productSku": "PROD-001", "quantity": 3, "price": 29.99}
    ]
  }'

# Refresh dashboard to see it appear
```

---

## 💡 Key Features

### ✅ Server-Side Rendered
- No JavaScript required
- Fast initial page load
- SEO-friendly (if needed)
- Works without JS enabled

### ✅ Responsive Design
- Mobile-friendly tables
- Breakpoints at 768px
- Scales from phone to desktop

### ✅ Graceful Error Handling
- Service failures don't crash page
- Empty states for no data
- Retry logic for network issues
- Comprehensive logging

### ✅ Real-Time Data
- Fresh data on every page load
- Live inventory service integration
- No stale cached data

### ✅ Clean UI
- Minimal, professional design
- Color-coded status indicators
- Clear visual hierarchy
- Accessibility-friendly

---

## 📊 Data Flow

```
User Request
    ↓
GET /dashboard
    ↓
DashboardController
    ↓
DashboardService
    ├─→ OrderRepository.countOrdersByStatus()
    │   └─→ [DB Query] → OrderStatusCount[]
    │
    ├─→ OrderRepository.findTopProductsByOrderCount()
    │   └─→ [DB Query] → ProductOrderCount[]
    │
    └─→ WebClient.get("/api/inventory/low-stock")
        └─→ [HTTP] → inventory-service
            └─→ LowStockItem[]
    ↓
DashboardData (aggregated)
    ↓
dashboard.html (Thymeleaf)
    ↓
Rendered HTML Response
```

---

## 🎨 UI Preview (ASCII)

```
┌────────────────────────────────────────────────────┐
│  📊 Order Service Dashboard                        │
├────────────────────────────────────────────────────┤
│                                                    │
│  Orders by Status                                  │
│  ┌──────────────────────────────────────────┐    │
│  │ Status    │ Count                        │    │
│  ├──────────────────────────────────────────┤    │
│  │ CREATED   │ 45                           │    │
│  │ PAID      │ 120                          │    │
│  │ FULFILLED │ 98                           │    │
│  │ CANCELLED │ 12                           │    │
│  └──────────────────────────────────────────┘    │
│                                                    │
│  Top 10 Products by Orders                        │
│  ┌──────────────────────────────────────────┐    │
│  │ # │ SKU      │ Orders │ Qty             │    │
│  ├──────────────────────────────────────────┤    │
│  │ 1 │ PROD-001 │ 45     │ 230             │    │
│  │ 2 │ PROD-002 │ 38     │ 156             │    │
│  │ 3 │ PROD-003 │ 32     │ 89              │    │
│  └──────────────────────────────────────────┘    │
│                                                    │
│  ⚠️ Low Stock Items                                │
│  ┌──────────────────────────────────────────┐    │
│  │ SKU      │ Avail │ Thresh │ Updated     │    │
│  ├──────────────────────────────────────────┤    │
│  │ PROD-005 │ 3     │ 10     │ 2025-11-15  │    │
│  │ PROD-012 │ 5     │ 15     │ 2025-11-15  │    │
│  └──────────────────────────────────────────┘    │
│                                                    │
│          Last updated: 2025-11-15 14:32:10        │
└────────────────────────────────────────────────────┘
```

---

## 🔍 Customization Examples

### Change Top Products Limit
**File:** `DashboardService.java`
```java
// Change from 10 to 20
return results.stream()
    .limit(20)  // ← Change here
    .map(...)
```

### Adjust Colors
**File:** `dashboard.html`
```css
/* Change primary color from blue to green */
th {
    background-color: #2ecc71; /* was #3498db */
}
```

### Add Auto-Refresh
**File:** `dashboard.html` (add to `<head>`)
```html
<meta http-equiv="refresh" content="30">
<!-- Refresh every 30 seconds -->
```

### Cache Dashboard Data
**File:** `DashboardService.java`
```java
@Cacheable(value = "dashboard", ttl = 60)
public DashboardData getDashboardData() {
    // Cached for 60 seconds
}
```

---

## 🐛 Common Issues & Solutions

| Issue | Solution |
|-------|----------|
| **Dashboard returns 404** | Check controller mapping, ensure service started |
| **Empty tables** | Add test data to database, check repository queries |
| **Low stock section empty** | Start inventory-service, check Eureka registration |
| **Template errors** | Verify `dashboard.html` in `src/main/resources/templates/` |
| **Styling broken** | Clear browser cache, check CSS syntax in template |

---

## 📦 Dependencies Required

**Added to `pom.xml`:**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-thymeleaf</artifactId>
</dependency>
```

**Already Present:**
- spring-boot-starter-web
- spring-boot-starter-data-jpa
- spring-boot-starter-webflux (for WebClient)
- spring-cloud-starter-loadbalancer

---

## ✅ What's Included

| Feature | Status |
|---------|--------|
| Server-side rendering | ✅ |
| Responsive design | ✅ |
| Orders by status | ✅ |
| Top 10 products | ✅ |
| Low stock alerts | ✅ |
| Error handling | ✅ |
| Retry logic | ✅ |
| Empty states | ✅ |
| Color-coded badges | ✅ |
| Mobile-friendly | ✅ |
| No JS frameworks | ✅ |
| Minimal CSS | ✅ |

---

## 🎯 Next Steps (Optional Enhancements)

1. **Add Authentication** - Protect with Spring Security
2. **Add Caching** - Cache dashboard data for 30-60 seconds
3. **Add Filters** - Date range filters for orders
4. **Add Charts** - Use Chart.js for visual graphs
5. **Add Export** - Export data to CSV/Excel
6. **Add Pagination** - For very large product lists
7. **Add Search** - Filter products by SKU
8. **Add Real-time Updates** - WebSocket for live updates

---

## 📝 Summary

**Endpoint:** `GET /dashboard`  
**Technology:** Thymeleaf + Embedded CSS  
**Data Sources:** Order DB (2 queries) + Inventory Service (1 API call)  
**Features:** 3 sections with tables, color coding, responsive design  
**Error Handling:** Graceful fallbacks, retry logic, logging  
**No External Dependencies:** Pure Spring Boot + Thymeleaf  

**The dashboard is production-ready and fully functional! 🚀**
