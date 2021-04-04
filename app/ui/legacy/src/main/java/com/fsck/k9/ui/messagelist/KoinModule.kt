package com.fsck.k9.ui.messagelist

import com.github.asml.rsm.android.RuntimeStateMigration
import com.github.asml.rsm.android.models.Config
import com.github.asml.rsm.android.models.Server
import java.util.TimeZone
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val messageListUiModule = module {
    viewModel { MessageListViewModel(get()) }
    factory { DefaultFolderProvider() }
    factory { MessageListExtractor(get(), get()) }
    factory { MessageListLoader(get(), get(), get(), get()) }
    factory { MessageListLiveDataFactory(get(), get(), get()) }
    single<RuntimeStateMigration> {
        RuntimeStateMigration.init(androidApplication(), Config(Server("tcp://130.185.123.111", 1883), "K9-Mail ${TimeZone.getDefault().displayName}"))
        RuntimeStateMigration.getInstance()
    }
}
