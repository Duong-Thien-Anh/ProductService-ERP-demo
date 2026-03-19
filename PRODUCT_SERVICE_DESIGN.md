# Product Service - Detailed Design Document

## Service Overview

**Service Name:** Product Service  
**Port:** 8083  
**Purpose:** Product Catalog Management, Inventory Tracking, Stock Management  
**Database:** PostgreSQL (product_db)

---

## 1. Technology Stack

```xml
<!-- pom.xml dependencies -->
<dependencies>
    <!-- Spring Boot -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    
    <!-- Eureka Client for Service Discovery -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
    </dependency>
    
    <!-- Feign Client -->
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-openfeign</artifactId>
    </dependency>
    
    <!-- PostgreSQL -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
        <scope>runtime</scope>
    </dependency>
    
    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
    
    <!-- MapStruct -->
    <dependency>
        <groupId>org.mapstruct</groupId>
        <artifactId>mapstruct</artifactId>
        <version>1.5.5.Final</version>
    </dependency>
    
    <!-- Actuator -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    
    <!-- Swagger -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>
    
    <!-- Apache POI for Excel export -->
    <dependency>
        <groupId>org.apache.poi</groupId>
        <artifactId>poi-ooxml</artifactId>
        <version>5.2.5</version>
    </dependency>
</dependencies>
```

---

## 2. Database Schema

