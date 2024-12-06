package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.vm.backgroundremove.objectremove.R

class RemoveObjectFragment : Fragment() {

    private lateinit var seekBar: SeekBar
    private lateinit var onSizeChangedListener: OnSizeChangedListener


    // Define an interface to communicate with Activity
    interface OnSizeChangedListener {
        fun onSizeChanged(size: Int)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
// Inflate layout cho fragment
        val view = inflater.inflate(R.layout.fragment_remove_object, container, false)

        // Tham chiếu đến các View trong layout
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        val textViewProgress = view.findViewById<TextView>(R.id.tv_brush_size)

        // Thiết lập giá trị ban đầu
        textViewProgress.text = seekBar.progress.toString()

        // Xử lý sự kiện thay đổi giá trị SeekBar
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                textViewProgress.text = progress.toString()
                // Gửi giá trị SeekBar về Activity
//                onSizeChangedListener.onSizeChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // Có thể thêm logic khi bắt đầu kéo SeekBar
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // Có thể thêm logic khi thả SeekBar
            }
        })



        return view
    }

}