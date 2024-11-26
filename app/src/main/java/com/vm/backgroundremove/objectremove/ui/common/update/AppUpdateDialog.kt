package com.vm.backgroundremove.objectremove.ui.common.update

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import android.os.Bundle
import android.view.View
import com.lib.admob.resumeAds.AppOpenResumeManager
import com.util.FirebaseLogEventUtils
import com.util.appupdate.AppUpdateManager
import com.vm.backgroundremove.objectremove.MainActivity
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import com.vm.backgroundremove.objectremove.a8_app_utils.toDp
import com.vm.backgroundremove.objectremove.databinding.DialogAppUpdateBinding

class AppUpdateDialog() {


    fun showUpdateDialog( context: Context) {

        val layoutInflater = (context as MainActivity).layoutInflater
        val dialogBinding = DialogAppUpdateBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding.root)
            .create()
        val cornerRadius = 24f.toDp()
        val radii = floatArrayOf(
            cornerRadius, cornerRadius, cornerRadius, cornerRadius,
            cornerRadius, cornerRadius, cornerRadius, cornerRadius
        )
        val shape = RoundRectShape(radii, null, null)
        val shapeDrawable = ShapeDrawable(shape)
        shapeDrawable.paint.color = Color.WHITE

        dialog.window?.setBackgroundDrawable(shapeDrawable)
        dialog.setCanceledOnTouchOutside(false)

        dialogBinding.btnSubmit.setOnClickListener {
            dialog.dismiss()
            SystemUtil.openAppInPlayStore(context)

            //log firebase
            val bundle = Bundle()
            FirebaseLogEventUtils.logEventCommonWithName(context, "update_ok", bundle)

            AppOpenResumeManager.setEnableAdsResume(false)
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
            //log firebase
            val bundle = Bundle()
            FirebaseLogEventUtils.logEventCommonWithName(context, "update_cancel", bundle)
        }

        if (AppUpdateManager.checkUpdate  == 2) {
            dialogBinding.btnCancel.visibility = View.GONE
        }

        //set text update content
        dialogBinding.tvContent.setText(dialogBinding.tvContent.text.toString() + AppUpdateManager.updateContent)
        dialogBinding.ivWhatNew.tap {
            if (AppUpdateManager.updateContent.isNotEmpty()) {
                if (dialogBinding.tvContent.visibility == View.VISIBLE) {
                    dialogBinding.tvContent.visibility = View.GONE
                    dialogBinding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatsnew)
                } else {
                    dialogBinding.tvContent.visibility = View.VISIBLE
                    dialogBinding.ivWhatNew.setBackgroundResource(R.drawable.ic_whatnew_dismiss)
                    if (!dialogBinding.tvContent.text.toString()
                            .contains(AppUpdateManager.updateContent)
                    ) {
                        dialogBinding.tvContent.text =
                            dialogBinding.tvContent.text.toString() + AppUpdateManager.updateContent
                    }
                }
            }
        }


        //ad native
//        AdCommon.loadAndShowAdNativeAdvance(context.lifecycle,
//            RemoteConfigKey.ad_native_app_update,
//            dialogBinding.frAd,
//            null)


        dialog.show()
    }

}