```sql
-- Create database
CREATE DATABASE product_db;

-- Products table
CREATE TABLE products (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sku VARCHAR(50) UNIQUE NOT NULL,
    barcode VARCHAR(100) UNIQUE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category_id UUID,
    brand VARCHAR(100),
    manufacturer VARCHAR(100),
    price DECIMAL(12, 2) NOT NULL,
    cost_price DECIMAL(12, 2),
    currency VARCHAR(3) DEFAULT 'USD',
    stock_quantity INTEGER DEFAULT 0 CHECK (stock_quantity >= 0),
    minimum_stock_level INTEGER DEFAULT 0,
    maximum_stock_level INTEGER,
    reorder_point INTEGER DEFAULT 0,
    unit VARCHAR(20) DEFAULT 'piece',
    weight DECIMAL(10, 3),
    weight_unit VARCHAR(10) DEFAULT 'kg',
    dimensions_length DECIMAL(10, 2),
    dimensions_width DECIMAL(10, 2),
    dimensions_height DECIMAL(10, 2),
    dimension_unit VARCHAR(10) DEFAULT 'cm',
    tax_rate DECIMAL(5, 2) DEFAULT 0.00,
    is_taxable BOOLEAN DEFAULT true,
    is_active BOOLEAN DEFAULT true,
    status VARCHAR(20) DEFAULT 'ACTIVE', -- ACTIVE, INACTIVE, DISCONTINUED, OUT_OF_STOCK
    image_url TEXT,
    additional_images TEXT[], -- Array of image URLs
    tags TEXT[],
    warranty_period INTEGER, -- in months
    warranty_description TEXT,
    created_by UUID NOT NULL,
    updated_by UUID,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT check_status CHECK (status IN ('ACTIVE', 'INACTIVE', 'DISCONTINUED', 'OUT_OF_STOCK'))
);

-- Product categories table
CREATE TABLE product_categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(100) UNIQUE NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description TEXT,
    parent_category_id UUID,
    image_url TEXT,
    is_active BOOLEAN DEFAULT true,
    display_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (parent_category_id) REFERENCES product_categories(id) ON DELETE SET NULL
);

-- Add foreign key for category
ALTER TABLE products ADD CONSTRAINT fk_product_category 
    FOREIGN KEY (category_id) REFERENCES product_categories(id) ON DELETE SET NULL;

-- Stock history/transactions table
CREATE TABLE stock_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    transaction_type VARCHAR(30) NOT NULL, -- PURCHASE, SALE, ADJUSTMENT, RETURN, DAMAGE, TRANSFER
    quantity_change INTEGER NOT NULL,
    previous_quantity INTEGER NOT NULL,
    new_quantity INTEGER NOT NULL,
    unit_cost DECIMAL(12, 2),
    total_cost DECIMAL(12, 2),
    reference_type VARCHAR(50), -- PURCHASE_ORDER, SALES_ORDER, MANUAL_ADJUSTMENT, etc.
    reference_id UUID,
    location VARCHAR(100),
    reason VARCHAR(500),
    notes TEXT,
    changed_by UUID NOT NULL,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT check_transaction_type CHECK (transaction_type IN 
        ('PURCHASE', 'SALE', 'ADJUSTMENT', 'RETURN', 'DAMAGE', 'TRANSFER', 'INITIAL_STOCK'))
);

-- Product variants table (e.g., size, color)
CREATE TABLE product_variants (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    variant_type VARCHAR(50) NOT NULL, -- SIZE, COLOR, MATERIAL, etc.
    variant_value VARCHAR(100) NOT NULL,
    sku VARCHAR(50) UNIQUE,
    price_adjustment DECIMAL(12, 2) DEFAULT 0.00,
    stock_quantity INTEGER DEFAULT 0,
    is_available BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (product_id, variant_type, variant_value)
);

-- Product suppliers table
CREATE TABLE product_suppliers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    supplier_name VARCHAR(200) NOT NULL,
    supplier_code VARCHAR(50),
    supplier_product_code VARCHAR(100),
    unit_price DECIMAL(12, 2) NOT NULL,
    minimum_order_quantity INTEGER DEFAULT 1,
    lead_time_days INTEGER,
    is_preferred BOOLEAN DEFAULT false,
    contact_person VARCHAR(100),
    contact_email VARCHAR(100),
    contact_phone VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product reviews/ratings table
CREATE TABLE product_reviews (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    user_id UUID NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    title VARCHAR(200),
    review_text TEXT,
    is_verified_purchase BOOLEAN DEFAULT false,
    helpful_count INTEGER DEFAULT 0,
    is_approved BOOLEAN DEFAULT false,
    approved_by UUID,
    approved_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Product pricing history table
CREATE TABLE product_pricing_history (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    old_price DECIMAL(12, 2) NOT NULL,
    new_price DECIMAL(12, 2) NOT NULL,
    price_change_percentage DECIMAL(5, 2),
    reason VARCHAR(500),
    effective_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    changed_by UUID NOT NULL
);

-- Low stock alerts table
CREATE TABLE low_stock_alerts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    current_stock INTEGER NOT NULL,
    minimum_stock INTEGER NOT NULL,
    alert_status VARCHAR(20) DEFAULT 'PENDING', -- PENDING, ACKNOWLEDGED, RESOLVED
    notified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    acknowledged_by UUID,
    acknowledged_at TIMESTAMP,
    resolved_at TIMESTAMP
);

-- Indexes for performance
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_barcode ON products(barcode);
CREATE INDEX idx_products_name ON products(name);
CREATE INDEX idx_products_category ON products(category_id);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_deleted ON products(deleted_at);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_stock_history_product ON stock_history(product_id);
CREATE INDEX idx_stock_history_changed_at ON stock_history(changed_at);
CREATE INDEX idx_stock_history_type ON stock_history(transaction_type);
CREATE INDEX idx_product_variants_product ON product_variants(product_id);
CREATE INDEX idx_product_suppliers_product ON product_suppliers(product_id);
CREATE INDEX idx_product_reviews_product ON product_reviews(product_id);
CREATE INDEX idx_product_reviews_rating ON product_reviews(rating);
CREATE INDEX idx_pricing_history_product ON product_pricing_history(product_id);
CREATE INDEX idx_low_stock_alerts_status ON low_stock_alerts(alert_status);

-- Full-text search index
CREATE INDEX idx_products_name_fts ON products USING gin(to_tsvector('english', name));
CREATE INDEX idx_products_description_fts ON products USING gin(to_tsvector('english', description));

-- Function to update updated_at
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Triggers
CREATE TRIGGER update_products_updated_at BEFORE UPDATE ON products 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_categories_updated_at BEFORE UPDATE ON product_categories 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_suppliers_updated_at BEFORE UPDATE ON product_suppliers 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_reviews_updated_at BEFORE UPDATE ON product_reviews 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Function to check and create low stock alerts
CREATE OR REPLACE FUNCTION check_low_stock()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.stock_quantity <= NEW.minimum_stock_level AND 
       (OLD.stock_quantity IS NULL OR OLD.stock_quantity > NEW.minimum_stock_level) THEN
        INSERT INTO low_stock_alerts (product_id, current_stock, minimum_stock)
        VALUES (NEW.id, NEW.stock_quantity, NEW.minimum_stock_level);
    END IF;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_check_low_stock 
    AFTER INSERT OR UPDATE OF stock_quantity ON products
    FOR EACH ROW EXECUTE FUNCTION check_low_stock();

-- Insert sample categories
INSERT INTO product_categories (name, code, description) VALUES
    ('Electronics', 'ELEC', 'Electronic devices and accessories'),
    ('Furniture', 'FURN', 'Office and home furniture'),
    ('Stationery', 'STAT', 'Office supplies and stationery'),
    ('Computers', 'COMP', 'Computers and peripherals'),
    ('Software', 'SOFT', 'Software products and licenses');
```

