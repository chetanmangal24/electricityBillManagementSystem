# Electricity Bill Management System

A complete college project for managing electricity consumers, meter readings, bills, and payments with a professional Java Swing desktop GUI.

## Technology Stack

- **Java**: 21 (OpenJDK)
- **GUI Framework**: Java Swing
- **Database**: MySQL 8.x
- **Build Tool**: Maven 3.x
- **IDE**: Apache NetBeans (compatible)
- **JDBC Driver**: MySQL Connector/J 8.3.0

---

## Features Implemented

### 1. Authentication
- Secure login with PBKDF2WithHmacSHA256 password hashing
- Default administrator account (username: `admin`, password: `admin123`)
- Session management with logout functionality

### 2. Dashboard
- Real-time statistics: total consumers, pending bills, outstanding amounts
- Automatic overdue bill detection
- Quick navigation to all modules

### 3. Consumer Management
- Add, view, search, update and delete consumers
- Validation: names, phone numbers (10 digits), meter numbers
- Prevent duplicate meter numbers
- Support for RESIDENTIAL, COMMERCIAL, and INDUSTRIAL connection types

### 4. Meter Readings
- Record dated meter readings for consumers
- Automatic previous reading retrieval
- Validation: current reading cannot be below previous reading
- Calculate consumed units automatically
- Prevent duplicate readings for the same date
- Prevent duplicate billing

### 5. Electricity Billing
- Generate unique bill numbers (format: BILL-YYYYMM-CONSUMERID-TIMESTAMP)
- Configurable tariff rates in `TariffConfig.java`:
  - **Residential**: ₹5/unit (0-100), ₹7/unit (101-200), ₹9/unit (>200) + ₹50 fixed
  - **Commercial**: ₹8/unit (0-100), ₹10/unit (>100) + ₹100 fixed
  - **Industrial**: Flat ₹12/unit + ₹200 fixed
- Calculate bills using BigDecimal for precision
- Include bill date, due date (30 days default), total amount
- Prevent repeated generation for the same reading

### 6. Payments
- Record payments against bills
- Support partial and full payments
- Calculate outstanding balances correctly
- Prevent negative payments and overpayments
- Update payment status automatically (UNPAID → PARTIALLY_PAID → PAID)
- Payment methods: CASH, CARD, UPI, NETBANKING, CHEQUE
- Transaction reference tracking

### 7. Bill History and Reports
- Search bills by consumer name, meter number, or bill number
- View bill details with consumer information
- Filter pending bills
- Printable bill view with formatted output
- Payment history tracking

### 8. Database Architecture
- Prepared statements for SQL injection prevention
- Transactions for multi-table updates (payments update bills)
- Foreign key constraints with CASCADE
- Unique constraints preventing duplicates
- Check constraints for data validation
- Indexes for optimized queries
- Credentials stored outside source control

---

## Prerequisites

1. **Java Development Kit (JDK) 21 or higher**
   - Check: `java -version`
   - Download from: https://adoptium.net/

2. **MySQL Server 8.x**
   - Download from: https://dev.mysql.com/downloads/mysql/
   - Start MySQL service: `net start MySQL` (Windows) or `sudo systemctl start mysql` (Linux)

3. **MySQL Workbench** (recommended for database setup)
   - Download from: https://dev.mysql.com/downloads/workbench/

4. **Apache NetBeans IDE** (optional, for development)
   - Download from: https://netbeans.apache.org/

---

## Database Setup

### Step 1: Create Database and Tables

1. Open **MySQL Workbench**
2. Connect to your MySQL server (localhost:3306)
3. Open the file: `sql/schema.sql`
4. **IMPORTANT**: Before executing, generate the admin password hash:
   
   ```bash
   cd ElectricityBillManagementSystem
   javac -d target/classes src/main/java/com/ebms/util/PasswordUtil.java
   javac -d target/classes -cp target/classes src/main/java/com/ebms/model/*.java
   java -cp target/classes com.ebms.util.PasswordUtil
   ```
   
   This will output:
   ```
   Password: admin123
   Hash: <GENERATED_HASH>
   Verification: true
   ```

5. Copy the generated hash and replace `REPLACE_HASH_HERE` in `schema.sql`
6. Execute the entire script (⚡ Execute icon or Ctrl+Shift+Enter)
7. Verify tables created: `SHOW TABLES;`

