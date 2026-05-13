# InstaBuy

InstaBuy is a full-stack, distributed E-commerce platform that connects buyers and vendors. The application features a robust microservices backend powered by Spring Boot and a dynamic frontend built with React.

## 📸 Screenshots

<table>
  <tr>
    <td align="center"><img src="docs/images/login.png" alt="Login" width="300" /><br /><b>Login</b></td>
    <td align="center"><img src="docs/images/register.png" alt="Register" width="300" /><br /><b>Register</b></td>
    <td align="center"><img src="docs/images/user%20Dashboard.png" alt="User Dashboard" width="300" /><br /><b>User Dashboard</b></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/images/Product.png" alt="Product Details" width="300" /><br /><b>Product Details</b></td>
    <td align="center"><img src="docs/images/Cart.png" alt="Cart" width="300" /><br /><b>Cart</b></td>
    <td align="center"><img src="docs/images/VendorCatalog.png" alt="Vendor Catalog" width="300" /><br /><b>Vendor Catalog</b></td>
  </tr>
  <tr>
    <td align="center"><img src="docs/images/VendorProducts.png" alt="Vendor Products" width="300" /><br /><b>Vendor Products</b></td>
    <td align="center"><img src="docs/images/Vendor%20Dashboard.png" alt="Vendor Dashboard" width="300" /><br /><b>Vendor Dashboard</b></td>
    <td align="center"><img src="docs/images/Invoice.png" alt="Vendor Dashboard" width="300" /><br /><b>Invoice</b></td>
  </tr>
  
</table>

## 🚀 Features

- **Vendor Management & Sales Dashboard:** Dedicated profiles for vendors, including inventory management and sales analytics dashboards powered by Recharts.
- **Advanced Checkout Flow:** Seamless checkout with address selection, order history, and integration with Stripe for processing payments and handling callbacks.
- **Local Storage Cart:** Optimized client-side cart management leveraging browser local storage for a faster user experience without backend dependencies.
- **Secure Authentication:** JWT-based user authentication supporting multiple roles (User, Vendor).
- **Service Discovery & Gateway:** Backend microservices orchestrated via Spring Cloud Netflix Eureka and Spring Cloud Gateway for efficient routing and load balancing.

## 🏗 Architecture

The platform adopts a microservices architecture, separating concerns into distinct deployable units:

### Backend Services (Java 17, Spring Boot 3.5.x)
- `ApiGatewayService`: Entry point for the frontend, routing requests to the appropriate microservices.
- `EurekaService`: Netflix Eureka server for service registry and discovery.
- `Userservice`: Manages user authentication, profiles, and role-based access control.
- `ProductService`: Handles product catalog and details.
- `InventoryService`: Manages product stock and availability.
- `OrderService`: Processes user orders and order history.
- `PaymentService`: Integrates with Stripe API to handle checkout sessions, webhooks, and payment statuses.

### Frontend (React 19)
- Built with Create React App.
- Uses **React Router** for navigation.
- Styling via **Bootstrap**.
- Charts and Data Visualization using **Recharts**.
- API communication via **Axios**.

## 🛠 Tech Stack

**Frontend:** React, React Router DOM, Bootstrap, Recharts, Axios  
**Backend:** Java 17, Spring Boot, Spring Cloud (Gateway, Eureka), Spring Security (JWT), Maven  
**Third-Party Services:** Stripe (Payments), GreenMail (Email Testing)

## 📦 Getting Started

### Prerequisites
- Node.js (v18+)
- Java 17
- Maven
- A Stripe Developer Account (for Payment testing)

### Running the Backend

1. Navigate to each backend service directory (start with `EurekaService` first, then `ApiGatewayService`, and then the rest).
2. Use Maven to run each application:
   ```bash
   cd backend/<ServiceName>
   ./mvnw spring-boot:run
   ```

### Running the Frontend

1. Navigate to the `frontend` directory:
   ```bash
   cd frontend
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start the React development server:
   ```bash
   npm start
   ```
4. Open [http://localhost:3000](http://localhost:3000) to view it in the browser.