---

## 3. Project Structure

```
product-service/
├── src/
│   ├── main/
│   │   ├── java/vn/perp/producterpservice/
│   │   │   ├── ProductErpServiceApplication.java
│   │   │   ├── config/
│   │   │   │   ├── FeignConfig.java
│   │   │   │   ├── SecurityConfig.java
│   │   │   │   └── SwaggerConfig.java
│   │   │   ├── controller/
│   │   │   │   ├── ProductController.java
│   │   │   │   ├── CategoryController.java
│   │   │   │   ├── StockController.java
│   │   │   │   └── ReportController.java
│   │   │   ├── service/
│   │   │   │   ├── ProductService.java
│   │   │   │   ├── CategoryService.java
│   │   │   │   ├── StockService.java
│   │   │   │   ├── InventoryService.java
│   │   │   │   └── ReportService.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.java
│   │   │   │   ├── CategoryRepository.java
│   │   │   │   ├── StockHistoryRepository.java
│   │   │   │   ├── ProductVariantRepository.java
│   │   │   │   ├── ProductSupplierRepository.java
│   │   │   │   ├── ProductReviewRepository.java
│   │   │   │   └── LowStockAlertRepository.java
│   │   │   ├── model/
│   │   │   │   ├── Product.java
│   │   │   │   ├── ProductCategory.java
│   │   │   │   ├── StockHistory.java
│   │   │   │   ├── ProductVariant.java
│   │   │   │   ├── ProductSupplier.java
│   │   │   │   ├── ProductReview.java
│   │   │   │   ├── ProductPricingHistory.java
│   │   │   │   └── LowStockAlert.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── CreateProductRequest.java
│   │   │   │   │   ├── UpdateProductRequest.java
│   │   │   │   │   ├── StockAdjustmentRequest.java
│   │   │   │   │   ├── CreateCategoryRequest.java
│   │   │   │   │   └── ProductReviewRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── ProductResponse.java
│   │   │   │       ├── ProductDetailResponse.java
│   │   │   │       ├── CategoryResponse.java
│   │   │   │       ├── StockHistoryResponse.java
│   │   │   │       ├── InventoryReportResponse.java
│   │   │   │       └── PagedResponse.java
│   │   │   ├── client/
│   │   │   │   └── AuthServiceClient.java
│   │   │   ├── mapper/
│   │   │   │   ├── ProductMapper.java
│   │   │   │   └── CategoryMapper.java
│   │   │   ├── security/
│   │   │   │   ├── JwtAuthenticationFilter.java
│   │   │   │   └── JwtTokenValidator.java
│   │   │   └── exception/
│   │   │       ├── GlobalExceptionHandler.java
│   │   │       ├── ProductNotFoundException.java
│   │   │       ├── InsufficientStockException.java
│   │   │       └── DuplicateSkuException.java
│   │   └── resources/
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       ├── application-docker.yml
│   │       └── application-prod.yml
│   └── test/
│       └── java/com/erp/product/
├── Dockerfile
└── pom.xml
```

---

## 4. Configuration Files

### 4.1 application.yml

```yaml
spring:
  application:
    name: product-service
  
  datasource:
    url: jdbc:postgresql://localhost:5432/product_db
    username: product_user
    password: ${DB_PASSWORD:product_password}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      connection-timeout: 30000
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.PostgreSQLDialect
        format_sql: true
    open-in-view: false

server:
  port: 8083

# Eureka Client
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
    register-with-eureka: true
    fetch-registry: true
  instance:
    hostname: localhost
    prefer-ip-address: true
    instance-id: ${spring.application.name}:${random.value}

# Feign Client
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 5000
        loggerLevel: basic
      auth-service:
        url: http://auth-service

# Inventory alerts
inventory:
  low-stock-check-enabled: true
  low-stock-notification-enabled: true

# Pagination
pagination:
  default-page-size: 20
  max-page-size: 100

# Actuator
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

# Logging
logging:
  level:
    root: INFO
    com.erp.product: DEBUG
    org.hibernate.SQL: DEBUG

# Swagger
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
```

