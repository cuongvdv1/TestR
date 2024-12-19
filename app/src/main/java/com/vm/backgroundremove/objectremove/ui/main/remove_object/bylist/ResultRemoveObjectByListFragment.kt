package com.vm.backgroundremove.objectremove.ui.main.remove_object.bylist

import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseFragment
import com.vm.backgroundremove.objectremove.databinding.FragmentRemoveObjectByListBinding
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel


class ResultRemoveObjectByListFragment : BaseFragment<FragmentRemoveObjectByListBinding>() {

    private lateinit var viewModel: RemoveBackGroundViewModel
    private lateinit var adapter: ResultRemoveObjectAdapter

    override fun inflateViewBinding(): FragmentRemoveObjectByListBinding {
        return FragmentRemoveObjectByListBinding.inflate(layoutInflater)

    }

    override fun initView() {
        super.initView()
        viewModel = ViewModelProvider(requireActivity())[RemoveBackGroundViewModel::class.java]

        setupRecyclerView()
        viewBinding.rlOther.alpha = 0.5f
        viewBinding.btnRemoveByList.alpha = 0.5f
        viewBinding.edRmvList.alpha = 0.5f
        viewBinding.edRmvList.isEnabled = false
    }

    private fun setupRecyclerView() {
        viewModel.itemListObject.observe(requireActivity()) { listObject ->
            Log.d("RemoveObjectByListFragment", "$listObject")
            adapter = ResultRemoveObjectAdapter(listObject)
            viewBinding.rlOther.adapter = adapter

        }
    }


}