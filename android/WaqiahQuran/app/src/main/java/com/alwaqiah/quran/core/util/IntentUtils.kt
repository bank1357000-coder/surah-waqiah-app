package com.alwaqiah.quran.core.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.shareText(text: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    startActivity(Intent.createChooser(send, chooserTitle).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** Returns false when no email client is installed. */
fun Context.composeEmail(address: String, subject: String): Boolean {
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
        putExtra(Intent.EXTRA_SUBJECT, subject)
    }
    return try {
        startActivity(intent)
        true
    } catch (e: ActivityNotFoundException) {
        false
    }
}

fun Context.playStoreUrl(): String = "https://play.google.com/store/apps/details?id=$packageName"
