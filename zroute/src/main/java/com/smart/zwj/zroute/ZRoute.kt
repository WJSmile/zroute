package com.smart.zwj.zroute

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController

object ZRoute {


    @ExperimentalAnimationApi
    fun initRoute(navHostController: NavHostController, navGraphBuilder: NavGraphBuilder){
        ZRouteCompose().initRoute(navHostController, navGraphBuilder)
    }

}