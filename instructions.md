Perfect. Below is the FULL, DETAILED, FINAL SYSTEM PLAN, retold cleanly from zero, aligned exactly with:

your device specs

your business workflow

your clarification about conversion rules being optional, not fixed, and creatable anytime

offline usage

Bluetooth ESC/POS printing

local + online backup

This is the reference version you can show to a friend, developer, or use yourself.

📌 POSApp – Complete System Plan
Offline Android POS for Construction & Delivery

(58mm Bluetooth ESC/POS Printer – Android 10)

1) Purpose of the System

Build a professional, offline-first Android POS system designed for construction and material delivery businesses, running on a small Android POS device with a built-in 58mm thermal printer.

The system is not a retail POS.
It is built for:

cement

concrete

asphalt

sand

gravel

construction materials

2) Core Principles (VERY IMPORTANT)
✅ Offline-first (non-negotiable)

The app works 100% without internet

All data is stored locally using SQLite (Room)

Sales, printing, reports, and history work offline

☁️ Online storage = safety & backup (not required to operate)

Internet is used only for backup

If internet is not available, nothing breaks

Data syncs later automatically

This matches real POS behavior.

3) Target Device Compatibility

The app is designed to fully match your device specs:

Android 10

1GB–2GB RAM

8GB–16GB storage

5.5" screen

Bluetooth 4.0+

Built-in 58mm thermal printer

ESC/POS support

UI is:

simple

fast

touch-friendly

no heavy animations

4) Printing System (LOCKED)
Printing method

Bluetooth Classic (RFCOMM / SPP)

ESC/POS command printing

Printer paired once in Android settings

Printer selected in app and MAC saved

Printing features

Test print

Auto reconnect

Graceful error handling

Optimized for 58mm paper (32-char width)

This matches exactly how your printer works today.

5) Clients (Customers) – Full Profiles

Each client has a profile that stores:

Name

Phone number

Main address

Optional email

Type: Cash / Credit

Balance (for credit customers)

Multiple delivery addresses

Each client can have:

Site 1

Site 2

Project A

Warehouse, etc.

On Delivery Authorization, you select which address is printed.

6) Client Order History & Totals (VERY IMPORTANT)

For every client, the system stores:

All receipts linked to that client

Order number

Date

Amount

Currency

Payment status

The system can always calculate:

Total orders

Total amount ordered

Total paid

Remaining balance

This is automatic from saved sales data.

7) Categories & Items
Categories

Create / edit / delete

Used to organize items

Items

Each item has:

Name

Category

Default unit

Price

Optional default tax

Active / inactive

Items are not limited to one unit.

8) Units System (Flexible)

The system supports:

kg

ton

m³

piece

custom units (bag, truck, etc.)

Important behavior

Unit is selected per line

Unit is never forced

Same item can be sold in different units on different orders

9) 🔁 Conversion Rules (KEY FEATURE – FINAL & LOCKED)
Core rule

Conversion is OPTIONAL, NOT FIXED, and USER-CONTROLLED

What this means

No item is permanently converted

Default conversion = None

Conversion is applied only when the user chooses it

Conversion rules

You can:

Create conversion rules anytime

Name them freely

Define:

From unit → To unit

Divide or Multiply

Factor

Decimals

How conversion is used

On each order line:

User selects:

None → no conversion

OR one of the saved conversion rules

Conversion applies only to that line, that order

Practical examples

Concrete:

Sometimes convert kg → m³

Sometimes leave as weight only

Asphalt:

Usually kg or ton

Conversion left as None

✔ You can create multiple conversions and choose any of them whenever you want
✔ Nothing is forced or fixed

10) Orders – One Entry, Two Prints (IMPORTANT)

You enter ONE transaction, and from it you can print:

🧾 Receipt (WITH prices)

Printed fields:

Business name, phone, location

Receipt number: R-000001

Date & time

Client name & phone

Items:

Quantity + selected unit

Optional converted quantity

Unit price

Line total

Subtotal

Discount

Tax

Total

Currency & exchange rate

Payment type

Footer text

🚚 Delivery Authorization (NO prices)

(formerly Ezen Tasleem)

Printed fields:

Business name, phone, location

Delivery Authorization number: DA-000001

Date & time

Client name & phone

Selected delivery address

Materials list:

Quantity + unit

Optional converted quantity

Driver name

Truck plate number

Weights:

Empty

Full

Net (auto calculated)

Signature lines (driver & receiver)

❌ No prices
❌ No totals

11) Drivers & Trucks
Drivers

Name

Optional phone

Select from dropdown

Trucks

Plate number

Optional notes

Select from dropdown

Required for Delivery Authorization.

12) Quick Text Print (Very Practical Feature)

A special screen to:

Paste any text

Print it instantly on 58mm paper

Use cases:

Text sent from iPhone

Notes

Announcements

Instructions

Features:

Multiline text box

Print button

Clear button

Uses same Bluetooth ESC/POS printer

No database save (v1)

13) Document Numbering System

Separate numbering sequences:

Receipts: R-000001

Delivery Authorization: DA-000001

Reset options:

Never

Yearly

Monthly

Numbers are:

Automatic

Unique

Never duplicated

14) Reports & Export
Reports

Sales history

Filter by date/type

Reprint any document

Daily summary

Monthly summary

Export

Export receipts to CSV

Export delivery authorizations to CSV

Export customers & items

Export Everything

One ZIP file containing:

Database file

All CSV exports

15) Data Safety & Backup (Local + Online)
Local backup (offline)

Automatic daily backup

Stored on device

Keep last N days (default 14)

Manual backup option

Online backup (cloud storage)

Optional

When internet is available:

Upload latest backup or ZIP

Purpose:

Protect data if device is lost/broken

Possible storage:

Google Drive (simple)

Your own server (advanced)

16) App Screens (Final)

Setup Wizard

Dashboard

Customers & delivery addresses

Categories

Items

Units manager

Conversion rules manager

Drivers manager

Trucks manager

New Sale (single entry)

Print Receipt / Print Delivery Authorization / Print Both

Quick Text Print

Sales History & Reprint

Reports & Export

Settings (business, printer, numbering, backup, cloud)

17) Technology Stack

Kotlin

Jetpack Compose

Room (SQLite)

MVVM

Coroutines

Bluetooth ESC/POS

✅ Final One-Line Summary

An offline Android construction POS system with client profiles, full order history, flexible units, optional user-controlled conversion rules, dual printing (receipt + delivery authorization), quick text printing, and strong local + online data backup — fully compatible with a 58mm Bluetooth ESC/POS Android POS device.