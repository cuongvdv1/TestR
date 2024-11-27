package com.vm.backgroundremove.objectremove.ui.common.feedback

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.util.RemoteConfig
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.RemoteConfigKey
import com.vm.backgroundremove.objectremove.a1_common_utils.model_remote_config.screens.RemoteConfigScreenFeedbackModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.DialogFeedBackBinding


class DialogFeedback : BottomSheetDialogFragment() {

    private val binding by lazy {
        DialogFeedBackBinding.inflate(layoutInflater)
    }


    private var remoteConfigScreenFeedbackModel: RemoteConfigScreenFeedbackModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //Feedback model
        remoteConfigScreenFeedbackModel = RemoteConfig.getConfigObject(
            requireActivity(),
            RemoteConfigKey.screen_feedback,
            RemoteConfigScreenFeedbackModel::class.java
        )

        dialog?.setCancelable(false)
        dialog?.setCanceledOnTouchOutside(false)

        // Vô hiệu hóa hành vi kéo của BottomSheet
        val bottomSheet =
            (dialog as BottomSheetDialog).findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            BottomSheetBehavior.from(it).apply {
                isDraggable = false
            }
        }
        binding.icDismiss.tap {
            dialog?.dismiss()
        }
        binding.btnSave.tap {


            val subject = binding.etSubject.text.toString()
            val yourFeedback = binding.etEnterYourFeedback.text.toString()
            if (subject.isEmpty() || yourFeedback.isEmpty()) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.please_send_us_some_feedback),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val emailIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(remoteConfigScreenFeedbackModel?.fb_email))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, yourFeedback)
                }
                emailIntent.setPackage("com.google.android.gm")
                try {
                    startActivity(emailIntent)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.gmail_is_not_available_on_this_device_feedback_needs_to_be_sent_via_gmail_application),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


    }
}