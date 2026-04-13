# Project Architecture & Workflow Analysis

This document provides a comprehensive detailed analysis of the **LifeSamadhan** project. The project is a Service Aggregation Platform designed to work with **one Frontend** that can dynamically switch between **two different Backends** (.NET Core and Spring Boot) which share identical business logic.

---

## 1. Core Workflow: The Service Lifecycle

The application generally follows this flow:
1.  **Authentication**: Users (Customers/Providers/Admins) login and receive a **JWT**.
2.  **Service Request**: A Customer requests a service (e.g., Cleaning).
3.  **Assignment**: The backend finds an eligible Provider and assigns the job.
4.  **Notification & OTP**:
    *   The Provider receives a socket notification.
    *   The Customer receives an **Email with an OTP**.
5.  **Execution**: The Provider verifies the OTP to **Start** the job.
6.  **Payment**: Upon completion, a **Razorpay** order is generated and paid by the customer.

---

## 2. Technical Implementation Details

Below is the breakdown of how each key technology is implemented in both backends.

### A. JWT Authentication (JSON Web Tokens)
**Purpose**: Stateless authentication. After login, the server issues a signed token containing the user's ID and Role. This token must be sent in the `Authorization: Bearer <token>` header for all future requests.

#### 1. Spring Boot Implementation
**File**: `lifeFull/SpringBootLifeSamadhan/src/main/java/com/lifesamadhan/api/security/JwtUtils.java`
**Logic**: Uses the `io.jsonwebtoken` library to build and sign tokens.

```java
@Component
public class JwtUtils {

    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String secret;

    public String generateToken(String email, Long userId, String role) {
        Date iat = new Date();
        Date expDate = new Date(iat.getTime() + 86400000); // 1 day

        return Jwts.builder()
                .subject(email)
                .issuedAt(iat)
                .expiration(expDate)
                .claims(Map.of("user_id", userId, "role", role))
                .signWith(getKey())
                .compact(); // Returns the encoded token string
    }
    // ... validation logic ...
}
```

#### 2. .NET Core Implementation
**File**: `LifeSamadhan/Backend/Services/JwtService.cs`
**Logic**: Uses `System.IdentityModel.Tokens.Jwt` to create tokens based on configuration in `appsettings.json`.

```csharp
public class JwtService
{
    private readonly IConfiguration config;

    public string Generate(User u)
    {
        var claims = new[]
        {
            new Claim(ClaimTypes.NameIdentifier, u.Id.ToString()),
            new Claim(ClaimTypes.Role, u.Role),
            new Claim(ClaimTypes.Email, u.Email)
        };

        var key = new SymmetricSecurityKey(Encoding.UTF8.GetBytes(config["Jwt:Key"]));
        
        var token = new JwtSecurityToken(
            issuer: config["Jwt:Issuer"],
            audience: config["Jwt:Audience"],
            claims: claims,
            expires: DateTime.Now.AddMinutes(double.Parse(config["Jwt:ExpireMinutes"])),
            signingCredentials: new SigningCredentials(key, SecurityAlgorithms.HmacSha256)
        );

        return new JwtSecurityTokenHandler().WriteToken(token);
    }
}
```

---

### B. OTP Generation & SMTP (Email)
**Purpose**: To verify the physical presence of the provider at the customer's location. The OTP is generated when a provider accepts a job and is emailed to the customer.

#### 1. Spring Boot Implementation
**File**: `lifeFull/SpringBootLifeSamadhan/src/main/java/com/lifesamadhan/api/service/EmailService.java`
Uses `JavaMailSender` to send simple emails.

```java
@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    public void sendOtpEmail(String toEmail, String otp, String providerName, String serviceName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("your-email@gmail.com");
        message.setTo(toEmail);
        message.setSubject("Your Service OTP - LifeSamadhan");
        message.setText("... Verification OTP: " + otp + " ...");
        mailSender.send(message);
    }
}
```

**File**: `lifeFull/SpringBootLifeSamadhan/src/main/java/com/lifesamadhan/api/service/ProviderService.java`
Generates the OTP within the `acceptAssignment` method.

```java
// Inside acceptAssignment method
if (assignment.getOtp() == null) {
    // Generate 4 digit random OTP
    String otp = String.format("%04d", new java.util.Random().nextInt(10000));
    assignment.setOtp(otp);
}
// ...
emailService.sendOtpEmail(customerEmail, assignment.getOtp(), ...);
```

#### 2. .NET Core Implementation
**File**: `LifeSamadhan/Backend/Services/EmailService.cs`
Uses `System.Net.Mail.SmtpClient`.

```csharp
public class EmailService : IEmailService
{
    public async Task SendEmailAsync(string toEmail, string subject, string message)
    {
        // ... config reading ...
        using (var client = new SmtpClient(host, port))
        {
            client.Credentials = new NetworkCredential(fromEmail, password);
            client.EnableSsl = true;
            
            var mailMessage = new MailMessage
            {
                From = new MailAddress(fromEmail),
                Subject = subject,
                Body = message,
                IsBodyHtml = true
            };
            mailMessage.To.Add(toEmail);
            await client.SendMailAsync(mailMessage);
        }
    }
}
```

