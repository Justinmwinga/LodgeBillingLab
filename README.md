# Lodge Billing Lab

## Project Overview

Lodge Billing Lab is a JavaFX desktop application for managing lodge guest bills. It includes a simple login/logout system, guest billing, discount calculation, payment tracking, saved bills, and receipt viewing. The interface uses a custom cocoa-and-tan theme (`styles.css`) with bundled Lora and Poppins fonts and three round lodge photos.

## Documentation

A full illustrated guide to the code, rules, design and testing is in `docs/LodgeBillingLab_Project_Guide.pdf`.

## Technologies

- Java 21
- JavaFX 21.0.12
- Gradle
- JavaFX Controls
- Java Module System

## Login System

The application starts with a login screen.

### Demo credentials

```text
Username: admin
Password: admin123
```

The user must successfully authenticate before accessing the lodge billing system.

### Login requirements

- Username field
- Password field
- Login button
- Invalid-login message
- Password field is cleared after an unsuccessful login

### Logout

The main application provides a **Log out** button and a **File > Log out** menu option.

When logout is selected:

1. A confirmation dialog appears.
2. The user can confirm or cancel.
3. Confirming returns the application to the login screen.
4. The billing interface is no longer accessible until the user logs in again.

> Note: This is an educational/demo authentication system. The username and password are stored directly in the source code and are not suitable for production security.

## Main Billing Features

- Enter guest name
- Select Standard, Deluxe, or Family room
- Enter number of nights
- Enter extras
- Enter amount paid
- Add notes
- Calculate total
- Apply 2% room-charge discount for 3 or more nights
- Calculate outstanding balance
- Save bills
- View saved bills
- View receipts
- Delete selected bills
- Start a new guest entry
- Log out
- Exit the application

## Interface Updates (v1.3)

- Cocoa-and-tan theme in `styles.css`: split-panel login, page header with user and Log out chips, solid cards with soft shadows, geometric triangle accent, round photo column
- Bundled fonts (Poppins for the interface, Lora for titles) so the look is the same on every computer
- Live bill summary: room charge, discount, extras, total, paid and balance update as you type
- Totals strip: bills saved, total billed, outstanding balance
- Saved-bills table with right-aligned amounts and a Paid / Owing / Overpaid status
- Receipt on cream paper with the full breakdown, date and notes (double-click a row to open it)
- Inline validation messages instead of pop-up error dialogs
- Number fields only accept digits; amount fields select their contents on focus
- Confirmation before deleting a bill
- Shortcuts: Ctrl+N (new guest), Ctrl+S (save bill)
- Amounts are rounded to two decimal places and shown with thousands separators

## Room Prices

| Room Type | Price per Night |
|---|---:|
| Standard | K350.00 |
| Deluxe | K550.00 |
| Family | K750.00 |

## Billing Rules

```text
Room Charge = Room Price × Number of Nights

If nights >= 3:
    Discount = Room Charge × 2%
    Discounted Room Charge = Room Charge - Discount

Total = Discounted Room Charge + Extras

Balance = Total - Amount Paid
```

Extras do not receive the 2% discount.

## Example

```text
Guest: John Phiri
Room: Deluxe
Nights: 3
Extras: K200
Amount Paid: K1000
```

```text
Room charge:
K550 × 3 = K1650

Discount:
K1650 × 2% = K33

Discounted room charge:
K1650 - K33 = K1617

Total:
K1617 + K200 = K1817

Balance:
K1817 - K1000 = K817
```

## Project Structure

```text
LodgeBillingLab/
├── build.gradle
├── settings.gradle
└── src/
    └── main/
        └── java/
            ├── module-info.java
            └── com/
                └── example/
                    └── lodgebilling/
                        └── Main.java
    └── resources/
        └── com/
            └── example/
                └── lodgebilling/
                    ├── styles.css
                    ├── images/        photo1.png, photo2.png, photo3.png
                    └── fonts/         Poppins-Regular, Poppins-Bold, Lora-Variable
```

## Requirements

- JDK 21
- Gradle or Gradle Wrapper
- Internet access the first time Gradle downloads JavaFX dependencies

## Running the Project

### Windows

Open PowerShell in the project directory:

```powershell
gradlew run
```

If the Gradle Wrapper does not exist:

```powershell
gradle wrapper
gradlew run
```

### Linux/macOS

```bash
./gradlew run
```

## Application Controls

### Login

Authenticates the user before the billing system opens.

### New Guest

Clears the billing form.

### Calculate

Calculates the total and balance without saving the bill.

### Save Bill

Creates a bill and adds it to the saved-bills table.

### View Receipt

Displays the selected bill as a receipt.

### Delete Selected Bill

Deletes the selected bill from the current session.

### Log out

Returns the user to the login screen.

### Exit

Closes the application.

### About

Displays information about the application.

## Data Storage

Bills are stored in an in-memory `ObservableList`.

Therefore:

- Bills remain available while the application is running.
- Bills can be added, viewed, and deleted.
- Closing the application removes the current bills.
- There is currently no database or permanent user-account storage.

## OOP Concepts Demonstrated

- Classes and objects
- Encapsulation
- Private fields
- Constructors
- Getter methods
- Methods
- Conditional statements
- Switch expressions
- Exception handling
- Collections
- JavaFX event handling
- JavaFX GUI controls
- JavaFX tables
- Java modules

## Testing Checklist

- [ ] Login screen appears when the application starts
- [ ] Correct username/password logs in
- [ ] Incorrect credentials are rejected
- [ ] Password is cleared after failed login
- [ ] Billing screen opens after login
- [ ] Guest information can be entered
- [ ] Room type can be selected
- [ ] Calculate works
- [ ] 2% discount works for 3 or more nights
- [ ] Extras are not discounted
- [ ] Balance is correct
- [ ] Save Bill works
- [ ] Saved bill appears in the table
- [ ] View Receipt works
- [ ] Delete Selected Bill works
- [ ] New Guest works
- [ ] Log out confirmation appears
- [ ] Confirming logout returns to login
- [ ] Billing screen cannot be accessed without logging in
- [ ] File menu works
- [ ] Help/About works
- [ ] Summary card updates while typing
- [ ] Totals strip updates after saving and deleting a bill
- [ ] Receipt shows discount, extras, notes and status
- [ ] Delete asks for confirmation
- [ ] Ctrl+N and Ctrl+S work
- [ ] Fonts load (Lora titles, Poppins text) and the three round photos show on the login and main screens

## Author

ICT261 Java/JavaFX Practical Lab

## Version

Version 1.3

## Educational Note

The login system is included for practical/educational purposes. For a production application, credentials should not be hard-coded. A secure authentication system would use hashed passwords, protected configuration or a database, session management, and appropriate access controls.
