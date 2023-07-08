package com.smart.zwj.zroute_annotation

import com.smart.zwj.zroute_annotation.bean.ParameterBean
import com.smart.zwj.zroute_annotation.utils.Utils
import com.smart.zwj.zroute_processor.Route
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec

import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment


class BuildRouteFiles {

    fun build(roundEnv: RoundEnvironment?, processingEnv: ProcessingEnvironment) {

        val module: String? = processingEnv.options[Config.MODULE_NAME]
        if (module.isNullOrEmpty()) {
            throw IllegalArgumentException("module_name cannot be empty")
        }

        val fileSpecBuilder = FileSpec.builder(
            Config.PACKAGE_NAME + "." + module,
            Config.FILE_NAME
        )

        val funSpecBuilder = FunSpec.builder(Config.FUN_NAME)
            .addParameter(
                Config.NAV_HOST_CONTROLLER,
                ClassName("androidx.navigation", "NavHostController")
            )
            .addParameter(
                Config.NAV_GRAPH_BUILDER,
                ClassName("androidx.navigation", "NavGraphBuilder")
            )


        roundEnv?.getElementsAnnotatedWith(Route::class.java)?.forEachIndexed { key, element ->

            val processorInformationBean =
                Utils.getProcessorInformation(key, element, processingEnv)
            processorInformationBean.parameterList
            val route = element.getAnnotation(Route::class.java)

            funSpecBuilder.addStatement(
                "${Config.NAV_GRAPH_BUILDER}.%T(\"${route.path}\",${getArguments(processorInformationBean.parameterList)})",
                ClassName("com.google.accompanist.navigation.animation", "composable"),
            )
        }

        fileSpecBuilder.addType(
            TypeSpec.classBuilder(Config.FILE_NAME)
                .addFunction(funSpecBuilder.build()).build()
        )


        fileSpecBuilder.build().writeTo(processingEnv.filer)
    }

    private fun getArguments(parameterList:List<ParameterBean>):String{
        var arguments = "arguments = listOf("
        parameterList.forEach {

        }
        return "$arguments)"
    }
}