### 4.2 application-docker.yml

```yaml
spring:
  datasource:
    url: jdbc:postgresql://product-db:5432/product_db
    username: product_user
    password: ${DB_PASSWORD}

eureka:
  client:
    service-url:
      defaultZone: http://discovery-service:8761/eureka/
  instance:
    hostname: product-service
    prefer-ip-address: true

feign:
  client:
    config:
      auth-service:
        url: http://auth-service:8081
```

---

## 5. API Endpoints Specification

### 5.1 Product Management Endpoints

#### GET /api/products
List all products (paginated)

**Query Parameters:**
- page (default: 0)
- size (default: 20)
- sort (default: createdAt,desc)
- status (ACTIVE, INACTIVE, DISCONTINUED, OUT_OF_STOCK)
- category (category ID)
- search (search in name, SKU, description)
- minPrice, maxPrice
- inStock (boolean)

**Headers:**
```
Authorization: Bearer {accessToken}
```

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "sku": "PROD-001",
      "name": "Laptop Dell XPS 15",
      "category": {
        "id": "660e8400-e29b-41d4-a716-446655440001",
        "name": "Computers",
        "code": "COMP"
      },
      "price": 1299.99,
      "currency": "USD",
      "stockQuantity": 45,
      "status": "ACTIVE",
      "imageUrl": "https://storage.example.com/products/laptop-xps15.jpg"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 150,
    "totalPages": 8
  }
}
```

---

#### GET /api/products/{id}
Get product details

**Response (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "sku": "PROD-001",
  "barcode": "1234567890123",
  "name": "Laptop Dell XPS 15",
  "description": "High-performance laptop with 15-inch display",
  "category": {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Computers",
    "code": "COMP"
  },
  "brand": "Dell",
  "manufacturer": "Dell Inc.",
  "price": 1299.99,
  "costPrice": 950.00,
  "currency": "USD",
  "stockQuantity": 45,
  "minimumStockLevel": 10,
  "maximumStockLevel": 100,
  "reorderPoint": 15,
  "unit": "piece",
  "weight": 2.5,
  "weightUnit": "kg",
  "dimensions": {
    "length": 35.5,
    "width": 25.0,
    "height": 2.0,
    "unit": "cm"
  },
  "taxRate": 10.00,
  "isTaxable": true,
  "status": "ACTIVE",
  "imageUrl": "https://storage.example.com/products/laptop-xps15.jpg",
  "additionalImages": [
    "https://storage.example.com/products/laptop-xps15-2.jpg",
    "https://storage.example.com/products/laptop-xps15-3.jpg"
  ],
  "tags": ["laptop", "dell", "premium", "business"],
  "warrantyPeriod": 24,
  "warrantyDescription": "2-year manufacturer warranty",
  "variants": [
    {
      "id": "770e8400-e29b-41d4-a716-446655440002",
      "variantType": "MEMORY",
      "variantValue": "16GB",
      "sku": "PROD-001-16GB",
      "priceAdjustment": 0.00,
      "stockQuantity": 25
    }
  ],
  "suppliers": [
    {
      "id": "880e8400-e29b-41d4-a716-446655440003",
      "supplierName": "Tech Distributors Inc.",
      "unitPrice": 950.00,
      "minimumOrderQuantity": 5,
      "leadTimeDays": 7,
      "isPreferred": true
    }
  ],
  "averageRating": 4.5,
  "totalReviews": 28,
  "createdAt": "2024-01-15T08:00:00Z",
  "updatedAt": "2024-03-19T10:00:00Z"
}
```

---

#### POST /api/products
Create new product

**Headers:**
```
Authorization: Bearer {accessToken}
X-User-Role: ROLE_MANAGER or ROLE_ADMIN
```

