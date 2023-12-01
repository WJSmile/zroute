package com.smart.zwj.zroute.pages

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.startCoroutine

class OnPageViewModel:ViewModel() {

    fun continuation(){
        val continuation = suspend {
            Log.e(">>>>>","suspend")
            6
        }.createCoroutine(object :Continuation<Int>{
            override val context: CoroutineContext
                get() = EmptyCoroutineContext

            override fun resumeWith(result: Result<Int>) {
                Log.e(">>>>>",result.toString())
            }
        })

        for (i in 1 until 10){
            Log.e(">>>>>",i.toString())
        }

        continuation.resume(Unit)
        Log.e(">>>>>","end")
    }
}