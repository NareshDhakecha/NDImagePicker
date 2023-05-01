package com.ndsoftwares.imagepicker.ext

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle

fun <T : java.io.Serializable?> getSerializableEx(
    bundle: Bundle,
    name: String, clazz:
    Class<T>
) : T {
    return if(Build.VERSION.SDK_INT >= 33)
        bundle.getSerializable(name, clazz)!!
    else
        bundle.getSerializable(name) as T
}