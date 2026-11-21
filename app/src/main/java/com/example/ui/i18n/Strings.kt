package com.example.ui.i18n

object Strings {

    fun get(key: String, isBn: Boolean): String {
        return if (isBn) (bn[key] ?: en[key] ?: key) else (en[key] ?: key)
    }

    fun toBengaliDigits(input: String): String {
        val bnDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')
        val sb = StringBuilder()
        for (ch in input) {
            if (ch in '0'..'9') {
                sb.append(bnDigits[ch - '0'])
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    fun formatNumber(number: Number, isBn: Boolean): String {
        val str = when (number) {
            is Double -> if (number % 1.0 == 0.0) number.toLong().toString() else String.format(java.util.Locale.US, "%.1f", number)
            is Float -> if (number % 1.0f == 0.0f) number.toLong().toString() else String.format(java.util.Locale.US, "%.1f", number)
            else -> number.toString()
        }
        return if (isBn) toBengaliDigits(str) else str
    }

    fun formatCurrency(amount: Double, isBn: Boolean): String {
        val formatted = if (amount % 1.0 == 0.0) {
            String.format(java.util.Locale.US, "%,d", amount.toLong())
        } else {
            String.format(java.util.Locale.US, "%,.1f", amount)
        }
        return if (isBn) "৳${toBengaliDigits(formatted)}" else "৳$formatted"
    }

    fun formatDate(dateStr: String, isBn: Boolean): String {
        // incoming format: "yyyy-MM-dd"
        val parts = dateStr.split("-")
        if (parts.size == 3) {
            val dmy = "${parts[2]}/${parts[1]}/${parts[0]}"
            return if (isBn) toBengaliDigits(dmy) else dmy
        }
        return if (isBn) toBengaliDigits(dateStr) else dateStr
    }

    private val en = mapOf(
        "app_title" to "Ghor Khata",
        "app_subtitle" to "Family Household Tracker",
        "nav_home" to "Home",
        "nav_calendar" to "Calendar",
        "nav_milk" to "Milk",
        "nav_bills" to "Bills",
        "nav_settings" to "Settings",

        // Quick Add
        "quick_add_title" to "Quick Add",
        "quick_add_refill" to "Refill Purchase",
        "quick_add_milk_pickup" to "Milk Pickup",
        "quick_add_milk_payment" to "Milk Payment",
        "quick_add_bill_payment" to "Bill Payment",
        "select_item" to "Select Item",
        "date" to "Date",
        "quantity" to "Quantity",
        "price" to "Price",
        "amount" to "Amount",
        "bottles" to "Bottles",
        "added_by" to "Added By",
        "note_optional" to "Note (optional)",
        "save" to "Save",
        "cancel" to "Cancel",
        "delete" to "Delete",
        "edit" to "Edit",

        // Home
        "greeting" to "Family Ledger",
        "household_code" to "Invite Code",
        "refills_heading" to "Household Refills",
        "refills_sub" to "Refill countdowns & usage tracker",
        "milk_heading" to "Farm Milk Status",
        "bills_heading" to "Bills Due Soon",
        "recent_activity" to "Recent Family Activity",
        "last_bought_days_ago" to "Last bought %s days ago",
        "usually_lasts" to "Usually lasts ~%s days",
        "runs_out_in" to "Runs out in ~%s days",
        "overdue" to "Overdue by %s days!",
        "runs_out_today" to "Runs out today!",
        "days_left" to "%s days left",
        "good" to "In Stock",
        "low" to "Low Stock",
        "critical" to "Overdue",
        "only_one_purchase" to "1 purchase logged so far",
        "log_refill_btn" to "Log Refill",

        // Milk
        "milk_summary_title" to "Monthly Summary",
        "total_bottles" to "Total Bottles",
        "cost" to "Cost",
        "paid" to "Paid",
        "due" to "Due",
        "net_due" to "Net Due",
        "carry_over" to "Previous Due",
        "bottle_price" to "Price / Bottle",
        "days_since_pickup" to "Days since last pickup",
        "log_pickup" to "Log Pickup",
        "mark_skip" to "Mark Skip",
        "pay_farm" to "Pay Farm",
        "pickups_and_skips" to "Pickups & Skips",
        "payments_history" to "Payments to Farm",
        "milk_skipped_day" to "Skipped (No milk delivered)",
        "bottles_count" to "%s bottles",
        "no_pickups_month" to "No pickups logged for this month",
        "no_payments_month" to "No payments logged for this month",

        // Bills
        "bills_summary" to "Monthly Bills Overview",
        "total_bills" to "Total Bills",
        "total_paid" to "Total Paid",
        "total_pending" to "Total Due",
        "due_on" to "Due on %s of month",
        "due_in_days" to "Due in %s days",
        "overdue_by_days" to "Overdue by %s days!",
        "paid_on" to "Paid on %s",
        "mark_paid" to "Mark Paid",
        "add_new_bill" to "Add Recurring Bill",
        "bill_name" to "Bill Name",
        "bill_name_bn" to "Bangla Name",
        "bill_amount" to "Amount (৳)",
        "due_day" to "Due Day of Month (1-31)",
        "active_status" to "Active for tracking",

        // Calendar
        "month_jan" to "January",
        "month_feb" to "February",
        "month_mar" to "March",
        "month_apr" to "April",
        "month_may" to "May",
        "month_jun" to "June",
        "month_jul" to "July",
        "month_aug" to "August",
        "month_sep" to "September",
        "month_oct" to "October",
        "month_nov" to "November",
        "month_dec" to "December",
        "legend_refill" to "Refill",
        "legend_pickup" to "Milk Pickup",
        "legend_payment" to "Milk Payment",
        "legend_bill_due" to "Bill Due",
        "legend_bill_paid" to "Bill Paid",
        "legend_skip" to "Milk Skip",
        "entries_for_day" to "Entries for %s",
        "no_entries_day" to "No entries for this date",
        "add_for_this_date" to "Add Entry for This Date",

        // Settings
        "settings_title" to "Household Settings",
        "language_setting" to "Language / ভাষা",
        "bengali_numerals" to "Bengali Numerals (১,২,৩)",
        "household_management" to "Household Management",
        "household_name" to "Household Name",
        "copy_code" to "Copy Code",
        "code_copied" to "Code copied to clipboard!",
        "family_members" to "Family Members",
        "add_member" to "Add Member",
        "active_user" to "Current Device User",
        "milk_settings" to "Farm Milk Settings",
        "milk_bottle_rate" to "Milk Price per Bottle (৳)",
        "manage_refill_items" to "Manage Refill Items",
        "add_refill_item" to "Add New Item",
        "join_household" to "Join Another Household",
        "create_household" to "Create New Household",
        "enter_code" to "Enter 6-digit Code",
        "join" to "Join",
        "create" to "Create",
        "reset_data" to "Reset to Default Demo Data",
        "reset_confirm" to "Reset all data to default demo state?",
        "app_info" to "Ghor Khata v1.0 • Bangladesh Family Ledger"
    )

    private val bn = mapOf(
        "app_title" to "ঘর খাতা",
        "app_subtitle" to "পারিবারিক হিসাব ও ট্র্যাকার",
        "nav_home" to "হোম",
        "nav_calendar" to "ক্যালেন্ডার",
        "nav_milk" to "দুধের হিসাব",
        "nav_bills" to "বিল সমূহ",
        "nav_settings" to "সেটিংস",

        // Quick Add
        "quick_add_title" to "দ্রুত হিসাব যোগ করুন",
        "quick_add_refill" to "পণ্য কেনাকাটা / রিফিল",
        "quick_add_milk_pickup" to "দুধ সংগ্রহ",
        "quick_add_milk_payment" to "দুধের বিল পরিশোধ",
        "quick_add_bill_payment" to "বিল পরিশোধ",
        "select_item" to "পণ্য বেছে নিন",
        "date" to "তারিখ",
        "quantity" to "পরিমাণ",
        "price" to "মূল্য",
        "amount" to "টাকার পরিমাণ",
        "bottles" to "বোতল সংখ্যা",
        "added_by" to "কে খরচ করেছেন / লিখেছেন",
        "note_optional" to "মন্তব্য (ঐচ্ছিক)",
        "save" to "সংরক্ষণ করুন",
        "cancel" to "বাতিল",
        "delete" to "মুছে ফেলুন",
        "edit" to "সম্পাদনা",

        // Home
        "greeting" to "ঘরের হিসাব খাতা",
        "household_code" to "ইনভাইট কোড",
        "refills_heading" to "ঘরের রিফিল ট্র্যাকার",
        "refills_sub" to "কোন পণ্য কবে ফুরিয়ে যাবে তার হিসাব",
        "milk_heading" to "এই মাসের দুধের হিসাব",
        "bills_heading" to "আসন্ন ও বকেয়া বিল",
        "recent_activity" to "সাম্প্রতিক পারিবারিক এন্ট্রি",
        "last_bought_days_ago" to "%s দিন আগে কেনা হয়েছে",
        "usually_lasts" to "সাধারণত প্রায় ~%s দিন চলে",
        "runs_out_in" to "~%s দিনের মধ্যে ফুরাবে",
        "overdue" to "%s দিন আগেই ফুরিয়ে গেছে!",
        "runs_out_today" to "আজকেই ফুরিয়ে যাবে!",
        "days_left" to "%s দিন বাকি",
        "good" to "মজুদ আছে",
        "low" to "দ্রুত ফুরাবে",
        "critical" to "ফুরিয়ে গেছে",
        "only_one_purchase" to "এখন পর্যন্ত ১ বার কেনা হয়েছে",
        "log_refill_btn" to "নতুন কেনাকাটা লিখুন",

        // Milk
        "milk_summary_title" to "মাসিক দুধের বিবরণী",
        "total_bottles" to "মোট বোতল",
        "cost" to "মোট খরচ",
        "paid" to "পরিশোধিত",
        "due" to "বকেয়া",
        "net_due" to "সর্বমোট পাওনা",
        "carry_over" to "আগের মাসের বকেয়া",
        "bottle_price" to "প্রতি বোতল দর",
        "days_since_pickup" to "শেষ দুধ নেওয়ার পর দিন",
        "log_pickup" to "দুধ সংগ্রহ লিখুন",
        "mark_skip" to "ছুটি (দুধ নেননি)",
        "pay_farm" to "গোয়ালাকে বিল দিন",
        "pickups_and_skips" to "সংগ্রহ ও ছুটির তালিকা",
        "payments_history" to "পরিশোধের ইতিহাস",
        "milk_skipped_day" to "দুধ নেওয়া হয়নি (ছুটি)",
        "bottles_count" to "%s বোতল",
        "no_pickups_month" to "এই মাসে এখনো কোনো দুধ এন্ট্রি নেই",
        "no_payments_month" to "এই মাসে কোনো পরিশোধের এন্ট্রি নেই",

        // Bills
        "bills_summary" to "এই মাসের বিলের সারসংক্ষেপ",
        "total_bills" to "মোট বিল সংখ্যা",
        "total_paid" to "পরিশোধিত বিল",
        "total_pending" to "বকেয়া বিল",
        "due_on" to "মাসের %s তারিখে পরিশোধযোগ্য",
        "due_in_days" to "%s দিনের মধ্যে দিতে হবে",
        "overdue_by_days" to "%s দিন অতিক্রম হয়েছে!",
        "paid_on" to "পরিশোধের তারিখ: %s",
        "mark_paid" to "পরিশোধ লিখুন",
        "add_new_bill" to "নতুন নিয়মিত বিল যোগ করুন",
        "bill_name" to "বিলের নাম (ইংরেজি)",
        "bill_name_bn" to "বিলের নাম (বাংলা)",
        "bill_amount" to "বিলের পরিমাণ (৳)",
        "due_day" to "মাসের কত তারিখে দিতে হয় (১-৩১)",
        "active_status" to "ট্র্যাকিং সক্রিয় আছে",

        // Calendar
        "month_jan" to "জানুয়ারি",
        "month_feb" to "ফেব্রুয়ারি",
        "month_mar" to "মার্চ",
        "month_apr" to "এপ্রিল",
        "month_may" to "মে",
        "month_jun" to "জুন",
        "month_jul" to "জুলাই",
        "month_aug" to "আগস্ট",
        "month_sep" to "সেপ্টেম্বর",
        "month_oct" to "অক্টোবর",
        "month_nov" to "নভেম্বর",
        "month_dec" to "ডিসেম্বর",
        "legend_refill" to "রিফিল/কেনা",
        "legend_pickup" to "দুধ নেওয়া",
        "legend_payment" to "দুধের টাকা",
        "legend_bill_due" to "বিল বাকি",
        "legend_bill_paid" to "বিল পরিশোধ",
        "legend_skip" to "দুধ বন্ধ",
        "entries_for_day" to "%s তারিখের হিসাব",
        "no_entries_day" to "এই তারিখে কোনো হিসাব নেই",
        "add_for_this_date" to "এই তারিখে এন্ট্রি যোগ করুন",

        // Settings
        "settings_title" to "পারিবারিক সেটিংস",
        "language_setting" to "অ্যাপের ভাষা / Language",
        "bengali_numerals" to "বাংলা সংখ্যা ব্যবহার (১,২,৩)",
        "household_management" to "পরিবার ও অ্যাকাউন্ট",
        "household_name" to "পরিবারের নাম",
        "copy_code" to "কোড কপি করুন",
        "code_copied" to "ইনভাইট কোড কপি করা হয়েছে!",
        "family_members" to "পরিবারের সদস্যবৃন্দ",
        "add_member" to "সদস্য যোগ করুন",
        "active_user" to "বর্তমান ফোন ব্যবহারকারী",
        "milk_settings" to "খামারের দুধের সেটিংস",
        "milk_bottle_rate" to "প্রতি বোতল দুধের দর (৳)",
        "manage_refill_items" to "রিফিল পণ্য তালিকা পরিচালনা",
        "add_refill_item" to "নতুন পণ্য যোগ করুন",
        "join_household" to "অন্য পরিবারে যোগ দিন",
        "create_household" to "নতুন পরিবার তৈরি করুন",
        "enter_code" to "৬ সংখ্যার কোড লিখুন",
        "join" to "যুক্ত হোন",
        "create" to "তৈরি করুন",
        "reset_data" to "ডেমো ডাটা পুনরায় সেট করুন",
        "reset_confirm" to "সব ডাটা পুনরায় প্রাথমিক অবস্থায় ফিরিয়ে নিবেন?",
        "app_info" to "ঘর খাতা ১.০ • বাংলাদেশি পরিবারের গৃহস্থালি ট্র্যাকার"
    )
}
