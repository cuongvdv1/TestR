package com.vm.backgroundremove.objectremove.a1_common_utils.application

import com.google.firebase.Firebase
import com.google.firebase.initialize
import com.lib.admob.B9AdApplication
import com.vm.backgroundremove.objectremove.di.networkModule
import com.vm.backgroundremove.objectremove.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class DemoApplication: B9AdApplication(){
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@DemoApplication)
            modules(listOf(networkModule, viewModelModule))
        }
        Firebase.initialize(this)
    }
}
