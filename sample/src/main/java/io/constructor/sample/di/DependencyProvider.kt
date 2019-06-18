package io.constructor.sample.di

import android.content.Context
import io.constructor.sample.data.CartDataStorage

class DependencyProvider {
    companion object {

        fun provideCartStorage(context: Context): CartDataStorage {
            return CartDataStorage(context)
        }

    }
}