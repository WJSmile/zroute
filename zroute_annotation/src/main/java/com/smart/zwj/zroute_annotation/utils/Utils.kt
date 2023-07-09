package com.smart.zwj.zroute_annotation.utils

import com.smart.zwj.zroute_annotation.bean.FunctionsData
import com.smart.zwj.zroute_annotation.bean.ParameterBean
import com.smart.zwj.zroute_annotation.bean.ProcessorInformationBean
import com.squareup.kotlinpoet.DelicateKotlinPoetApi
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.declaresDefaultValue
import com.squareup.kotlinpoet.metadata.isNullable
import com.squareup.kotlinpoet.metadata.toKmClass
import com.squareup.kotlinpoet.metadata.toKotlinClassMetadata
import kotlinx.metadata.KmFunction
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

object Utils {

    val functionsMap:MutableMap<String, FunctionsData> = hashMapOf()
    @OptIn(DelicateKotlinPoetApi::class, KotlinPoetMetadataPreview::class)
    fun getProcessorInformation(
        element: Element,
        processingEnv: ProcessingEnvironment
    ): ProcessorInformationBean {
        val packageName =
            processingEnv.elementUtils.getPackageOf(element.enclosingElement).qualifiedName.toString()

        val parameterList: MutableList<ParameterBean> = mutableListOf()
        val fileName = (element.enclosingElement as TypeElement).qualifiedName.toString()
        val funName = element.simpleName.toString()

        var functionsData:FunctionsData ?=null

        if (functionsMap.keys.contains(fileName)){
            functionsData = functionsMap[fileName]
        }else{
             try {
                functionsMap[fileName] = FunctionsData(element.enclosingElement.getAnnotation(Metadata::class.java)
                    .toKmClass().functions,0,element.enclosingElement.simpleName.toString())

            } catch (e: Exception) {
                functionsMap[fileName] = FunctionsData(element.enclosingElement.getAnnotation(Metadata::class.java)
                    .toKotlinClassMetadata<KotlinClassMetadata.FileFacade>()
                    .toKmPackage().functions,0,"")
                ""
            }
        }


        (element as ExecutableElement).parameters.forEachIndexed { index, variableElement ->

            parameterList.add(
                ParameterBean(
                    variableElement.toString(),
                    variableElement.asType().asTypeName().toString(),
                    functionsData?.num?.let { functionsData.functions[it].valueParameters[index].declaresDefaultValue }
                        ?:false,
                    functionsData?.num?.let { functionsData.functions[it].valueParameters[index].type.isNullable }
                        ?:false,
                )
            )
            functionsData?.apply {
                num++
            }
        }

        return ProcessorInformationBean(packageName, functionsData?.className?:"",funName, parameterList)
    }
}