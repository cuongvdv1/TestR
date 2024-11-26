package com.vm.backgroundremove.objectremove.ui.main.remove_background

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.ui.main.remove_background.adapter.ColorAdapter

class ChooseBackGroundColorFragment:Fragment() {
    private lateinit var colorAdapter: ColorAdapter
    private lateinit var recyclerView: RecyclerView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_color_background, container, false)
        recyclerView = view?.findViewById(R.id.rv_list_color)!!
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        val colors = listOf(
            Pair("color_FF37414B", R.color.color_FF37414B),
            Pair("color_FF6846", R.color.color_FF6846),
            Pair("color_FECE51", R.color.color_FECE51)
        )

        colorAdapter = ColorAdapter(requireContext(),colors)
        recyclerView.adapter = colorAdapter

        return view

    }
}