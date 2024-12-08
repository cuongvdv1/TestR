package com.vm.backgroundremove.objectremove.ui.main.remove_object

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.database.HistoryModel
import com.vm.backgroundremove.objectremove.databinding.ActivityResultRemoveObjectBinding

class ResultRemoveObjectActivity : BaseActivity<ActivityResultRemoveObjectBinding,BaseViewModel>() {
    private var historyModel: HistoryModel? = null
    private var type = ""
    override fun createBinding(): ActivityResultRemoveObjectBinding {
        return ActivityResultRemoveObjectBinding.inflate(layoutInflater)
    }

    override fun setViewModel(): BaseViewModel {
    return BaseViewModel()
    }

    override fun initView() {
        super.initView()

    }
}