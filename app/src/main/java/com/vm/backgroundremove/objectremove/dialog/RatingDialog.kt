package com.vm.backgroundremove.objectremove.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap


class RatingDialog @SuppressLint("NonConstantResourceId") constructor(context2: Context) :
    Dialog(context2, R.style.CustomDialogTheme) {
    private var onPress: OnPress? = null
    private val tvTitle: TextView
    private val tvContent: TextView
    private val tvCancel: ImageView
    private val rtb: RatingBar
    private val editFeedback: EditText? = null
    private val context: Context
    private val btnLater: TextView
    private val btnRate: TextView
    private val rootBgButton: ViewGroup? = null
    private var s = 5

    init {

        this.context = context2
        setContentView(R.layout.rating_dialog)
        val attributes = window!!.attributes
        attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)
        tvTitle = findViewById<TextView>(R.id.tv_title)
        tvContent = findViewById<TextView>(R.id.tv_content)
        rtb = findViewById<RatingBar>(R.id.rating_bar)
        btnLater = findViewById<TextView>(R.id.btn_cancel)
        btnRate = findViewById<TextView>(R.id.btn_submit)
        tvCancel = findViewById<ImageView>(R.id.img_close)

        rtb.rating = 5.0f

        onclick()
        changeRating()



    }

    interface OnPress {
        fun send(s: Int)

        fun rating(s: Int)

        fun cancel()

        fun later()

        fun gotIt()
    }

    fun init(context: Context?, onPress: OnPress?) {
        this.onPress = onPress
    }

    fun changeRating() {
        rtb.onRatingBarChangeListener =
            OnRatingBarChangeListener { ratingBar: RatingBar?, rating: Float, fromUser: Boolean ->
                val getRating = rtb.rating.toString()
                s = when (getRating) {
                    "1.0" -> 1
                    "2.0" -> 2
                    "3.0" -> 3
                    "4.0" -> 4
                    "5.0" -> 5
                    else -> 0
                }
            }
    }

    val newName: String
        get() = editFeedback!!.text.toString()

    val rating: String
        get() = rtb.rating.toString()

    fun onclick() {
        btnRate.setOnClickListener { view: View? ->
            Log.d("TAG23", "onclick: ${rtb.rating}")
            if (rtb.rating <= 4.0) {
                onPress!!.send(s)
            } else {
                onPress!!.rating(s)
            }

            rtb.visibility = View.GONE

        }

        btnLater.setOnClickListener {
                view: View? -> onPress!!.later()
        }

        tvCancel.setOnClickListener {
                view: View? -> onPress!!.cancel()
        }
    }
}