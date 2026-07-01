package dev.mobase.firebase.util

import android.os.Bundle
import android.os.Parcelable
import java.io.Serializable
import kotlin.collections.iterator
import kotlin.jvm.isArrayOf

internal fun Map<String, Any?>.toBundle(): Bundle {
    val bundle = Bundle()

    for ((key, value) in this) {
        bundle.apply {
            when (value) {
                null -> putString(key, null)
                is Int -> putInt(key, value)
                is Long -> putLong(key, value)
                is String -> putString(key, value)
                is Boolean -> putBoolean(key, value)
                is Float -> putFloat(key, value)
                is Double -> putDouble(key, value)
                is Bundle -> putBundle(key, value)
                is IntArray -> putIntArray(key, value)
                is LongArray -> putLongArray(key, value)
                is BooleanArray -> putBooleanArray(key, value)
                is FloatArray -> putFloatArray(key, value)
                is DoubleArray -> putDoubleArray(key, value)
                is Array<*> -> when {
                    value.isArrayOf<String>() -> {
                        @Suppress("UNCHECKED_CAST")
                        putStringArray(key, value as Array<String>)
                    }

                    value.isArrayOf<Parcelable>() -> {
                        @Suppress("UNCHECKED_CAST")
                        putParcelableArray(key, value as Array<Parcelable>)
                    }

                    else -> throw IllegalArgumentException("Unsupported array type for key: $key")
                }

                is Serializable -> putSerializable(key, value)
                is Parcelable -> putParcelable(key, value)

                else -> throw IllegalArgumentException("Unsupported type for key: $key = ${value::class.java}")
            }
        }
    }

    return bundle
}