**File**: `LifeSamadhan/Backend/Services/ServiceAssignmentService.cs`
Generates OTP during assignment logic.

```csharp
var assignment = new ServiceAssignment
{
    // ...
    Otp = new Random().Next(100000, 999999).ToString(), // 6 digit OTP
    // ...
};
// ...
await _emailService.SendEmailAsync(customer.Email, subject, body);
```

---

### C. Real-Time Notifications (WebSocket / SignalR)
**Purpose**: To push updates (like "Job Accepted") to the frontend immediately.

#### 1. Spring Boot (WebSocket + STOMP)
**File**: `lifeFull/SpringBootLifeSamadhan/src/main/java/com/lifesamadhan/api/config/WebSocketConfig.java`
Configures the STOMP endpoint (`/ws`) and broker (`/topic`).

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }
}
```

**Usage** (`ProviderService.java`):
```java
messagingTemplate.convertAndSend("/topic/request-" + assignment.getRequestId(), responseDTO);
```

#### 2. .NET Core (SignalR)
**File**: `LifeSamadhan/Backend/Hubs/NotificationHub.cs`
Defines the Hub.

```csharp
public class NotificationHub : Hub
{
    // Clients connect to this Hub
    public async Task SendNotification(string userId, string message)
    {
        await Clients.User(userId).SendAsync("ReceiveNotification", message);
    }
}
```

**Usage** (`Services/NotificationService.cs`):
```csharp
await _hub.Clients.User(userId.ToString()).SendAsync("ReceiveNotification", message);
```

---

### D. Razorpay Payment Integration
**Purpose**: Handling payments. This involves creating an "Order" on the backend, ensuring the amount is correct, and then verifying the payment signature after the frontend completes the transaction.

#### 1. Spring Boot Implementation
**File**: `lifeFull/SpringBootLifeSamadhan/src/main/java/com/lifesamadhan/api/service/PaymentService.java`

**Create Order**:
```java
public Map<String, Object> createRazorpayOrderDetail(Long assignmentId) {
    RazorpayClient razorpay = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
    JSONObject orderRequest = new JSONObject();
    orderRequest.put("amount", amount.multiply(new BigDecimal(100)).intValue()); // Amount in paise
    orderRequest.put("currency", "INR");
    orderRequest.put("receipt", "txn_" + assignmentId);
    
    Order order = razorpay.orders.create(orderRequest);
    // ... save to DB ...
}
```

**Verify Payment**:
```java
public void verifyRazorpayPayment(Map<String, String> data) {
    // Spring Boot implementation often trusts the frontend sending the success signature 
    // or verifies it using HmacSHA256 similar to .NET logic if strict security is applied.
    // In this codebase, it looks up the order and marks it as COMPLETED.
    String orderId = data.get("razorpayOrderId");
    Payment payment = paymentRepository.findByRazorpayOrderId(orderId).orElseThrow();
    payment.setPaymentStatus("COMPLETED");
}
```

#### 2. .NET Core Implementation
**File**: `LifeSamadhan/Backend/Controllers/PaymentController.cs`

**Create Order**:
```csharp
[HttpPost("create-order/{assignmentId}")]
public IActionResult CreateOrder(long assignmentId)
{
    Razorpay.Api.RazorpayClient client = new Razorpay.Api.RazorpayClient(_keyId, _keySecret);
    Dictionary<string, object> options = new Dictionary<string, object>();
    options.Add("amount", (int)(request.Amount * 100));
    // ...
    Razorpay.Api.Order order = client.Order.Create(options);
    // ...
}
```

**Verify Payment (Manual Signature Check)**:
```csharp
public static string GetGeneratedSignature(string orderId, string paymentId, string secret)
{
    string payload = orderId + "|" + paymentId;
    using (var hmac = new HMACSHA256(Encoding.UTF8.GetBytes(secret)))
    {
        byte[] hash = hmac.ComputeHash(Encoding.UTF8.GetBytes(payload));
        return BitConverter.ToString(hash).Replace("-", "").ToLower();
    }
}
// Matches generated signature with dto.RazorpaySignature
```

---

## 3. Frontend Integration Code
**File**: `Frontend/src/services/notificationService.js`
This file acts as the bridge. It checks an environment variable to decide which WebSocket implementation to load.

```javascript
import signalRService from './signalr';           // For .NET
import springBootService from './springboot-websocket'; // For Java

const BACKEND_TYPE = import.meta.env.VITE_BACKEND_TYPE || 'SPRINGBOOT';

class NotificationService {
    constructor() {
        // Dynamically selects the service based on configuration
        this.service = (BACKEND_TYPE === 'DOTNET') ? signalRService : springBootService;
        console.log(`Notification Service initialized using: ${BACKEND_TYPE}`);
    }

    startConnection(token, user) {
        return this.service.startConnection(token, user);
    }
    // ...
}
```
