package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseFragment
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.FragmentRemoveObjectByListBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel

class RemoveObjectByListFragment : BaseFragment<FragmentRemoveObjectByListBinding>() {
    private lateinit var viewModel: RemoveBackGroundViewModel
    private lateinit var adapter: RemoveObjectAdapter
    override fun inflateViewBinding(): FragmentRemoveObjectByListBinding {
        return FragmentRemoveObjectByListBinding.inflate(layoutInflater)
    }

    override fun initView() {
        super.initView()
        viewModel = ViewModelProvider(requireActivity())[RemoveBackGroundViewModel::class.java]

        adapter = RemoveObjectAdapter(emptyList()) // Khởi tạo với danh sách rỗng
        viewBinding.rlOther.adapter = adapter

        setupRecyclerView()
        setupListeners()

        viewModel.itemDisabledState.observe(requireActivity()) { disabledItems ->
            if (disabledItems.isNotEmpty()) {
                viewBinding.rlOther.alpha = 0.5f
                viewBinding.btnRemoveByList.alpha = 0.5f
                viewBinding.edRmvList.alpha = 0.5f

                // Vô hiệu hóa giao diện
                viewBinding.rlOther.isEnabled = false
                viewBinding.btnRemoveByList.isEnabled = false
                viewBinding.edRmvList.isEnabled = false

                // Hủy sự kiện click
                viewBinding.rlOther.setOnClickListener(null)
                viewBinding.btnRemoveByList.setOnClickListener(null)
                viewBinding.edRmvList.setOnClickListener(null)
            } else {
                // Kích hoạt lại giao diện nếu cần
                viewBinding.rlOther.alpha = 1f
                viewBinding.btnRemoveByList.alpha = 1f
                viewBinding.edRmvList.alpha = 1f

                viewBinding.rlOther.isEnabled = true
                viewBinding.btnRemoveByList.isEnabled = true
                viewBinding.edRmvList.isEnabled = true

                setupListeners() // Thiết lập lại sự kiện
            }
        }
    }

    private fun setupListeners() {
        updateEditTextState()

        // Nút Remove
        viewBinding.btnRemoveByList.tap {

            val text = viewBinding.edRmvList.text.toString()
            val textList = adapter.getSelectedItems().toString()
            val textData = text + textList
            if (textData.isEmpty()) return@tap
            val disabledItems = viewModel.itemDisabledState.value.orEmpty()
            if (disabledItems.isNotEmpty()) return@tap
            val updatedSelectedItems = adapter.mergeSelectionsWithDisabled(disabledItems)

            // Gộp dữ liệu từ EditText và danh sách được chọn
            val finalData = text + updatedSelectedItems.joinToString(", ")

            if (finalData.isEmpty()) return@tap

            // Cập nhật dữ liệu cho ViewModel
            viewModel.setTextByListSelected(updatedSelectedItems.toString())
            viewModel.setTextByList(text)
            viewModel.triggerRemoveByListSelected()
        }


        viewBinding.edRmvList.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateRemoveButtonState()
                if (s.isNullOrEmpty()) {
                    adapter.setSelectionEnabled(true)
                    viewBinding.rlOther.alpha = 1f
                } else {
                    adapter.setSelectionEnabled(false)
                    adapter.clearSelections()
                    viewBinding.rlOther.alpha = 0.5f
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }


    private fun setupRecyclerView() {
        viewModel.itemListObject.observe(requireActivity()) { listObject ->
            Log.d("RemoveObjectByListFragment", "$listObject")

            if (listObject.toString().startsWith("[[")) {
                val convertList = convertOtherToList(listObject.toString())
                adapter = RemoveObjectAdapter(convertList)
            } else {
                adapter = RemoveObjectAdapter(listObject)
            }


            viewModel.itemDisabledState.value?.let { disabledItems ->
                adapter.setDisabledItems(disabledItems)
            }
            adapter.setOnSelectionChangedListener { selectedCount ->
                updateRemoveButtonState()
                if (selectedCount == 0) {
                    viewBinding.edRmvList.isEnabled = true
                    viewBinding.edRmvList.alpha = 1f
                } else {
                    viewBinding.edRmvList.isEnabled = false
                    viewBinding.edRmvList.alpha = 0.5f
                }
            }

            viewBinding.rlOther.adapter = adapter
        }
        // Quan sát danh sách itemDisabledState
        viewModel.itemDisabledState.observe(viewLifecycleOwner) { disabledItems ->
            Log.d("TAG_MODEL", "disabledItems $disabledItems")
            adapter.setDisabledItems(disabledItems)
        }
    }

    private fun updateRemoveButtonState() {
        val isEditTextEmpty = viewBinding.edRmvList.text.isNullOrEmpty()
        val selectedItemsCount = adapter.getSelectedItems().size

        if (isEditTextEmpty && selectedItemsCount == 0) {
            viewBinding.btnRemoveByList.alpha = 0.5f
            viewBinding.btnRemoveByList.isEnabled = false
        } else {
            viewBinding.btnRemoveByList.alpha = 1f
            viewBinding.btnRemoveByList.isEnabled = true
        }
    }

    private fun updateEditTextState() {
        val selectedItemsCount = adapter.getSelectedItems().size
        val isEditTextEmpty = viewBinding.edRmvList.text.isNullOrEmpty()

        viewBinding.edRmvList.isEnabled = selectedItemsCount == 0
        viewBinding.edRmvList.alpha = if (selectedItemsCount > 0) 0.5f else 1f
        viewBinding.rlOther.alpha = if (isEditTextEmpty) 1f else 0.5f
    }

    private fun convertOtherToList(other: String): List<String> {
        val cleanedString = other.removePrefix("[[").removeSuffix("]]")
        return cleanedString.split(",").map { it.trim() }
    }

}