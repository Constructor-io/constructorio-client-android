package io.constructor.data

import io.reactivex.annotations.Nullable

class ConstructorData<V> private constructor(@param:Nullable @field:Nullable
                                             private val value: V?, @param:Nullable @field:Nullable
                                             private val error: Throwable?, var networkError: Boolean = false) {

    val isEmpty: Boolean
        get() = value == null && error == null

    val isError: Boolean
        get() = error != null

    fun onValue(action: (V) -> Unit): ConstructorData<V> {
        if (value != null) {
            action.invoke(value)
        }
        return this
    }

    fun onEmpty(action: () -> Unit): ConstructorData<V> {
        if (isEmpty) {
            action.invoke()
        }
        return this
    }

    fun onError(action: (Throwable) -> Unit): ConstructorData<V> {
        if (error != null) {
            action.invoke(error)
        }
        return this
    }

    fun hasValue(): Boolean {
        return value != null
    }

    @Nullable
    fun get(): V? {
        return value
    }

    fun error(): Throwable? {
        return error
    }

    override fun toString(): String {
        return "Data{" + "value=" + value + ", error=" + error + '}'.toString()
    }

    companion object {

        fun <V> of(@Nullable value: V): ConstructorData<V> {
            return ConstructorData(nullIfEmptyCollection(value), null)
        }

        private fun <V> nullIfEmptyCollection(value: V): V? {
            return if (value is Collection<*> && (value as Collection<*>).isEmpty())
                null
            else
                value
        }

        fun <V> empty(): ConstructorData<V> {
            return ConstructorData(null, null)
        }

        fun <V> error(error: Throwable?): ConstructorData<V> {
            return ConstructorData(null, error)
        }

        fun <V> networkError(msg: String?): ConstructorData<V> {
            return ConstructorData(null, Exception(msg), true)
        }

        fun <V> asError(data: ConstructorData<V>): ConstructorData<V> {
            return error(data.error)
        }
    }
}
