package com.smart.zwj.zroute_annotation.utils

import com.smart.zwj.zroute_annotation.bean.ParameterBean
import com.smart.zwj.zroute_annotation.bean.ProcessorInformationBean
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import com.squareup.kotlinpoet.metadata.toKmClass
import com.squareup.kotlinpoet.metadata.toKotlinClassMetadata
import com.zwj.zroute_annotation.Parameter
import kotlinx.metadata.KmClassifier
import kotlinx.metadata.KmFunction
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

object Utils {

    @OptIn(KotlinPoetMetadataPreview::class)
    fun getProcessorInformation(
        key: Int,
        element: Element,
        processingEnv: ProcessingEnvironment
    ): ProcessorInformationBean {
        val packageName =
            processingEnv.elementUtils.getPackageOf(element.enclosingElement).qualifiedName.toString()
        val fileName = (element.enclosingElement as TypeElement).qualifiedName.toString()

        val parameterList: MutableList<ParameterBean> = mutableListOf()
        var functions: MutableList<KmFunction>
        var className: String

        try {
            functions = element.enclosingElement.getAnnotation(Metadata::class.java)
                .toKmClass().functions
            className = fileName.split(".").last()
        } catch (e: Exception) {
            functions = element.enclosingElement.getAnnotation(Metadata::class.java)
                .toKotlinClassMetadata<KotlinClassMetadata.FileFacade>()
                .toKmPackage().functions
            className = ""
        }

        (element as ExecutableElement).parameters.forEachIndexed { index, variableElement ->
            functions[key].valueParameters[index].apply {
                if (variableElement.getAnnotation(Parameter::class.java) != null) {
                    parameterList.add(
                        ParameterBean(
                            name,
                            variableElement.getAnnotation(Parameter::class.java).key,
                            (type.classifier as KmClassifier.Class).name
                        )
                    )
                } else {
                    parameterList.add(
                        ParameterBean(
                            name,
                            "",
                            (type.classifier as KmClassifier.Class).name
                        )
                    )
                }
            }
        }

        return ProcessorInformationBean(packageName, className, parameterList)
    }
}