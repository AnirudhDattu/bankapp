# 📱 IzBank — Android Banking App: Codebase Guide

## Technology Stack

| Layer | Technology |
|---|---|
| **Language** | Java (Android) |
| **Backend/Database** | Back4App (hosted Parse Server — cloud BaaS) |
| **UI** | AndroidX, Material Design 3, RecyclerView, Fragment-based navigation |
| **Charts** | MPAndroidChart (line & pie charts) |
| **QR Code** | ZXing (generate) + ZXing Android Embedded (scan) |
| **Real-time** | Parse LiveQuery + polling fallback |
| **Architecture** | Activity + Fragment pattern with a global singleton `mainUser` |

---

## 📂 Folder-by-Folder Breakdown

---

### 1. `splashScreen/` — App Entry & Role Routing

**Purpose:** This is the very first screen a user sees after logging in. It detects whether the user is a regular user or admin and routes them accordingly.

#### `splashScreen.java`
- **`onCreate()`**: Inflates the splash layout, plays a pulsing logo animation, then after 3 seconds uses a `Handler` to check `mainUser` identity.
  - If the user ID is `"9999"` (admin), it applies the **State design pattern** by setting `userContext.setState(adminUser)` and routes to `AdminPanelActivity`.
  - Otherwise, sets state to the regular `User` and routes to `MainScreenActivity`.
- Uses `UserContext` + `UserTypeState` (State Pattern) to determine and apply role.

#### `GifImageView.java`
- A custom Android `View` that renders animated GIFs using the `android.graphics.Movie` API.
- **Key methods:**
  - `setGifImageResource(int id)` — loads GIF from raw resources
  - `onDraw()` — continuously redraws each frame to animate the GIF
- **Note:** This view was used in an older splash animation but is now replaced by the alpha animation in `splashScreen.java`.

---

### 2. `Sign/` — Authentication (Login & Registration)

**Purpose:** Handles user sign-in and sign-up flows, and loads all user data from the Parse database after login.

#### `SignIn.java`
This is also the **data loader hub** of the whole app — it holds the global `mainUser` static object that every other part of the app reads.

- **`signIn(View view)`**: Reads username/password EditText. Special handling:
  - ID `"9999"` → Admin mode (fetches all users, goes to `AdminPanelActivity`)
  - ID `"0000"` → Demo mode (pre-fills a fake user with demo accounts and history, no DB call)
  - Otherwise → calls `ParseUser.logInInBackground()`, then queries `UserInfo` table to build a `User` object with job, address, etc.
- **`getBankAccounts(User user)`**: Queries `BankAccount` table filtered by `userId`. Also auto-heals missing/inconsistent UPI IDs by regenerating them.
- **`getCreditCards(User user)`**: Queries `CreditCard` table, populates user's credit card list.
- **`getHistory()`**: Queries `History` table, builds a `Stack<History>` with income/expense flag.
- **`getUserBills()`**: Queries `Bill` table, parses date strings, populates user's bill list.
- **`getUserCredits()`**: Queries `Credit` table, populates loan list.
- **`getAllUsers(Runnable onComplete)`**: Loads ALL users from `UserInfo` (for admin panel), fetching their accounts and photos too.
- **`setupDemoUser()`**: Creates a synthetic user with hardcoded accounts, cards, and a history stack for demo/testing.

#### `SignUpActivity.java`
- **`defineJobSpinner()`**: Populates a `Spinner` with all 13 job types using their class instances.
- **`selectImage(View view)`**: Opens device gallery to pick a profile photo.
- **`signUp(View view)`**: Validates fields, calls `ParseUser.signUpInBackground()` to create the auth record, then calls `saveUserInfo()`.
- **`saveUserInfo()`**: Saves user's profile data (name, phone, address, job, credit limits, photo) to the `UserInfo` Parse table.

---

### 3. `UserInfo/` — Core Data Models

**Purpose:** Contains all the plain Java model classes (POJOs) that represent the domain objects of the app.

