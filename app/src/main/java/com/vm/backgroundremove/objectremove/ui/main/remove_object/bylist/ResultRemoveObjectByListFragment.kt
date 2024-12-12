package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

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
import com.vm.backgroundremove.objectremove.databinding.FragmentRemoveObjectByListBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel


class ResultRemoveObjectByListFragment : Fragment() {

    private lateinit var binding: FragmentRemoveObjectByListBinding
    private lateinit var viewModel: RemoveBackGroundViewModel
    private lateinit var adapter: ResultRemoveObjectAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentRemoveObjectByListBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[RemoveBackGroundViewModel::class.java]

        setupRecyclerView()
        binding.rlOther.alpha = 0.5f
        binding.btnRemoveByList.alpha = 0.5f
        binding.edRmvList.alpha = 0.5f
        binding.edRmvList.isEnabled = false

        return binding.root
    }

    private fun setupRecyclerView() {
        viewModel.itemListObject.observe(requireActivity()) { listObject ->
            Log.d("RemoveObjectByListFragment", "$listObject")
            adapter = ResultRemoveObjectAdapter(listObject)
            binding.rlOther.adapter = adapter

        }
    }




}