Expected tables:
- `users` - Authentication
- `consumers` - Electricity consumers
- `meter_readings` - Monthly readings
- `bills` - Generated bills
- `payments` - Payment records

### Step 2: Configure Database Connection

1. Navigate to `src/main/resources/`
2. Copy `db.properties.template` to `db.properties`:
   ```bash
   cp src/main/resources/db.properties.template src/main/resources/db.properties
   ```

3. Edit `db.properties` with your MySQL credentials:
   ```properties
   db.url=jdbc:mysql://localhost:3306/ebms?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
   db.username=root
   db.password=your_mysql_password
   ```

4. **IMPORTANT**: Never commit `db.properties` to version control (already in `.gitignore`)

---

## Build and Run

### Option 1: Using NetBeans (Recommended)

1. Open NetBeans
2. File → Open Project
3. Navigate to `ElectricityBillManagementSystem` folder
4. NetBeans will detect it as a Maven project
5. Right-click project → Build (or F11)
6. Right-click project → Run (or F6)

### Option 2: Using Command Line

#### If Maven is installed:
```bash
cd ElectricityBillManagementSystem
mvn clean compile
mvn exec:java -Dexec.mainClass="com.ebms.Main"
```

#### If Maven is NOT installed (using javac):
```bash
cd ElectricityBillManagementSystem
mkdir -p target/classes

# Compile all Java files
javac -d target/classes -sourcepath src/main/java \
  src/main/java/com/ebms/model/*.java \
  src/main/java/com/ebms/util/*.java \
  src/main/java/com/ebms/dao/*.java \
  src/main/java/com/ebms/service/*.java \
  src/main/java/com/ebms/ui/*.java \
  src/main/java/com/ebms/Main.java

# Copy resources
cp src/main/resources/db.properties target/classes/

# Run application
java -cp target/classes:lib/mysql-connector-j-8.3.0.jar com.ebms.Main
```

**Note**: You need to download MySQL Connector/J manually and place it in `lib/` folder if not using Maven.

---

## Initial Login

1. Launch the application
2. Login screen appears
3. Use default credentials:
   - **Username**: `admin`
   - **Password**: `admin123`
4. **IMPORTANT**: Change the admin password after first login for security

---

## Application Structure

```
ElectricityBillManagementSystem/
├── pom.xml                          # Maven configuration
├── sql/
│   └── schema.sql                   # Database schema with seed data
├── src/
│   ├── main/
│   │   ├── java/com/ebms/
│   │   │   ├── dao/                 # Data Access Objects
│   │   │   │   ├── BaseDAO.java
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── ConsumerDAO.java
│   │   │   │   ├── MeterReadingDAO.java
│   │   │   │   ├── BillDAO.java
│   │   │   │   └── PaymentDAO.java
│   │   │   ├── model/               # Entity classes
│   │   │   │   ├── User.java
│   │   │   │   ├── Consumer.java
│   │   │   │   ├── MeterReading.java
│   │   │   │   ├── Bill.java
│   │   │   │   └── Payment.java
│   │   │   ├── service/             # Business logic
│   │   │   │   ├── AuthService.java
│   │   │   │   ├── DashboardService.java
│   │   │   │   └── TariffConfig.java
│   │   │   ├── ui/                  # Swing GUI components
│   │   │   │   ├── LoginFrame.java
│   │   │   │   ├── MainFrame.java
│   │   │   │   ├── ConsumerPanel.java
│   │   │   │   ├── ViewConsumersPanel.java
│   │   │   │   ├── MeterReadingPanel.java
│   │   │   │   ├── ViewReadingsPanel.java
│   │   │   │   ├── GenerateBillPanel.java
│   │   │   │   ├── ViewBillsPanel.java
│   │   │   │   ├── PaymentPanel.java
│   │   │   │   └── ViewPaymentsPanel.java
│   │   │   ├── util/                # Utilities
│   │   │   │   ├── DBConnection.java
│   │   │   │   ├── PasswordUtil.java
│   │   │   │   ├── ValidationUtil.java
│   │   │   │   └── BillNumberGenerator.java
│   │   │   └── Main.java            # Application entry point
│   │   └── resources/
│   │       └── db.properties.template
│   └── test/
│       └── java/                    # Test cases (future)
└── README.md
```