#### `User.java`
The central model. Holds:
- Basic info: `name`, `id`, `pass`, `phoneNumber`
- An `Address` object
- A `Job` object (determines loan limits)
- `ArrayList<CreditCard>`, `ArrayList<BankAccount>`, `ArrayList<Bill>`, `ArrayList<Credit>`
- `Stack<History>` — transaction history
- `Bitmap photo` — profile image
- `boolean userType` — false=user, true=admin (though role is managed via State Pattern)
- **`addressWrite()`**: Concatenates all address fields into a single string for display/storage.

#### `BankAccount.java`
Represents a bank account. Key methods:
- **`setBankAccountNo()`**: Generates a random 10-digit account number.
- **`generateUpiId()`**: Creates a UPI ID in the format `userId.XXXX@izbank` (last 4 digits of account number).
- **`getUpiUri(String name)`**: Generates a UPI deep-link URI (`upi://pay?pa=...&pn=...&cu=INR`) for QR code generation.

#### `CreditCard.java`
- `limit` — remaining spending limit
- `creditCardNo` — 16-character formatted card number (e.g., `**** **** **** 1234`)
- **`setCreditCardNo()`**: Generates last 4 random digits.

#### `History.java`
A single transaction record.
- `process` — human-readable string like `"UPI Pay to Alice (-₹500)"`
- `date` — Java `Date`
- `isIncome` — auto-detected by checking if process contains `"+₹"` or `"received"`
- **`getDateString()`**: Formats date as `dd/MM/yyyy`.

#### `Address.java`
A plain model: `street`, `neighborhood`, `apartmentNumber`, `floor`, `homeNumber`, `province`, `city`, `country` — with getters/setters.

#### `Admin.java`
An admin role model. Implements `UserTypeState`. The **`TypeChange(User user)`** method copies the regular user's name and history into this admin state, marking `userType=true`.

#### `UserContext.java`
Implements the **State Design Pattern** context. Holds a `UserTypeState` reference and delegates `TypeChange()` to it. This lets the splash screen switch between admin and user modes cleanly.

#### `UserTypeState.java`
An interface with a single method: `TypeChange(User user)`. Implemented by both `User` and `Admin`.

---

### 4. `Job/` — Job Types & Credit Eligibility

**Purpose:** Each job type defines the maximum loan amount, maximum installment period, and interest rate a user qualifies for. This determines how much credit they can request.

#### `Job.java` (base class)
- Fields: `name`, `maxCreditAmount`, `maxCreditInstallment`, `interestRate`
- Plain getters/setters, plus a parameterized constructor and an empty default constructor.

#### Subclasses (all follow the same pattern)
Each overrides the 4 getters with hardcoded values:

| Job | Max Credit (₹) | Max Installment (months) | Interest Rate |
|---|---|---|---|
| **Doctor** | 3,00,000 | 40 | 3% |
| **Engineer** | 3,00,000 | 40 | 4% |
| **Contractor** | 1,50,000 | 30 | 5% |
| **Driver** | 50,000 | 12 | 8% |
| **Entrepreneur** | 5,00,000 | 60 | 5% |
| **Farmer** | 1,00,000 | 24 | 4% |
| **Police** | 2,00,000 | 36 | 3% |
| **Soldier** | 2,00,000 | 36 | 3% |
| **Sportsman** | 2,00,000 | 24 | 5% |
| **Student** | 30,000 | 12 | 10% |
| **Teacher** | 1,50,000 | 36 | 4% |
| **Waiter** | 40,000 | 12 | 9% |
| **Worker** | 80,000 | 24 | 7% |

---

### 5. `Bill/` — Bill Payment Models

**Purpose:** Models for utility bill payments. Uses inheritance — `Bill` is the base class, with specific bill types extending it.

#### `Bill.java`
Base class with `type` (String), `amount` (int), `date` (custom `Date` object). Has 3 constructors and standard getters/setters. Implements `Serializable`.

#### `Date.java`
A simple custom date model (not `java.util.Date`) with `day`, `month`, `year` as Strings. Used to store and display bill dates as `dd/MM/yyyy`.

#### Subclasses: `ElectricBill`, `GasBill`, `InternetBill`, `PhoneBill`, `WaterBill`
Each has a hardcoded `type` string (e.g., `"Electric Bill"`) and overrides `getType()`, `getAmount()`, `getDate()`. All implement `Serializable`. The type string is what gets displayed in the bill list and saved to the database.

---

