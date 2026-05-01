# How to Run Angavai Sangavai - Bidding Platform

## requirements
1. **Java JDK 8 or higher** 
2. **Maven** 
3.**MySql**
### Step 1: Start the Backend Server

1. Open Command Prompt
2. Navigate to the backend folder:
   cd C:\Users\Desktop\bis\backend
3. Run the server:

   mvn spring-boot:run

4. Wait for the server to start. You'll see:
   

   Server running at: http://localhost:8080 <------------------------------(Open this after running the backend)

5. **Keep this Command Prompt window open!** The server must keep running.

### Step 2: Open the Application

control +click
|
---->http://localhost:8080

### Step 3: Use the Application

**For Buyers:**
1. Click "Sign Up" to register as a Buyer
2. Login with your credentials
3. Browse products and place bids
4. Add funds to wallet
5. Add won items to cart
6. Place orders

**For Sellers:**
1. Click "Sign Up" to register as a Seller
2. Login with your credentials
3. Click "Add New Product" to list items
4. Manage your products (Start/Stop/Delete)
5. View bids on your products
6. Manage orders (Accept/Reject)
7. View return requests

### Step 4: Stop the Server

When you're done:
1. Go to the Command Prompt window where the server is running
2. Press `Ctrl + C`
3. Type `Y` and press Enter to confirm

## Quick Start Commands
# Start Backend
cd C:\Users\Desktop\bis\backend
mvn spring-boot:run

