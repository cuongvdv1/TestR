package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.databinding.FragmentRemoveObjectBinding
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

        setupRecyclerView()
        textClick()
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
        return binding.root
    }

    private fun setupRecyclerView() {
        viewModel.itemListObject.observe(requireActivity()) { listObject ->
            Log.d("RemoveObjectByListFragment", "$listObject")
            adapter = RemoveObjectAdapter(listObject)
            binding.rlOther.adapter = adapter

        }
    }

    private fun textClick() {
        binding.tvText.tap {
            binding.tvText.setTextColor(Color.parseColor("#FF6846"))
            binding.tvList.setTextColor(Color.parseColor("#8F9DAA"))
            binding.ctlRmvObjText.visibility = View.VISIBLE
            binding.ctlRmvObjList.visibility = View.GONE
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