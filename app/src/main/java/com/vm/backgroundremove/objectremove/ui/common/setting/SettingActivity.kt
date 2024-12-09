package com.vm.backgroundremove.objectremove.ui.common.setting

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.tasks.Task
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.lib.admob.resumeAds.AppOpenResumeManager
import com.util.RemoteConfig
import com.vm.backgroundremove.objectremove.BuildConfig
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.RemoteConfigKey
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenFeedbackModel
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenHomeModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.SharePrefUtils
import com.vm.backgroundremove.objectremove.a8_app_utils.toDp
import com.vm.backgroundremove.objectremove.databinding.ActivitySettingBinding
import com.vm.backgroundremove.objectremove.dialog.RatingDialog
import com.vm.backgroundremove.objectremove.dialog.ThankDialog
import com.vm.backgroundremove.objectremove.ui.common.feedback.DialogFeedback
import com.vm.backgroundremove.objectremove.ui.common.language.LanguageSettingActivity
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.ProjectsActivity
import com.vm.backgroundremove.objectremove.ui.main.your_projects.YourProjectsResultActivity


class SettingActivity : BaseActivity<ActivitySettingBinding, BaseViewModel>() {

    private var check = false

    private var remoteConfigHomeModel: RemoteConfigScreenHomeModel? = null
    private var remoteConfigScreenFeedbackModel: RemoteConfigScreenFeedbackModel? = null


    override fun createBinding() = ActivitySettingBinding.inflate(layoutInflater)

    override fun setViewModel() = BaseViewModel()

    override fun initView() {
        super.initView()

        binding.tvVersion.text = getString(R.string.version) + " " + BuildConfig.VERSION_NAME

        //home model
        remoteConfigHomeModel = RemoteConfig.getConfigObject(
            this,
            RemoteConfigKey.screen_home,
            RemoteConfigScreenHomeModel::class.java
        )
        //Feedback model
        remoteConfigScreenFeedbackModel = RemoteConfig.getConfigObject(
            this,
            RemoteConfigKey.screen_feedback,
            RemoteConfigScreenFeedbackModel::class.java
        )

        binding.ctlHome.tap {
            val intent = Intent(this@SettingActivity, HomeActivity::class.java)
            startActivity(intent)
        }


        binding.ctlYourProjects.tap {
            val intent = Intent(this, ProjectsActivity::class.java)
            startActivity(intent)
        }

        binding.languageSetting.tap {
            val intent = Intent(this, LanguageSettingActivity::class.java)
            startActivity(intent)
        }

        binding.shareSettings.tap {
            if (!check) {
                check = true
                share()
            }
        }

        binding.ratingSettings.tap {
            if (!check) {
                check = true
                showRateDialog()
            }
        }


        binding.feedbackSettings.tap {
            if (!check) {
                check = true // Đánh dấu là nút đã được bấm


                if (remoteConfigScreenFeedbackModel?.type.equals("gmail")) {
                    val emailIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(
                            Intent.EXTRA_EMAIL,
                            arrayOf(remoteConfigScreenFeedbackModel?.fb_email)
                        )
                        putExtra(Intent.EXTRA_SUBJECT, remoteConfigScreenFeedbackModel?.fb_tittle)
                    }
                    emailIntent.setPackage("com.google.android.gm")
                    try {
                        startActivity(emailIntent)
                    } catch (e: Exception) {
                        Toast.makeText(
                            this,
                            getString(R.string.gmail_is_not_available_on_this_device_feedback_needs_to_be_sent_via_gmail_application),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    val dialogFeedback = DialogFeedback()
                    dialogFeedback.show(supportFragmentManager, "dialogfeeback")

                }

                // Đặt lại biến sau khi hoàn thành hành động
                Handler(Looper.getMainLooper()).postDelayed({
                    check = false
                }, 1000) // Thời gian chờ 500ms, điều chỉnh nếu cần

                //disable ad resume for share
                AppOpenResumeManager.setEnableAdsResume(false)
            }
        }


        binding.privacyPolicySettings.tap {
            check = true
            val intent = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://firebasestorage.googleapis.com/v0/b/td1tech-v1-v042-chatmath.appspot.com/o/Privacy-Policy.html?alt=media&token=65f14b09-bdfe-4c89-a153-3f850db5c623")
            )
            startActivity(intent)
            //disable ad resume for policy
            AppOpenResumeManager.setEnableAdsResume(false)
        }

        if (SharePrefUtils.isRated(this@SettingActivity)) {
            binding.ratingSettings.visibility = View.GONE
        } else {
            binding.ratingSettings.visibility = View.VISIBLE
        }

    }