### 6. `Credit/` — Loan Models

**Purpose:** Models for the loan/credit system.

#### `Credit.java`
Holds loan details:
- `amount` — loan principal
- `installment` — number of months to repay
- `interestRate` — annual interest percentage
- `payAmount` — total amount to be repaid (principal + interest, pre-calculated)

#### `CustomEventListener.java`
A simple interface: `void MyEventListener()`. Used by `CreditFragment` to notify `CreditAdapter` when a credit is paid off, triggering a list refresh.

---

### 7. `Adapters/` — RecyclerView Data Binders

**Purpose:** Each adapter binds a specific data model list to a `RecyclerView`, handling how each item is displayed and what happens when clicked.

#### `AnalysisCarouselAdapter.java`
Drives the swipeable financial analysis carousel on the home screen.
- **`AnalysisPage`** (inner class): A page can be type 0 (Line Chart), 1 (Pie Chart), or 2 (Text Summary).
- **`setupLineChart()`**: Configures an MPAndroidChart line chart — smooth bezier curves, blue fill, no axes shown.
- **`setupPieChart()`**: Configures a donut pie chart with percentages, colored slices, and entry animations.
- **`setupTextSummary()`**: Shows a large stat number and a label (used for "Total Account Value").

#### `BankSelectionAdapter.java`
A selection list of bank accounts (used in payment flows and QR code screen).
- Tracks `selectedPosition` to show a checkmark on the selected account.
- Calls `OnAccountSelectedListener.onAccountSelected()` callback when user taps an account.

#### `BillAdapter.java`
Displays paid bills as cards — shows bill type, amount in Rs, and formatted date.

#### `CreditAdapter.java`
Displays active loans as cards.
- **`payCredit()`**: Finds the user's richest bank account, deducts the loan `payAmount`, deletes the loan from the Parse DB, adds a history entry, and removes it from the list.
- **`deleteCreditFromDatabase()`**: Queries `Credit` table by matching all 4 loan fields, then deletes.
- **`updateBankAccount()`**, **`accountsToDatabase()`**: Delete-and-recreate pattern to update bank balance in Parse.

#### `HistoryAdapter.java`
Simple adapter — shows transaction description text and date for each history entry.

#### `MyBankAccountAdapter.java`
Displays bank account cards (account number, balance, UPI ID).
- **`showAccountDetailsDialog()`**: On click, shows an `AlertDialog` with full account details and copy-to-clipboard buttons for account number and UPI ID.

#### `MyCreditCardAdapter.java`
Displays credit cards with formatted 16-digit card number and credit limit.
- On click, shows a dialog to **pay off credit card debt** — user selects which bank account to debit and enters amount, which reduces their bank balance and increases card limit.
- **`updateCreditCards()`**, **`updateBankAccount()`**: Both use the delete-and-recreate pattern for Parse DB updates.

#### `UserAdapter.java`
Used in the Admin Panel. Displays all registered users.
- **`showUserDetails()`**: Opens a popup with user info, bank accounts list, and admin action buttons.
- **`showAddAccountDialog()`** → **`createNewAccount()`**: Admin can create a new bank account for any user.
- **`showEditAccountDialog()`** → **`showEditBalanceDialog()`** → **`updateAccountBalanceInDb()`**: Admin can edit a user's account balance directly.
- **`deleteAccount()`**: Admin can remove a bank account from a user.
- **`deleteUserCompletely()`**: Deletes the `UserInfo` record, all their `BankAccount` records, and removes from the local list.

---

### 8. `MainScreen/` — Screens & UI Flows

**Purpose:** All the Activities and Fragments that make up the main app interface after login.

#### `MainScreenActivity.java`
The shell of the app. Hosts the bottom navigation bar and 5 fragments.
- **`onCreate()`**: Sets up all 5 fragments using `FragmentManager`, all pre-added but hidden (efficient tab switching with no re-creation).
- **`setupPaymentNotifications()`**: Connects to Parse LiveQuery WebSocket to listen for new income `History` records for the current user.
- **`startPollingFallback()`**: If LiveQuery fails, polls the `History` table every 5 seconds for new income entries.
- **`handleNewTransaction()`**: On new income, shows an in-app popup AND an Android system notification.
- **`showPaymentReceivedPopup()`**: Displays an animated "Money Received 💰" dialog.

