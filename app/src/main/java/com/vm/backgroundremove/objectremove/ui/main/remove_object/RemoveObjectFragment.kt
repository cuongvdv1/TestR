package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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


}