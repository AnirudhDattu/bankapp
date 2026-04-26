# UPI Feature Implementation Tracker

## Roadmap

- [x] **QR Code System**
    - [x] Add ZXing library for QR generation and scanning.
    - [x] Implement `getUpiUri()` in `BankAccount.java`.
    - [x] Create `MyQRCodeActivity` to display user's QR.
    - [x] Support switching between multiple bank accounts for QR.
- [x] **Payment Flow**
    - [x] Implement QR Scanner integration in `UPIFragment`.
    - [x] Create `PayToUpiActivity` for manual ID entry.
    - [x] Create `ConfirmPaymentActivity` to review transaction details.
    - [x] Create `BankSelectionAdapter` and BottomSheet for choosing payment source.
- [x] **Transaction Logic**
    - [x] Implement balance deduction in `ConfirmPaymentActivity`.
    - [x] Implement transaction logging in `History`.
    - [ ] Add `UpiPinDialog` for transaction authorization (Simulated security).
- [x] **UI Integration**
    - [x] Update `UPIFragment` to link all new activities.
    - [x] Implement `onResume` refresh for transaction history.
    - [x] Register all activities and permissions in `AndroidManifest.xml`.

## Current Status
- [x] Core UPI flows (Scan & Pay, Pay to ID, My QR) implemented.
- [x] Navigation and data passing between activities setup.
- [ ] Working on adding a simulated UPI PIN dialog for better realism.