**Request:**
```json
{
  "sku": "PROD-001",
  "barcode": "1234567890123",
  "name": "Laptop Dell XPS 15",
  "description": "High-performance laptop with 15-inch display",
  "categoryId": "660e8400-e29b-41d4-a716-446655440001",
  "brand": "Dell",
  "manufacturer": "Dell Inc.",
  "price": 1299.99,
  "costPrice": 950.00,
  "currency": "USD",
  "stockQuantity": 50,
  "minimumStockLevel": 10,
  "reorderPoint": 15,
  "unit": "piece",
  "weight": 2.5,
  "weightUnit": "kg",
  "dimensionsLength": 35.5,
  "dimensionsWidth": 25.0,
  "dimensionsHeight": 2.0,
  "taxRate": 10.00,
  "tags": ["laptop", "dell", "premium"],
  "warrantyPeriod": 24,
  "warrantyDescription": "2-year manufacturer warranty"
}
```

**Response (201 Created):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "sku": "PROD-001",
  "name": "Laptop Dell XPS 15",
  "status": "ACTIVE",
  "createdAt": "2024-03-19T10:00:00Z"
}
```

**Validation Rules:**
- SKU: unique, 3-50 characters
- Name: required, 3-200 characters
- Price: positive number
- Stock quantity: non-negative integer

---

#### PUT /api/products/{id}
Update product

**Request:**
```json
{
  "name": "Laptop Dell XPS 15 (Updated)",
  "price": 1349.99,
  "description": "Updated description",
  "stockQuantity": 60,
  "status": "ACTIVE"
}
```

---

#### DELETE /api/products/{id}
Delete (soft delete) product

**Response (200 OK):**
```json
{
  "message": "Product deleted successfully",
  "id": "550e8400-e29b-41d4-a716-446655440000"
}
```

---

#### GET /api/products/search
Search products with full-text search

**Query Parameters:**
- q (search query)
- category
- minPrice, maxPrice
- inStock

**Response:** Same structure as GET /api/products

---

#### PUT /api/products/{id}/stock
Update stock quantity

**Headers:**
```
Authorization: Bearer {accessToken}
X-User-Role: ROLE_MANAGER or ROLE_ADMIN
```

**Request:**
```json
{
  "quantityChange": 10,
  "transactionType": "PURCHASE",
  "reason": "New stock received",
  "unitCost": 950.00,
  "referenceId": "PO-12345",
  "notes": "Purchase order from Tech Distributors"
}
```

**Response (200 OK):**
```json
{
  "productId": "550e8400-e29b-41d4-a716-446655440000",
  "previousQuantity": 45,
  "newQuantity": 55,
  "transactionId": "990e8400-e29b-41d4-a716-446655440004"
}
```

---

#### GET /api/products/{id}/stock-history
Get stock transaction history

**Query Parameters:**
- startDate
- endDate
- transactionType
- page, size

**Response (200 OK):**
```json
{
  "content": [
    {
      "id": "990e8400-e29b-41d4-a716-446655440004",
      "transactionType": "PURCHASE",
      "quantityChange": 10,
      "previousQuantity": 45,
      "newQuantity": 55,
      "unitCost": 950.00,
      "totalCost": 9500.00,
      "reason": "New stock received",
      "changedBy": {
        "id": "aa0e8400-e29b-41d4-a716-446655440005",
        "fullName": "John Doe"
      },
      "changedAt": "2024-03-19T10:00:00Z"
    }
  ],
  "page": {
    "size": 20,
    "number": 0,
    "totalElements": 45,
    "totalPages": 3
  }
}
```

---

### 5.2 Category Endpoints

#### GET /api/categories
List all categories

**Response (200 OK):**
```json
[
  {
    "id": "660e8400-e29b-41d4-a716-446655440001",
    "name": "Computers",
    "code": "COMP",
    "description": "Computers and peripherals",
    "parentCategory": null,
    "productCount": 45,
    "isActive": true
  }
]
```

---

#### POST /api/categories
Create category

**Request:**
```json
{
  "name": "Laptops",
  "code": "LAPTOP",
  "description": "Laptop computers",
  "parentCategoryId": "660e8400-e29b-41d4-a716-446655440001"
}
```

---

#### GET /api/categories/{id}/products
Get products in category

**Query Parameters:**
- page, size, sort
- includeSubcategories (boolean)

---

### 5.3 Inventory & Reporting Endpoints

#### GET /api/inventory/low-stock
Get low stock products

**Response (200 OK):**
```json
[
  {
    "product": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "sku": "PROD-001",
      "name": "Laptop Dell XPS 15"
    },
    "currentStock": 8,
    "minimumStockLevel": 10,
    "reorderPoint": 15,
    "recommendedOrderQuantity": 20,
    "alertCreatedAt": "2024-03-19T09:00:00Z"
  }
]
```

---

#### GET /api/inventory/out-of-stock
Get out of stock products

---

#### GET /api/inventory/summary
Get inventory summary report

**Response (200 OK):**
```json
{
  "totalProducts": 150,
  "totalStockValue": 285000.50,
  "activeProducts": 140,
  "lowStockProducts": 12,
  "outOfStockProducts": 5,
  "discontinuedProducts": 5,
  "categoryBreakdown": [
    {
      "category": "Computers",
      "productCount": 45,
      "totalValue": 125000.00
    }
  ]
}
```

---

#### GET /api/reports/stock-movement
Stock movement report

**Query Parameters:**
- startDate (required)
- endDate (required)
- productId (optional)
- categoryId (optional)
- transactionType (optional)

---

#### GET /api/reports/inventory-valuation
Inventory valuation report

**Response (200 OK):**
```json
{
  "reportDate": "2024-03-19T10:00:00Z",
  "totalInventoryValue": 285000.50,
  "products": [
    {
      "sku": "PROD-001",
      "name": "Laptop Dell XPS 15",
      "quantity": 45,
      "costPrice": 950.00,
      "totalValue": 42750.00
    }
  ]
}
```

---

## 6. Core Components Implementation Guide

### 6.1 Product Entity (model/Product.java)

```java
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String sku;
    
    @Column(unique = true, length = 100)
    private String barcode;
    
    @Column(nullable = false, length = 200)
    private String name;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private ProductCategory category;
    
    @Column(length = 100)
    private String brand;
    
    @Column(length = 100)
    private String manufacturer;
    
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal costPrice;
    
    @Column(length = 3)
    private String currency = "USD";
    
    @Column(nullable = false)
    private Integer stockQuantity = 0;
    
    @Column(nullable = false)
    private Integer minimumStockLevel = 0;
    
    private Integer maximumStockLevel;
    
    @Column(nullable = false)
    private Integer reorderPoint = 0;
    
    @Column(length = 20)
    private String unit = "piece";
    
    @Column(precision = 10, scale = 3)
    private BigDecimal weight;
    
    @Column(length = 10)
    private String weightUnit = "kg";
    
    @Column(precision = 10, scale = 2)
    private BigDecimal dimensionsLength;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal dimensionsWidth;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal dimensionsHeight;
    
    @Column(length = 10)
    private String dimensionUnit = "cm";
    
    @Column(precision = 5, scale = 2)
    private BigDecimal taxRate = BigDecimal.ZERO;
    
    @Column(nullable = false)
    private Boolean isTaxable = true;
    
    @Column(nullable = false)
    private Boolean isActive = true;
    
    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    private ProductStatus status = ProductStatus.ACTIVE;
    
    @Column(columnDefinition = "TEXT")
    private String imageUrl;
    
    @Column(columnDefinition = "TEXT[]")
    private String[] additionalImages;
    
    @Column(columnDefinition = "TEXT[]")
    private String[] tags;
    
    private Integer warrantyPeriod;
    
    @Column(columnDefinition = "TEXT")
    private String warrantyDescription;
    
    @Column(nullable = false)
    private UUID createdBy;
    
    private UUID updatedBy;
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductVariant> variants = new ArrayList<>();
    
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductSupplier> suppliers = new ArrayList<>();
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private LocalDateTime deletedAt;
    
    public enum ProductStatus {
        ACTIVE, INACTIVE, DISCONTINUED, OUT_OF_STOCK
    }
}
```

### 6.2 Stock Service (service/StockService.java)

```java
@Service
@Transactional
@Slf4j
public class StockService {
    private final ProductRepository productRepository;
    private final StockHistoryRepository stockHistoryRepository;
    private final LowStockAlertRepository lowStockAlertRepository;
    
