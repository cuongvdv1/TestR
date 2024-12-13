package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.FragmentRemoveObjectBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel

class RemoveObjectFragment : Fragment() {
    private lateinit var binding: FragmentRemoveObjectBinding
    private lateinit var viewModel: RemoveBackGroundViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemoveObjectBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[RemoveBackGroundViewModel::class.java]
        textClick()
        listClick()

        binding.edRmvObject.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    binding.btnRemove.alpha = 0.5f
                    binding.btnRemove.isEnabled = false
                    binding.edRmvObject.setBackgroundResource(R.drawable.bg_editext_selected)
                } else {
                    binding.btnRemove.alpha = 1f
                    binding.btnRemove.isEnabled = true
                    binding.edRmvObject.setBackgroundResource(R.drawable.bg_border_white_16)
                }
            }

            override fun afterTextChanged(s: Editable?) {
                binding.edRmvObject.setBackgroundResource(R.drawable.bg_editext_selected)
            }
        })
        binding.btnRemove.tap {
            val text = binding.edRmvObject.text.toString()
            if (text.isEmpty()) {
                return@tap
            }
            viewModel.setText(text.toString())
            viewModel.triggerRemove()
        }
        val maxLengthFilter = InputFilter.LengthFilter(25)
        binding.edRmvObject.filters = arrayOf(maxLengthFilter)
        binding.clDetecting.tap {
            viewModel.triggerRemoveByList()
        }
        return binding.root
    }


    private fun textClick() {
        binding.tvText.tap {
            binding.tvText.setTextColor(Color.parseColor("#FF6846"))
            binding.tvList.setTextColor(Color.parseColor("#8F9DAA"))
            binding.ctlRmvObjText.visibility = View.VISIBLE
            binding.ctlRmvObjList.visibility = View.GONE
            binding.viewColorIndicator.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_FF6846
                )
            )
            binding.viewBgIndicator.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_8F9DAA
                )
            )
        }
    }

    private fun listClick() {
        binding.tvList.tap {
            binding.tvText.setTextColor(Color.parseColor("#8F9DAA"))
            binding.tvList.setTextColor(Color.parseColor("#FF6846"))
            binding.ctlRmvObjText.visibility = View.GONE
            binding.ctlRmvObjList.visibility = View.VISIBLE
            binding.viewBgIndicator.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_FF6846
                )
            )
            binding.viewColorIndicator.setBackgroundColor(
                ContextCompat.getColor(
                    requireActivity(),
                    R.color.color_8F9DAA
                )
            )
        }
    }


}