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
import kotlinx.metadata.KmType
import kotlinx.metadata.KmValueParameter
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.annotation.Nullable
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

        var functionsData:FunctionsData ?

        if (functionsMap.keys.contains(fileName)){
            functionsData = functionsMap[fileName]
        }else{
             try {
                 functionsData = FunctionsData(element.enclosingElement.getAnnotation(Metadata::class.java)
                     .toKmClass().functions,0,element.enclosingElement.simpleName.toString())
                functionsMap[fileName] = functionsData

            } catch (e: Exception) {
                 functionsData =FunctionsData(element.enclosingElement.getAnnotation(Metadata::class.java)
                     .toKotlinClassMetadata<KotlinClassMetadata.FileFacade>()
                     .toKmPackage().functions,0,"")
                functionsMap[fileName] = functionsData
            }
        }

        (element as ExecutableElement).parameters.forEachIndexed { index, variableElement ->
            parameterList.add(
                ParameterBean(
                    variableElement.toString(),
                    variableElement.asType().asTypeName().toString(),
                    getDeclaresDefaultValue(functionsData,index),
                    isNullable(functionsData,index),
                )
            )
            functionsData?.apply {
                num++
            }
        }

        return ProcessorInformationBean(packageName, functionsData?.className?:"",funName, parameterList)
    }

    @OptIn(KotlinPoetMetadataPreview::class)
    fun getDeclaresDefaultValue(functionsData:FunctionsData?, index:Int):Boolean{
        if (functionsData?.num == null){
            return false
        }
        if (functionsData.functions[functionsData.num].valueParameters.isEmpty()){
            return false
        }
        return functionsData.functions[functionsData.num].valueParameters[index].declaresDefaultValue
    }

    @OptIn(KotlinPoetMetadataPreview::class)
    fun isNullable(functionsData:FunctionsData?, index:Int):Boolean{
        if (functionsData?.num == null){
            return false
        }
        if (functionsData.functions[functionsData.num].valueParameters.isEmpty()){
            return false
        }
        return functionsData.functions[functionsData.num].valueParameters[index].type.isNullable
    }
}