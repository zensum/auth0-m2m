package io.klira.auth0.m2m

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class Atomic<T: Any>(private var value: T? = null) {
    private val lock = Mutex(false)

    fun get(): T? = value

    suspend fun set(newValue: T): T? {
        return lock.withLock {
            this.value.also {
                this.value = newValue
            }
        }
    }

    suspend fun update(oldValue: T?, newValue: T): Boolean {
        return lock.withLock {
            onTrue(this.value == oldValue) {
                this.value = newValue
            }
        }
    }

    suspend fun updateAndGet(apply: suspend (current: T?) -> T): T {
        return lock.withLock {
            val updated: T = apply(this.value)
            this.value = updated
            updated
        }
    }
}

private fun onTrue(predicate: Boolean, execute: () -> Unit): Boolean {
    if(predicate) {
        execute()
    }
    return predicate
}