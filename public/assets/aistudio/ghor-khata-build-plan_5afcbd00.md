# Ghor Khata: Family Household Tracker

Build plan for Google AI Studio. Mobile-first web app (PWA) for tracking refills, farm milk and bills for the family, with a calendar view and shared data.

---

## 1. Overview

**Stack:** React web app (AI Studio Build), Firebase Firestore + Firebase Auth (Google sign-in), installable PWA.

**Family sync:** one person creates a Household and gets a 6-digit invite code. Others sign in with Google, enter the code, and everyone sees the same data in real time.

**Language:** English and বাংলা, switchable in Settings. Currency ৳, dates dd/mm/yyyy.

---

## 2. Screens

| Screen | What it does |
|---|---|
| **Home** | Status cards: each refill item (last bought, runs out in ~N days), Milk this month (bottles, cost, paid, due), bills due soon |
| **Calendar** | Month view with colored dots: orange = refill, blue = milk pickup, green = milk payment, red = bill due (grey once paid). Tapping a day opens its entries plus an Add button |
| **Quick Add (+)** | Choose Refill / Milk pickup / Milk payment / Bill payment. Date defaults to today |
| **Milk** | Log pickups, payments and skipped days. Monthly summary with carry-over |
| **Bills** | Recurring bills with paid/unpaid status for each month |
| **Settings** | Items, milk price per bottle, household and invite code, language toggle, install app |

### Home card example
- Gas: bought 23 days ago · usually lasts ~30 days · **runs out in ~7 days**
- Milk (Nov): 18 bottles · ৳1,620 · paid ৳1,000 · **due ৳620**
- Electricity bill due in 3 days

---

## 3. Logic

- **Runs out in:** average gap between the last 3 purchases of an item. With only 1 purchase, show "last bought X days ago" only. Orange when ≤5 days are left, red when overdue.
- **Milk pickup default:** days since the last pickup, minus skipped days. Always editable.
- **Milk monthly:** total bottles × price per bottle = cost. Due = cost + previous unpaid amount − paid.
- **Bills:** unpaid bills due within 5 days show on Home. Overdue ones show in red.

---

## 4. Data model (Firestore)

```
households/{householdId}
  name, inviteCode, members[]
  /items         name, nameBn, category, unit, usualPrice
  /purchases     itemId, date, qty, price, addedBy, note
  /milkPickups   date, bottles, addedBy
  /milkPayments  date, amount, addedBy
  /milkSkips     date
  /bills         name, nameBn, amount, dueDay, active
  /billPayments  billId, month (YYYY-MM), amount, paidDate, addedBy
  /settings      milkPricePerBottle
users/{uid}
  name, householdId, language
```

---

## 5. Before you build: Firebase setup (free tier)

1. Go to console.firebase.google.com and create a project.
2. Build → **Firestore Database** → Create (production mode).
3. Build → **Authentication** → Sign-in method → enable **Google**.
4. Project settings → Add app → **Web**, then copy the `firebaseConfig`.
5. Paste the config into the prompt below where it says `[PASTE FIREBASE CONFIG]`.
6. After deploying, add the app's domain under Authentication → Settings → Authorized domains.

---

## 6. Combined prompt (paste into AI Studio)

```
Build a mobile-first React web app called "Ghor Khata" for a family in Bangladesh to track household refills, farm milk deliveries and bills, with a calendar view and shared family data.

GENERAL
- All storage goes through one data service file (dataService.ts). Components never touch storage directly.
- Backend: Firebase Firestore with real-time listeners and offline persistence, plus Firebase Auth with Google sign-in.
- i18n from the start: every UI string comes from en.json and bn.json, with an English/বাংলা toggle in Settings saved per user.
- Currency ৳, dates dd/mm/yyyy.
- Clean, minimal design with large tap targets and readable text, because older family members will use it.
- Bottom nav: Home, Calendar, Milk, Bills, Settings. Floating + button for quick add on every screen.

AUTH & HOUSEHOLD
- On first login: "Create household" (generates a 6-digit invite code) or "Join household" (enter the code).
- All data lives under households/{householdId}/... Every entry stores addedBy (user's name), shown small on each entry.
- Write Firestore security rules so only household members can read or write that household's data.

1. REFILLS
- Items: name, Bangla name, category, unit, usual price. Seed with Gas cylinder, Rice, Oil. Manage them in Settings.
- Log a purchase: item, date (default today), qty, price, note.
- Home card per item: "last bought X days ago". With 2+ purchases, also show average days between purchases and "runs out in ~N days" (average of the last 3 gaps). Orange when ≤5 days are left, red when overdue.

2. MILK
- Settings field: milk price per bottle.
- Log pickup: date (default today), bottle count defaulting to days since the last pickup minus skipped days (editable).
- Mark skipped days (no milk that day).
- Log payment to the farm: date, amount.
- Monthly summary with a month selector: total bottles, total cost (bottles × price), paid, due, and carry-over of unpaid amounts from previous months.
- Home card: this month's bottles, due amount, days since last pickup.

3. BILLS
- A bill has name, Bangla name, amount, due day of month, and active on/off.
- This month's bills show paid/unpaid status. Tap to mark paid (amount editable, date defaults to today).
- Unpaid bills due within 5 days show on Home. Overdue ones show in red.

4. CALENDAR
- Month view with colored dots: orange = refill purchase, blue = milk pickup, green = milk payment, red = bill due (grey once paid), grey X = milk skipped.
- Tapping a day opens a bottom sheet with that day's entries (edit/delete each) and an "Add" button pre-filled with that date.

5. PWA
- Manifest, icons, a service worker for offline loading, and an "Install app" button in Settings.

Firebase config: [PASTE FIREBASE CONFIG]
```

