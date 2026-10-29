import http from 'k6/http';
import { check, sleep } from 'k6';
import { Counter } from 'k6/metrics';

// Base Configuration & Environment Variables
const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const CUSTOMER_IDENTITY = __ENV.CUSTOMER_IDENTITY || 'charlie.brown@example.com';
const CUSTOMER_PASSWORD = __ENV.CUSTOMER_PASSWORD || 'Password123!';
const ADMIN_IDENTITY = __ENV.ADMIN_IDENTITY || 'alice.manager@midnightcafe.dev';
const ADMIN_PASSWORD = __ENV.ADMIN_PASSWORD || 'Password123!';

// Custom metrics
const errorCount = new Counter('errors');

let product = {
    productId: String,
    name: String,
    description: String,
    price: Number,
    category: String
}

let products = [product]

export const options = {
    scenarios: {
        anonymous_user: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 1 },
                { duration: '30s', target: 3 },
                { duration: '1m', target: 5 },
                { duration: '30s', target: 0 },
            ],
            exec: 'anonymousUser',
        },
        customer_user: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 1 },
                { duration: '30s', target: 3 },
                { duration: '1m', target: 5 },
                { duration: '30s', target: 0 },
            ],
            exec: 'customerUser',
        },
        admin_user: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '30s', target: 5 },
                { duration: '30s', target: 0 },
                { duration: '30s', target: 5 },
            ],
            exec: 'adminUser',
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<400', 'p(99)<900'],
        http_req_failed: ['rate<0.01'],
        errors: ['count<10'],
    },
};

// Helper: UUID generator
function generateUUID() {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
        const r = (Math.random() * 16) | 0;
        const v = c === 'x' ? r : (r & 0x3) | 0x8;
        return v.toString(16);
    });
}

// Helper: Category randomizer
const CATEGORIES = ['BEVERAGES', 'BAKED_GOODS'];

function getRandomCategory() {
    return CATEGORIES[Math.floor(Math.random() * CATEGORIES.length)];
}

// Record metrics and checks safely
function recordResponse(res, checkName, expectedStatuses = [200]) {
    const isOk = check(res, {
        [`${checkName} - status ${expectedStatuses.join('/')}`]: (r) =>
            expectedStatuses.includes(r.status),
    });

    if (!isOk) {
        errorCount.add(1);
    }
    return isOk;
}

// Helper: Random sleep between min and max seconds
function sleepRandom(min = 1, max = 6) {
    sleep(Math.random() * (max - min) + min);
}

// 1. Anonymous User Flow (Browsing public pages, product catalog, public read endpoints)
export function anonymousUser() {
    // 1. Home Page
    let res = http.get(`${BASE_URL}/`);
    recordResponse(res, 'Anonymous: Home Page', [200]);
    sleepRandom(1, 6);

    // 2. Browse Products Page
    res = http.get(`${BASE_URL}/products`);
    recordResponse(res, 'Anonymous: Products List Page', [200]);
    sleepRandom(1, 6);

    let randomProductId = generateUUID();
    if (res.status === 200) {
        try {
            const products = res.json();
            if (Array.isArray(products) && products.length > 0 && products[0].id) {
                randomProductId = products[Math.floor(Math.random() * products.length)].id;
            }
        } catch (e) {
            // fallback to generated UUID
        }
    }

    // 3. Product Detail Page
    res = http.get(`${BASE_URL}/products/${randomProductId}`);
    recordResponse(res, 'Anonymous: Product Detail Page', [200, 404]);
    sleepRandom(1, 6);

    // 4. Login Page
    res = http.get(`${BASE_URL}/login`);
    recordResponse(res, 'Anonymous: Login Page', [200]);
    sleepRandom(1, 6);

    // 5. Register Page
    res = http.get(`${BASE_URL}/register`);
    recordResponse(res, 'Anonymous: Register Page', [200]);
    sleepRandom(1, 6);
}

