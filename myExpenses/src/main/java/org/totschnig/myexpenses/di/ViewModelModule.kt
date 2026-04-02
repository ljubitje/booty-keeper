package org.totschnig.myexpenses.di

import dagger.Module
import dagger.Provides
import org.totschnig.myexpenses.viewmodel.MyExpensesViewModel

@Module
open class ViewModelModule {
    @Provides
    open fun provideMyExpensesViewModelClass(): Class<out MyExpensesViewModel> = MyExpensesViewModel::class.java
}
