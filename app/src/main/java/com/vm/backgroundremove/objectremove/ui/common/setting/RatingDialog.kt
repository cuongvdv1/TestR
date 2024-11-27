package com.vm.backgroundremove.objectremove.ui.common.setting

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap


class RatingDialog @SuppressLint("NonConstantResourceId") constructor(context2: Context) :
    Dialog(context2, R.style.CustomDialogTheme) {
    private var onPress: OnPress? = null
    private val tvTitle: TextView
    private val tvContent: TextView

    private val rootStar1: ImageView
    private val rootStar2: ImageView
    private val rootStar3: ImageView
    private val rootStar4: ImageView
    private val rootStar5: ImageView

    private val editFeedback: EditText? = null
    private val context: Context
    private val btnLater: TextView
    private val ivClose: ImageView

    private val btnRate: TextView
    private val rootBgButton: ViewGroup? = null
    private var rate = 5

    init {

        this.context = context2
        setContentView(R.layout.rating_dialog)
        val attributes = window!!.attributes
//        attributes.width = WindowManager.LayoutParams.MATCH_PARENT
        attributes.height = WindowManager.LayoutParams.WRAP_CONTENT
        attributes.width = (context.resources.displayMetrics.widthPixels * 0.9).toInt()
        window!!.attributes = attributes
        window!!.setSoftInputMode(16)

        tvTitle = findViewById<TextView>(R.id.tv_title)
        tvContent = findViewById<TextView>(R.id.tv_content)
        ivClose = findViewById<ImageView>(R.id.iv_close)

        btnLater = findViewById<TextView>(R.id.btn_cancel)
        btnRate = findViewById<TextView>(R.id.btn_submit)

        rootStar1 = findViewById<ImageView>(R.id.rootRate1)
        rootStar2 = findViewById<ImageView>(R.id.rootRate2)
        rootStar3 = findViewById<ImageView>(R.id.rootRate3)
        rootStar4 = findViewById<ImageView>(R.id.rootRate4)
        rootStar5 = findViewById<ImageView>(R.id.rootRate5)

        onclick()
        changeRating()

        rootStar1.setOnClickListener {
            rate = 1
            setStar(1)
        }

        rootStar2.setOnClickListener {
            rate = 2
            setStar(2)
        }

        rootStar3.setOnClickListener {
            rate = 3
            setStar(3)
        }

        rootStar4.setOnClickListener {
            rate = 4
            setStar(4)
        }

        rootStar5.setOnClickListener {
            rate = 5
            setStar(5)
        }

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
//        rtb.onRatingBarChangeListener =
//            OnRatingBarChangeListener { ratingBar: RatingBar?, rating: Float, fromUser: Boolean ->
//                val getRating = rtb.rating.toString()
//                s = when (getRating) {
//                    "1.0" -> 1
//                    "2.0" -> 2
//                    "3.0" -> 3
//                    "4.0" -> 4
//                    "5.0" -> 5
//                    else -> 0
//                }
//            }
    }

    val newName: String
        get() = editFeedback!!.text.toString()


    fun onclick() {
        btnRate.setOnClickListener { view: View? ->
            if (rate < 4.0) {
                onPress!!.send(rate)
            } else {
                onPress!!.rating(rate)
            }

        }

        btnLater.setOnClickListener { view: View? ->
            onPress!!.later()
        }

        ivClose.tap {
            onPress!!.cancel()
        }

    }

    fun setStar(value: Int) {
        val rates = listOf(
            rootStar1,
            rootStar2,
            rootStar3,
            rootStar4,
            rootStar5
        )

        for (i in rates.indices) {
            if (i < value) {
                rates[i].setBackgroundResource(R.drawable.ic_rate_seclect)
            } else {
                rates[i].setBackgroundResource(R.drawable.ic_rate_unseclect)
            }
        }
    }
}