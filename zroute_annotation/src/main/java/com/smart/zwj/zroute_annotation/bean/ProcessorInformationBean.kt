package com.smart.zwj.zroute_annotation.bean

data class ProcessorInformationBean(
    val packageName: String,
    val className: String,
    val parameterList:List<ParameterBean>
)

data class ParameterBean(val name:String,val key:String,val type:String)