package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        binding.btnRemove.tap {
            val text = binding.edRmvObject.text.toString()
            if (text.isEmpty()) {
                return@tap
            }
            viewModel.setText(text.toString())
            viewModel.triggerRemove()
        }

        binding.btnRemoveByList.tap {
            val text = binding.edRmvList.text.toString()
            if (text.isEmpty()) {
                return@tap
            }

            viewModel.setTextByList(text)
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