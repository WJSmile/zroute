package com.smart.zwj.zroute_processor

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.FUNCTION)
annotation class Route(val path:String)