---

## 7. If the combined prompt breaks: build in steps

AI Studio sometimes drops features from long prompts. If that happens, start a fresh build and paste these one at a time, testing after each.

**Step 1: Core + Refills + Calendar (localStorage first)**
```
Build a mobile-first React web app called "Ghor Khata" for tracking household refills, milk deliveries and bills for a family in Bangladesh.
For now, store data in localStorage behind a single data service file (dataService.ts). Components never touch storage directly, because I will swap it to Firebase later.
Set up i18n from the start: every UI string comes from en.json and bn.json, with a language toggle in Settings. Currency ৳, dates dd/mm/yyyy.
Features:
1. Items: name, Bangla name, category, unit, usual price. Seed with Gas cylinder, Rice, Oil.
2. Log a purchase: item, date (default today), qty, price, note.
3. Home: card per item with "last bought X days ago". With 2+ purchases, show average days between purchases and "runs out in ~N days" (average of the last 3 gaps). Orange when ≤5 days are left, red when overdue.
4. Calendar: month view with dots on days that have entries. Tapping a day opens a bottom sheet with its entries and an Add button pre-filled with that date.
5. Floating + button for quick add.
Bottom nav: Home, Calendar, Milk, Bills, Settings (Milk and Bills as placeholders for now). Clean, minimal design with large tap targets, because older family members will use it.
```

**Step 2: Milk**
```
Build the Milk screen. Settings gets a "milk price per bottle" field.
- Log pickup: date (default today), bottle count defaulting to days since the last pickup minus skipped days (editable).
- Mark skipped days.
- Log payment to the farm: date, amount.
- Monthly summary with a month selector: total bottles, cost (bottles × price), paid, due, and carry-over of unpaid amounts from previous months.
- Calendar: blue dots for pickups, green for payments, grey X for skipped days.
- Home card: this month's bottles, due amount, days since last pickup.
```

**Step 3: Bills**
```
Build the Bills screen. A bill has name, Bangla name, amount, due day of month, and active on/off.
- This month's bills show paid/unpaid status. Tap to mark paid (amount editable, date defaults to today).
- Unpaid bills due within 5 days show on Home. Overdue ones show in red.
- Calendar: red dots on due dates, grey once paid.
```

**Step 4: Firebase + family sync**
```
Replace the localStorage implementation in dataService.ts with Firebase Firestore, using real-time listeners and offline persistence.
- Google sign-in with Firebase Auth.
- After first login: "Create household" (generates a 6-digit invite code) or "Join household" (enter code).
- All data lives under households/{householdId}/... Every entry stores addedBy (user's name), shown small on each entry.
- Write Firestore security rules so only household members can read or write that household's data.
Firebase config: [PASTE FIREBASE CONFIG]
```

**Step 5: Install as app**
```
Make this an installable PWA: manifest, icons, a service worker for offline loading, and an "Install app" button in Settings.
```

---

## 8. Test checklist

- [ ] Add 2 gas purchases a month apart: does Home show "runs out in"?
- [ ] Log a milk pickup 3 days after the last one: does it default to 3 bottles?
- [ ] Mark a skipped day: does the next pickup default drop by 1?
- [ ] Pay part of the milk bill: does the remaining due carry over to next month?
- [ ] Mark a bill paid: does its calendar dot turn grey?
- [ ] Second phone joins with the invite code: does a new entry show up on the first phone live?
- [ ] Switch to বাংলা: is any English text left behind?
- [ ] Turn off the internet, add an entry, then reconnect: does it sync?

---

## 9. Later (v2 ideas)

- Push reminders (gas running low, bill due)
- Monthly spending chart
- Export a month to PDF or Google Sheets
- More delivery-type items (newspaper, eggs) using the same logic as milk