    private fun share() {
        check = true
        val intentShare = Intent(Intent.ACTION_SEND)
        intentShare.type = "text/plain"
        intentShare.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intentShare.putExtra(
            Intent.EXTRA_TEXT,
            "${getString(R.string.app_name)}\nhttps://play.google.com/store/apps/details?id=${this.packageName}"
        )
        startActivity(Intent.createChooser(intentShare, "Share"))
        //disable ad resume for share
        AppOpenResumeManager.setEnableAdsResume(false)
    }


    private fun showRateDialog() {
        check = true
        val manager = ReviewManagerFactory.create(this)
        val ratingDialog = RatingDialog(this)
        ratingDialog.init(this, object : RatingDialog.OnPress {
            override fun send(s: Int) {
//                Toast.makeText(
//                    this@SettingActivity,
//                    getString(R.string.thank_you),
//                    Toast.LENGTH_SHORT
//                ).show()
                showThankYouDialog()
                binding.ratingSettings.visibility = View.GONE
                SharePrefUtils.forceRated(this@SettingActivity)
                ratingDialog.dismiss()
            }

            override fun rating(s: Int) {
                onRateAppNew()
                binding.ratingSettings.visibility = View.GONE
                SharePrefUtils.forceRated(this@SettingActivity)
                ratingDialog.dismiss()
            }

            override fun cancel() {
                ratingDialog.dismiss()
            }

            override fun later() {
                ratingDialog.dismiss()
            }

            override fun gotIt() {
                ratingDialog.dismiss()
            }
        })

        ratingDialog.show()
        ratingDialog.setOnDismissListener {
            check = false
        }
    }


    private fun rateAppOnStoreNew() {
        val packageName = baseContext.packageName
        val uri: Uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(
            Intent.FLAG_ACTIVITY_NO_HISTORY or
                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        )
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }


    private fun onRateAppNew() {
        val manager: ReviewManager?
        var reviewInfo: ReviewInfo?
        manager = ReviewManagerFactory.create(this)
        val request: Task<ReviewInfo> = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                SharePrefUtils.forceRated(this)
                reviewInfo = task.result
                val flow: Task<Void> =
                    manager.launchReviewFlow(this, reviewInfo!!)
                flow.addOnSuccessListener {
                    showThankYouDialog()
                    rateAppOnStoreNew()
                }
            }
        }
    }


    private fun showThankYouDialog() {
        check = true

        val cornerRadius = 24f.toDp()
        val radii = floatArrayOf(
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius,
            cornerRadius
        )
        val shape = RoundRectShape(radii, null, null)
        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = Color.WHITE
        val thankDialog = ThankDialog(this@SettingActivity)
        thankDialog.window!!.setBackgroundDrawable(shapeDrawable)
        thankDialog.setCanceledOnTouchOutside(false)
        thankDialog.init(this@SettingActivity, object : ThankDialog.OnPress {
            override fun send(s: Int) {
                Toast.makeText(
                    this@SettingActivity,
                    getString(R.string.thank_you),
                    Toast.LENGTH_SHORT
                ).show()
                binding.ratingSettings.visibility = View.GONE
                SharePrefUtils.forceRated(this@SettingActivity)
                thankDialog.dismiss()
            }

            override fun rating(s: Int) {
                onRateAppNew()
                binding.ratingSettings.visibility = View.GONE
                SharePrefUtils.forceRated(this@SettingActivity)
                thankDialog.dismiss()
            }

            override fun cancel() {
                thankDialog.dismiss()
            }

            override fun later() {
                thankDialog.dismiss()
            }

            override fun gotIt() {
                thankDialog.dismiss()
            }
        })

        thankDialog.show()
        thankDialog.setOnDismissListener {
            check = false
        }
    }


    override fun onResume() {
        super.onResume()
        check = false
    }


}