#### `AccountFragment.java` _(Tab 1 — Home)_
The main dashboard.
- **`refreshDataFromServer()`**: On every resume, re-fetches bank accounts, history, and profile photo from Parse to ensure data is always up to date.
- **`updateUI()`**: Sets up both RecyclerViews (credit cards + bank accounts), sets the greeting name, profile photo, and triggers carousel setup.
- **`setupCarousel()`**: Builds the `AnalysisCarouselAdapter` pages — currently shows the total portfolio value as a text summary card.
- **`click()`**: Wires up button interactions:
  - History icon → shows full transaction history in a fullscreen dialog
  - "+" bank account → validates ₹1000 minimum, creates account and saves to DB
  - Profile photo → navigates to Settings tab
  - Send/Request Money → navigates to UPI tab

#### `CreditFragment.java` _(Tab 2 — Loans)_
Loan management screen.
- **`showCreditPopup()`** (Step 1): User enters loan amount and number of installments. Validates against their job's credit limits.
- **`showConfirmPopup()`** (Step 2): Calculates total repayment using simple interest formula: `Total = Principal + (Principal × Rate × Months) / 1200`. Shows monthly installment. User picks which bank account receives the funds.
- **`receiveCredit()`**: Adds loan amount to selected bank account, saves `Credit` to DB, logs history.

#### `UPIFragment.java` _(Tab 3 — UPI Payments)_
The UPI hub with 3 actions:
- **Scan QR** → launches `CaptureActivityPortrait` (ZXing scanner), then on success parses the UPI URI and goes to `ConfirmPaymentActivity`.
- **My QR** → opens `MyQRCodeActivity`.
- **Pay by UPI ID** → opens `PayToUpiActivity`.
- **Recent history** — shows last 5 UPI-related transactions (filtered by keyword "upi" or "scan").
- **`filterUPIHistory()`**: Pops/pushes history stack to filter without permanently destroying it.

#### `BillFragment.java` _(Tab 4 — Bills)_
Utility bill payment screen.
- **`showBillPopup()`**: Shows a dialog with bill type spinner (Electric, Gas, Internet, Phone, Water) + amount input + source account spinner.
- **`processBillPayment()`**: Validates sufficient balance, deducts from the chosen bank account (updates DB), creates the appropriate bill subclass, saves to DB, adds history entry.

#### `SettingFragment.java` _(Tab 5 — Profile/Settings)_
User profile management.
- **`populateUserData()`**: Displays current name, job, and email (`userId@izbank.com`).
- **`updateProfile()`**: Saves new name and selected job (with job-specific credit limits) to Parse `UserInfo`. Also updates password via `ParseUser` if provided.
- **`savePhotoToDb(Bitmap bitmap)`**: Compresses photo → saves as `ParseFile` → links to `UserInfo` record.
- **`logOut()`**: Calls `ParseUser.logOutInBackground()` and clears the back stack to return to `SignIn`.
- **`deleteAccount()`**: Deletes the `ParseUser` auth record.

