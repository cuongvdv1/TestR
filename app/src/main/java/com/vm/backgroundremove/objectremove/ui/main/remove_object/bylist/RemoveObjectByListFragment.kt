package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
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

        adapter = RemoveObjectAdapter(emptyList()) // Khởi tạo với danh sách rỗng
        binding.rlOther.adapter = adapter

        setupRecyclerView()
        setupListeners()


        return binding.root
    }

    private fun setupListeners() {
        updateEditTextState()

        // Nút Remove
        binding.btnRemoveByList.tap {

            val text = binding.edRmvList.text.toString()
            val textList = adapter.getSelectedItems().toString()
            val textData = text + textList
            if (textData.isEmpty()) return@tap
            val disabledItems = viewModel.itemDisabledState.value.orEmpty()
            val updatedSelectedItems = adapter.mergeSelectionsWithDisabled(disabledItems)


            // Gộp dữ liệu từ EditText và danh sách được chọn
            val finalData = text + updatedSelectedItems.joinToString(", ")

            if (finalData.isEmpty()) return@tap

            // Cập nhật dữ liệu cho ViewModel
            viewModel.setTextByListSelected(updatedSelectedItems.toString())
            viewModel.setTextByList(text)
            viewModel.triggerRemoveByListSelected()
        }


        binding.edRmvList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRemoveButtonState()
                if (s.isNullOrEmpty()) {
                    adapter.setSelectionEnabled(true)
                    binding.rlOther.alpha = 1f
                } else {
                    adapter.setSelectionEnabled(false)
                    adapter.clearSelections()
                    binding.rlOther.alpha = 0.5f
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun setupRecyclerView() {
        viewModel.itemListObject.observe(requireActivity()) { listObject ->
            Log.d("RemoveObjectByListFragment", "$listObject")

            if (listObject.toString().startsWith("[[")){
                val convertList = convertOtherToList(listObject.toString())
                adapter = RemoveObjectAdapter(convertList)
            }else{
                adapter = RemoveObjectAdapter(listObject)
            }


            viewModel.itemDisabledState.value?.let { disabledItems ->
                adapter.setDisabledItems(disabledItems)
            }
            adapter.setOnSelectionChangedListener { selectedCount ->
                updateRemoveButtonState()
                if (selectedCount == 0){
                    binding.edRmvList.isEnabled = true
                    binding.edRmvList.alpha = 1f
                }else{
                    binding.edRmvList.isEnabled = false
                    binding.edRmvList.alpha = 0.5f
                }
            }

            binding.rlOther.adapter = adapter
        }
        // Quan sát danh sách itemDisabledState
        viewModel.itemDisabledState.observe(viewLifecycleOwner) { disabledItems ->
            Log.d("TAG_MODEL", "disabledItems $disabledItems")
            adapter.setDisabledItems(disabledItems) // Truyền danh sách String vào Adapter
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
    private fun updateEditTextState() {
        val selectedItemsCount = adapter.getSelectedItems().size
        val isEditTextEmpty = binding.edRmvList.text.isNullOrEmpty()

        binding.edRmvList.isEnabled = selectedItemsCount == 0
        binding.edRmvList.alpha = if (selectedItemsCount > 0) 0.5f else 1f
        binding.rlOther.alpha = if (isEditTextEmpty) 1f else 0.5f
    }
    private fun convertOtherToList(other: String): List<String> {
        val cleanedString = other.removePrefix("[[").removeSuffix("]]")
        return cleanedString.split(",").map { it.trim() }
    }

}