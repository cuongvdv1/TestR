package com.vm.backgroundremove.objectremove.ui.common.language

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.vm.backgroundremove.objectremove.MainActivity
import com.vm.backgroundremove.objectremove.R
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseActivity
import com.vm.backgroundremove.objectremove.a1_common_utils.base.BaseViewModel
import com.vm.backgroundremove.objectremove.a1_common_utils.view.tap
import com.vm.backgroundremove.objectremove.a8_app_utils.SystemUtil
import com.vm.backgroundremove.objectremove.databinding.ActivityLanguageSettingBinding
import com.vm.backgroundremove.objectremove.ui.main.home.HomeActivity


class LanguageSettingActivity : BaseActivity<ActivityLanguageSettingBinding, BaseViewModel>() {


    private var languageAdapter: LanguageAdapter? = null
    private var lang: String = ""

    override fun createBinding()= ActivityLanguageSettingBinding.inflate(layoutInflater)

    override fun setViewModel()= BaseViewModel()

    override fun initView() {
        super.initView()
        window.statusBarColor = ContextCompat.getColor(this, R.color.color_F0F8FF)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR

        binding.ivSave.tap {
            //other process
            SystemUtil.saveLocale(this@LanguageSettingActivity, lang)
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        binding.ivBack.tap {
            finish()
        }

        val listLanguage = mutableListOf<LanguageModel>()

        val listLang = mutableListOf(
            LanguageModel("Hindi", "hi", false, R.drawable.img_logo_hindi),
            LanguageModel("French", "fr", false, R.drawable.img_logo_french),
            LanguageModel("Spanish", "es", false, R.drawable.img_logo_spanish),
            LanguageModel("Portuguese", "pt", false, R.drawable.img_logo_portuguese),
            LanguageModel("Indonesian", "in", false, R.drawable.img_logo_indo),
            LanguageModel("German", "de", false, R.drawable.img_logo_german),
            LanguageModel("English", "en", true, R.drawable.img_logo_english),
        )


        listLanguage.addAll(listLang)
        val linearLayoutManager = LinearLayoutManager(this)

        for (i in listLang.indices) {
            val languageModel = listLang[i]
            if (languageModel.code.contains(SystemUtil.getPreLanguage(this).toString())) {
                listLanguage.remove(languageModel)
                listLanguage.add(0, languageModel)
                break
            }
        }

        languageAdapter =
            LanguageAdapter(this, listLanguage, object : LanguageAdapter.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    lang = listLanguage[position].code
                }

            })
        binding?.rvLanguage?.layoutManager = linearLayoutManager
        binding?.rvLanguage?.adapter = languageAdapter

        languageAdapter?.selectedItemPosition = 0

    }
}