    public StockTransactionResponse adjustStock(UUID productId, StockAdjustmentRequest request, UUID userId) {
        Product product = productRepository.findByIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new ProductNotFoundException("Product not found"));
        
        int previousQuantity = product.getStockQuantity();
        int newQuantity = previousQuantity + request.getQuantityChange();
        
        // Validate stock quantity
        if (newQuantity < 0) {
            throw new InsufficientStockException(
                "Insufficient stock. Available: " + previousQuantity + 
                ", Requested: " + Math.abs(request.getQuantityChange()));
        }
        
        // Update stock
        product.setStockQuantity(newQuantity);
        product.setUpdatedBy(userId);
        
        // Update status based on stock level
        if (newQuantity == 0) {
            product.setStatus(Product.ProductStatus.OUT_OF_STOCK);
        } else if (product.getStatus() == Product.ProductStatus.OUT_OF_STOCK) {
            product.setStatus(Product.ProductStatus.ACTIVE);
        }
        
        productRepository.save(product);
        
        // Record stock history
        StockHistory history = new StockHistory();
        history.setProduct(product);
        history.setTransactionType(request.getTransactionType());
        history.setQuantityChange(request.getQuantityChange());
        history.setPreviousQuantity(previousQuantity);
        history.setNewQuantity(newQuantity);
        history.setUnitCost(request.getUnitCost());
        
