package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.FragmentRemoveObjectByListBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel

class RemoveObjectByListFragment : Fragment() {
    private lateinit var binding: FragmentRemoveObjectByListBinding
    private lateinit var viewModel: RemoveBackGroundViewModel
    private lateinit var adapter: RemoveObjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemoveObjectByListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[RemoveBackGroundViewModel::class.java]
        binding.tvText.isEnabled = false
        setupRecyclerView()
        listClick()
        binding.btnRemoveByList.tap {
            val text = binding.edRmvList.text.toString()
            val textList = adapter.getSelectedItems().toString()
            val textData = text + textList
            if (textData.isEmpty()) {
                return@tap
            }
            viewModel.setTextByListSelected(textData)
            viewModel.triggerRemoveByListSelected()
        }
        binding.edRmvList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRemoveButtonState()
                if (s.isNullOrEmpty()) {
                    binding.edRmvList.setBackgroundResource(R.drawable.bg_editext_selected)
                } else {
                    binding.edRmvList.setBackgroundResource(R.drawable.bg_border_white_16)
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        return binding.root
    }

    private fun setupRecyclerView() {
        viewModel.itemListObject.observe(requireActivity()) { listObject ->
            Log.d("RemoveObjectByListFragment", "$listObject")
            adapter = RemoveObjectAdapter(listObject)

            // Lắng nghe sự thay đổi của danh sách
            adapter.setOnSelectionChangedListener { selectedCount ->
                updateRemoveButtonState()
            }

            binding.rlOther.adapter = adapter
        }
    }

    private fun updateRemoveButtonState() {
        val isEditTextEmpty = binding.edRmvList.text.isNullOrEmpty()
        val selectedItemsCount = adapter.getSelectedItems().size

        if (isEditTextEmpty && selectedItemsCount == 0) {
            binding.btnRemoveByList.alpha = 0.5f
            binding.btnRemoveByList.isEnabled = false
        } else {
            binding.btnRemoveByList.alpha = 1f
            binding.btnRemoveByList.isEnabled = true
        }
    }


    private fun listClick() {
        binding.tvList.tap {
            binding.tvText.setTextColor(Color.parseColor("#8F9DAA"))
            binding.tvList.setTextColor(Color.parseColor("#FF6846"))
            binding.ctlRmvObjText.visibility = View.GONE
            binding.ctlRmvObjList.visibility = View.VISIBLE
        }
    }


}