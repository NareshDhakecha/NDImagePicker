package com.ndsoftwares.imagepicker.listener

/**
 * Generic Class To Listen Async Result
 */
internal interface IResultListener<T> {

    fun onResult(t: T?)
}
