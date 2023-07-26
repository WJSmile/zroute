package com.smart.zwj.zroute.pages

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import com.smart.zwj.zroute_processor.Route
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class ZRoutePage {
    @Route(path = "main/one")
    @Composable
    fun OnePage(navHostController: NavHostController) {
        rememberSystemUiController().setStatusBarColor(
            color = Color.White, darkIcons = true
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .statusBarsPadding()
        ) {

            Text("hello", color = Color.Black, fontSize = 30.sp,modifier=Modifier.clickable {
                navHostController.navigate("main/tow?hello=dhjjhkcdkjds")
            })
        }

    }


    @Route(path = "main/tow?hello={hello}")
    @Composable
    fun TowPage(hello: String?) {
        rememberSystemUiController().setStatusBarColor(
            color = Color.White, darkIcons = true
        )
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Text(hello ?: "ddd", color = Color.Black, fontSize = 30.sp)
        }
    }


}

