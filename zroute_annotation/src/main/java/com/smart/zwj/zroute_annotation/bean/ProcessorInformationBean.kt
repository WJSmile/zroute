package com.smart.zwj.zroute_annotation.bean

import kotlinx.metadata.KmFunction

data class ProcessorInformationBean(
    val packageName: String,
    val className: String,
    val funName:String,
    val parameterList:List<ParameterBean>
)

data class ParameterBean(val name:String,val type:String,val declaresDefaultValue:Boolean,val isNullable: Boolean)

data class FunctionsData(val functions:MutableList<KmFunction>,var num:Int,var className:String)