#### `AdminPanelActivity.java`
The admin-only screen. Accessed via user ID `"9999"`.
- **`fetchUsers()`**: Loads all users from `UserInfo`, along with their bank accounts and credit cards (via `fetchBankAccounts()` and `fetchCreditCards()`).
- **`fetchAdminHistory()`**: Queries the entire global `History` table (all users' transactions) and shows it in a popup.
- **`showHistoryDialog()`**: Displays all system-wide transaction history in a `RecyclerView` dialog.

#### `PayToUpiActivity.java`
Manual UPI payment by typing a UPI ID.
- **`verifyUpiId()`**: Queries `BankAccount` table to check if the UPI ID exists, then fetches the recipient's name from `UserInfo`, constructs a UPI URI, and navigates to `ConfirmPaymentActivity`.

#### `ConfirmPaymentActivity.java`
The secure transfer flow. Called from both QR scan and manual UPI ID entry.
- **`parseUpiUri()`**: Parses `pa` (UPI ID), `pn` (recipient name), `am` (amount) from the `upi://pay?...` URI.
- **`showBankSelectionSheet()`**: Shows a bottom sheet with the user's accounts to choose source.
- **`startSecureTransferProtocol()`**: Validates amount > 0 and > balance.
- **`verifyRecipientConnection()`**: Fetches recipient's account from DB first (to verify existence before any money moves).
- **`deductFromSender()`**: Re-verifies balance in DB (not just in-memory), deducts, saves.
- **`creditRecipientAndVerify()`**: Adds amount to recipient's DB balance, then logs history for both sender and recipient.
- **`logSenderHistory()`** / **`logRecipientHistory()`**: Creates `History` records with `isIncome=false`/`true` flags for notification system.
- On success, launches `PaymentSuccessActivity`.

#### `PaymentSuccessActivity.java`
A success animation screen after payment.
- Shows a "Processing…" spinner for 0.8 seconds, then reveals a checkmark with a **bounce animation** (loaded from `R.anim.bounce`).
- Shows amount and recipient name. A "Done" button calls `finish()`.

#### `MyQRCodeActivity.java`
Generates and shares the user's UPI QR code.
- **`generateQRCode(BankAccount account)`**: Uses ZXing `QRCodeWriter` to encode the UPI URI string (`upi://pay?pa=userId.XXXX@izbank&pn=Name&cu=INR`) into a 512×512 pixel `Bitmap` QR code.
- **`saveAndShareQR()`**: Inflates a custom branded share layout, renders it to a `Bitmap` using `Canvas`, and saves to the device gallery via `MediaStore`.
- **`setupRecyclerView()`**: Shows a `BankSelectionAdapter` to switch which account's QR is displayed.

#### `UPIHistoryActivity.java`
Full-screen list of all UPI transactions.
- Filters `mainUser.getHistory()` stack by entries containing `"upi"` or `"scan"` keywords.

#### `CaptureActivityPortrait.java`
A one-liner class that extends ZXing's `CaptureActivity` to force the QR scanner to stay in portrait orientation (preventing screen rotation during scanning).

---

### 9. `database/` — Backend Initialization

#### `ParseStarterClass.java`
Extends `Application` — Android's global app class, called before any Activity.
- **`onCreate()`**: Initializes the Parse SDK with the Back4App `applicationId`, `clientKey`, and server URL. This must run before any Parse operations anywhere in the app.

---

## 🗄️ Parse Database Schema (Back4App)

| Table | Key Fields | Purpose |
|---|---|---|
| `_User` (ParseUser) | `username`, `password` | Authentication |
| `UserInfo` | `username`, `userRealName`, `phone`, `address`, `job`, `maxCreditAmount`, `interestRate`, `images` | User profile |
| `BankAccount` | `accountNo`, `userId`, `cash`, `upiId` | Bank accounts |
| `CreditCard` | `creditCardNo`, `userId`, `limit` | Credit cards |
| `Bill` | `username`, `type`, `amount`, `date` | Paid utility bills |
| `Credit` | `username`, `amount`, `installment`, `interestRate`, `payAmount` | Active loans |
| `History` | `userId`, `process`, `date`, `isIncome` | All transactions (used for notifications) |

---

## 🔄 App Flow Summary

```
Launch → SignIn → [parse credentials] → splashScreen (role check)
                                              ↓
                              Admin (9999) → AdminPanelActivity
                              User → MainScreenActivity
                                         ├── Tab 1: AccountFragment (Home/Dashboard)
                                         ├── Tab 2: CreditFragment (Loans)
                                         ├── Tab 3: UPIFragment → Scan QR / Pay by ID / My QR
                                         ├── Tab 4: BillFragment (Utility Bills)
                                         └── Tab 5: SettingFragment (Profile/Logout)
```

---

## 🧩 Design Patterns Used

| Pattern | Where Used |
|---|---|
| **State Pattern** | `UserContext`, `UserTypeState`, `User`, `Admin` — for role-based routing |
| **Adapter Pattern** | All RecyclerView adapters in `Adapters/` |
| **Singleton** | `SignIn.mainUser` — global user object accessed throughout the app |
| **Observer/Callback** | `CustomEventListener`, `OnAccountSelectedListener`, Parse callbacks |
| **Template Method** | `Bill` base class with subclasses overriding `getType()` |