---

## Usage Guide

### 1. Consumer Management
- **Menu**: Consumers → Add Consumer
- Fill in name, address, phone (10 digits), meter number, connection type
- System validates inputs and prevents duplicate meter numbers

### 2. Recording Meter Readings
- **Menu**: Meter Readings → Record Reading
- Select consumer
- System auto-loads previous reading
- Enter current reading (must be ≥ previous)
- System calculates units consumed

### 3. Generating Bills
- **Menu**: Bills → Generate Bill
- Select consumer and unbilled reading
- System calculates amount based on tariff rates
- Set bill date and due date (default: 30 days)
- System generates unique bill number

### 4. Recording Payments
- **Menu**: Payments → Record Payment
- Select consumer and pending bill
- System shows bill details and outstanding amount
- Enter payment amount (validates against remaining)
- Select payment method and add transaction reference
- System updates bill status automatically

### 5. Viewing Reports
- **Menu**: Bills → View Bills
- Search by bill number, consumer name, or meter number
- Filter: Show All / Show Pending
- View bill details
- Print bill (formatted view)

---

## Troubleshooting

### Database Connection Failed
**Symptoms**: "Database connection failed" on login

**Solutions**:
1. Verify MySQL server is running:
   ```bash
   # Windows
   net start MySQL
   
   # Linux
   sudo systemctl status mysql
   ```

2. Check `db.properties` credentials:
   - Username and password correct
   - Database `ebms` exists
   - MySQL running on localhost:3306

3. Test connection manually:
   ```bash
   mysql -u root -p
   USE ebms;
   SHOW TABLES;
   ```

### Login Failed with Correct Credentials
**Cause**: Password hash not generated correctly

**Solution**:
1. Run `PasswordUtil.java` to generate hash
2. Update `users` table manually:
   ```sql
   UPDATE users SET password_hash = 'YOUR_GENERATED_HASH' WHERE username = 'admin';
   ```

### Compilation Errors
**Cause**: MySQL JDBC driver not found

**Solution**:
- Using Maven: Dependencies auto-download
- Using javac: Download MySQL Connector/J from https://dev.mysql.com/downloads/connector/j/ and add to classpath

### UI Not Appearing
**Cause**: Main class not executed properly

**Solution**:
```bash
java -cp target/classes com.ebms.Main
```

Ensure all classes compiled successfully

---

## Security Notes

1. **Password Storage**: Uses PBKDF2WithHmacSHA256 with 10,000 iterations
2. **SQL Injection**: All queries use PreparedStatement
3. **Database Credentials**: Stored outside source control in `db.properties`
4. **Default Password**: Change `admin` password immediately after first login
5. **Connection Security**: Use SSL for production deployments

---

## Modifying Tariff Rates

Edit `src/main/java/com/ebms/service/TariffConfig.java`:

```java
// Residential rates
private static BigDecimal RES_RATE_0_100 = new BigDecimal("5.00");
private static BigDecimal RES_RATE_101_200 = new BigDecimal("7.00");
private static BigDecimal RES_RATE_ABOVE_200 = new BigDecimal("9.00");
private static BigDecimal RES_FIXED_CHARGE = new BigDecimal("50.00");
```

Recompile after changes.

---

## Known Limitations

1. **Single User Session**: No concurrent user support
2. **Tariff Changes**: Not retroactive to existing bills
3. **Bill Deletion**: Not implemented (data integrity)
4. **Backup**: Manual MySQL dump required
5. **Reports**: Limited to screen display (no PDF export)

---

## Future Enhancements

- Multi-user concurrent access
- Bill PDF generation and email delivery
- Advanced reporting (monthly summaries, graphs)
- Tariff history tracking
- Consumer portal for self-service
- SMS/Email notifications for due dates
- Payment gateway integration
- Automated meter reading import

---

## License

This is a college project. Free to use for educational purposes.

---

## Support

For issues or questions:
1. Check the Troubleshooting section above
2. Verify all prerequisites are installed
3. Ensure database schema is correctly set up
4. Review error messages in the application console

---

**Project Status**: Complete and functional for college submission

**Last Updated**: October 2026