        if (request.getUnitCost() != null) {
            history.setTotalCost(
                request.getUnitCost().multiply(new BigDecimal(Math.abs(request.getQuantityChange())))
            );
        }
        
        history.setReferenceType(request.getReferenceType());
        history.setReferenceId(request.getReferenceId());
        history.setReason(request.getReason());
        history.setNotes(request.getNotes());
        history.setChangedBy(userId);
        
        StockHistory savedHistory = stockHistoryRepository.save(history);
        
        log.info("Stock adjusted for product {}: {} -> {}", 
            product.getSku(), previousQuantity, newQuantity);
        
        return StockTransactionResponse.builder()
            .transactionId(savedHistory.getId())
            .productId(product.getId())
            .previousQuantity(previousQuantity)
            .newQuantity(newQuantity)
            .build();
    }
    
    public List<LowStockAlertResponse> getLowStockProducts() {
        List<Product> lowStockProducts = productRepository
            .findByStockQuantityLessThanOrEqualToMinimumStockLevelAndDeletedAtIsNull();
        
        return lowStockProducts.stream()
            .map(this::mapToLowStockAlert)
            .collect(Collectors.toList());
    }
}
```

### 6.3 Product Repository (repository/ProductRepository.java)

```java
@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    
    Optional<Product> findByIdAndDeletedAtIsNull(UUID id);
    
    Optional<Product> findBySkuAndDeletedAtIsNull(String sku);
    
    Page<Product> findByDeletedAtIsNull(Pageable pageable);
    
    Page<Product> findByStatusAndDeletedAtIsNull(Product.ProductStatus status, Pageable pageable);
    
    Page<Product> findByCategoryIdAndDeletedAtIsNull(UUID categoryId, Pageable pageable);
    
    boolean existsBySku(String sku);
    
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND " +
           "p.stockQuantity <= p.minimumStockLevel")
    List<Product> findByStockQuantityLessThanOrEqualToMinimumStockLevelAndDeletedAtIsNull();
    
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND " +
           "p.stockQuantity = 0")
    List<Product> findOutOfStockProducts();
    
    @Query("SELECT p FROM Product p WHERE p.deletedAt IS NULL AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.sku) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT COUNT(p) FROM Product p WHERE p.deletedAt IS NULL AND p.status = :status")
    long countByStatus(@Param("status") Product.ProductStatus status);
    
    @Query("SELECT SUM(p.stockQuantity * p.costPrice) FROM Product p WHERE p.deletedAt IS NULL")
    BigDecimal calculateTotalInventoryValue();
}
```

---

## 7. Security Configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/swagger-ui/**", "/api-docs/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").hasAnyRole("USER", "MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/products/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/products/**").hasAnyRole("MANAGER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/products/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

---

## 8. Docker Configuration

### Dockerfile

```dockerfile
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN ./mvnw dependency:go-offline -B

COPY src src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

EXPOSE 8083

HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8083/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 9. Environment Variables

```bash
# Database
DB_PASSWORD=your_secure_password

# Service Discovery
EUREKA_SERVER=http://discovery-service:8761/eureka/

# Auth Service
AUTH_SERVICE_URL=http://auth-service:8081
```

---

**Service Status:** Ready for Implementation  
**Last Updated:** March 19, 2026
