package com.example.tutorconnect.utils

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.widget.TextView
import com.example.tutorconnect.R

class ProgressDialog(context: Context) : Dialog(context) {
    init {
        val inflate = LayoutInflater.from(context).inflate(R.layout.dialog_progress, null)
        setContentView(inflate)
        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun setMessage(message: String) {
        findViewById<TextView>(R.id.tvMessage)?.text = message
    }
}
