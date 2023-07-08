package com.zwj.zroute_annotation

@Retention(AnnotationRetention.BINARY)
@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class Parameter(val key:String)
