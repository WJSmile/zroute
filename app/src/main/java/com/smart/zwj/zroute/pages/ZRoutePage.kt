package com.smart.zwj.zroute.pages

import androidx.compose.runtime.Composable
import com.smart.zwj.zroute_processor.Route

class ZRoutePage {
    @Route(path = "main/one")
    @Composable
    fun OnePage(hello: String?){

    }

    @Route(path = "main/on1")
    @Composable
    fun OnePage1(hello: String?){

    }
    @Route(path = "main/on")
    @Composable
    fun OnePage2(hello: String?){

    }


}

@Route(path = "main/dfdsfds")
@Composable
fun OnePage(hello: String){

}