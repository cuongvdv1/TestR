package com.vm.backgroundremove.objectremove.di


import android.app.Application
import com.vm.backgroundremove.objectremove.ui.main.progress.ProcessViewModel
import com.vm.backgroundremove.objectremove.ui.main.remove_background.RemoveBackGroundViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { RemoveBackGroundViewModel(get()) }
    viewModel { (application: Application) ->
        ProcessViewModel(application, get())
    }
}