// 2. Customer User Flow (Login, Browse, Product Detail, Add to Cart, Profile lookups)
export function customerUser() {
    // 1. Customer Login
    const loginPayload = {
        identity: CUSTOMER_IDENTITY,
        password: CUSTOMER_PASSWORD,
    };

    let userDto = null;

    let res = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        redirects: 0,
    });
    recordResponse(res, 'Customer: Login', [200, 302, 303, 401]);

    let userId;
    try {
        userDto = res.json();
        userId = userDto ? userDto.id : null;
    } catch (e) {
        userDto = null;
    }
    sleepRandom(1, 6);

    // 2. Browse Products
    res = http.get(`${BASE_URL}/api/v1/products`);
    recordResponse(res, 'Customer: Browse Products', [200]);
    sleepRandom(1, 6);

    let randomProductId;
    if (res.status === 200) {
        try {
            products = res.json();
            if (Array.isArray(products) && products.length > 0 && products[0].productId) {
                product = products[Math.floor(Math.random() * products.length)]
                randomProductId = product.productId;
            }
        } catch (e) {}
    }

    // 3. View Product Detail
    res = http.get(`${BASE_URL}/products/${randomProductId}`);
    recordResponse(res, 'Customer: View Product Detail', [200, 404]);
    sleepRandom(1, 6);

    // 4. Add Product to Cart
    let customerId = generateUUID();
    const lookupId = userId || (userDto && userDto.id);
    if (lookupId) {
        let cust = http.get(`${BASE_URL}/api/v1/customers/${lookupId}`);
        try {
            const customerDto = cust.json();
            if (customerDto && customerDto.id) {
                customerId = customerDto.id;
            }
        } catch (e) {}
    }
    let cartRes = http.post(`${BASE_URL}/api/v1/products/cart/${customerId}`, product, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    });

    // Add to cart redirects (303 SEE_OTHER) to /products/{productId}
    recordResponse(cartRes, 'Customer: Add Product to Cart', [200, 302, 303]);
    sleepRandom(1, 6);

    // 5. Customer Profile
    res = http.get(`${BASE_URL}/api/v1/customers`);
    recordResponse(res, 'Customer: Get Customers List', [200, 401, 403]);
    sleepRandom(1, 6);
}

// 3. Admin User Flow (Admin Login, Dashboard, Admin views, Entity Creation APIs)
export function adminUser() {
    // 1. Admin / Manager Login
    const loginPayload = {
        identity: ADMIN_IDENTITY,
        password: ADMIN_PASSWORD,
    };

    let res = http.post(`${BASE_URL}/api/auth/login`, loginPayload, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        redirects: 0,
    });
    recordResponse(res, 'Admin: Login', [200, 302, 303, 401]);
    sleepRandom(1, 6);

    // 2. Admin Dashboard
    res = http.get(`${BASE_URL}/admin`);
    recordResponse(res, 'Admin: Access Dashboard', [200, 302, 403]);
    sleepRandom(1, 6);

    // 3. Admin Products View
    res = http.get(`${BASE_URL}/admin/products`);
    recordResponse(res, 'Admin: Products View', [200, 302, 403]);
    sleepRandom(1, 6);

    // 4. Create New Product (Admin API)
    const productPayload = JSON.stringify({
        name: `Specialty Roast ${generateUUID().substring(0, 8)}`,
        description: 'Single-origin artisan dark roast',
        price: parseFloat((Math.random() * 10 + 2).toFixed(2)),
        category: getRandomCategory(),
        image: 'coffee-special.jpg',
    });

    res = http.post(`${BASE_URL}/api/v1/products`, productPayload, {
        headers: { 'Content-Type': 'application/json' },
    });
    recordResponse(res, 'Admin: Create New Product (API)', [200, 201, 400, 403]);
    sleepRandom(1, 6);

    // 5. Admin Customers View
    res = http.get(`${BASE_URL}/admin/customers`);
    recordResponse(res, 'Admin: Customers View', [200, 302, 403]);
    sleepRandom(1, 6);

    // 6. Create New Customer (Admin API)
    const customerPayload = JSON.stringify({
        name: `Customer ${generateUUID().substring(0, 6)}`,
        email: `customer_${generateUUID().substring(0, 8)}@example.com`,
        phone: '555-0144',
    });

    res = http.post(`${BASE_URL}/api/v1/customers`, customerPayload, {
        headers: { 'Content-Type': 'application/json' },
    });
    recordResponse(res, 'Admin: Create New Customer (API)', [200, 201, 400, 403]);
    sleepRandom(1, 6);

    // 7. Admin Employees View
    res = http.get(`${BASE_URL}/admin/employees`);
    recordResponse(res, 'Admin: Employees View', [200, 302, 403]);
    sleepRandom(1, 6);

    // 8. Create New Employee (Admin API)
    const employeePayload = JSON.stringify({
        name: `Barista ${generateUUID().substring(0, 6)}`,
        email: `barista_${generateUUID().substring(0, 8)}@themidnightcafe.dev`,
        position: 'Shift Barista',
    });

    res = http.post(`${BASE_URL}/api/v1/employees`, employeePayload, {
        headers: { 'Content-Type': 'application/json' },
    });
    recordResponse(res, 'Admin: Create New Employee (API)', [200, 201, 400, 403]);
    sleepRandom(1, 6);
}
