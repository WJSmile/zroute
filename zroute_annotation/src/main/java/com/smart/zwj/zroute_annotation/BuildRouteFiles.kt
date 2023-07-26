package com.smart.zwj.zroute_annotation

import com.smart.zwj.zroute_annotation.bean.ProcessorInformationBean
import com.smart.zwj.zroute_annotation.bean.ZRouteItem
import com.smart.zwj.zroute_annotation.utils.Utils
import com.smart.zwj.zroute_processor.Route
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.metadata.toKmClass

import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.RoundEnvironment


class BuildRouteFiles {

    fun build(
        roundEnv: RoundEnvironment?,
        processingEnv: ProcessingEnvironment,
        zRouteList: MutableList<ZRouteItem>
    ) {

        val module: String? = processingEnv.options[Config.MODULE_NAME]
        if (module.isNullOrEmpty()) {
            throw IllegalArgumentException("module_name cannot be empty")
        }

        val fileName = module + "$" + Config.FILE_NAME
        val paths: MutableList<String> = mutableListOf()

        val fileSpecBuilder = FileSpec.builder(
            Config.PACKAGE_NAME,
            fileName
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
        Utils.functionsMap.clear()
        roundEnv?.getElementsAnnotatedWith(Route::class.java)?.forEachIndexed { _, element ->

            val processorInformationBean =
                Utils.getProcessorInformation(element, processingEnv)
            val route = element.getAnnotation(Route::class.java)
            paths.add(route.path)
            funSpecBuilder.addStatement(
                "${Config.NAV_GRAPH_BUILDER}.%T(\"${route.path}\"){${Config.NAV_BACK_STACK_ENTRY}->\n${
                    getArguments(
                        processorInformationBean
                    )
                }}",
                ClassName("com.google.accompanist.navigation.animation", "composable"),
                ClassName(processorInformationBean.packageName,
                    processorInformationBean.className.ifEmpty { processorInformationBean.funName }),
            )
        }

        fileSpecBuilder.addType(
            TypeSpec.classBuilder(fileName)
                .addAnnotation(ClassName("androidx.compose.animation", "ExperimentalAnimationApi"))
                .addFunction(funSpecBuilder.build()).build()
        )


        fileSpecBuilder.build().writeTo(processingEnv.filer)
        zRouteList.add(ZRouteItem().apply {
            className = Config.PACKAGE_NAME + "." + fileName
            funName = Config.FUN_NAME
            this.paths = paths
        })
    }

    private fun getArguments(processorInformationBean: ProcessorInformationBean): String {

        var code: String

        if (processorInformationBean.parameterList.isEmpty()) {
            code = "%T().${processorInformationBean.funName}()"
        } else {
            val codeItem = "${Config.NAV_BACK_STACK_ENTRY}.arguments?."
            code = if (processorInformationBean.className.isEmpty()) {
                "%T("
            } else {
                "%T().${processorInformationBean.funName}("
            }
            processorInformationBean.parameterList.forEach {
                if (!it.declaresDefaultValue) {
                    System.err.println(it.type)
                    when (it.type) {
                        java.lang.String::class.java.name.toString() -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}getString(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}getString(\"${it.name}\",\"\")"
                            }
                        }

                        Int::class.asTypeName().toString(),
                        java.lang.Integer::class.java.name.toString() -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}getInt(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}getInt(\"${it.name}\",0)"
                            }

                        }

                        Long::class.asTypeName().toString(),
                        java.lang.Long::class.java.name.toString() -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}getLong(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}getLong(\"${it.name}\",0L)"
                            }
                        }

                        Float::class.asTypeName().toString(),
                        java.lang.Float::class.java.name.toString() -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}getFloat(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}getFloat(\"${it.name}\",0F)"
                            }
                        }

                        Double::class.asTypeName().toString(),
                        java.lang.Double::class.java.name.toString() -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}getDouble(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}getDouble(\"${it.name}\",0.0)"
                            }
                        }

                        Boolean::class.asTypeName().toString(),
                        java.lang.Boolean::class.java.name.toString() -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}geBoolean(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}geBoolean(\"${it.name}\",false)"
                            }
                        }

                        "java.util.List<java.lang.Integer>" -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}getIntegerArrayList(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}getIntegerArrayList(\"${it.name}\")?:listOf()"
                            }

                        }

                        "java.util.List<java.lang.String>" -> {
                            code += if (it.isNullable) {
                                "${it.name}=${codeItem}getStringArrayList(\"${it.name}\")"
                            } else {
                                "${it.name}=${codeItem}getStringArrayList(\"${it.name}\")?:listOf()"
                            }

                        }

                        "androidx.navigation.NavHostController" -> {
                            code += "${it.name}=${Config.NAV_HOST_CONTROLLER}"
                        }

                        "androidx.navigation.NavGraphBuilder" -> {
                            code +="${it.name}=${Config.NAV_GRAPH_BUILDER}"
                        }

                        else -> {

                        }
                    }
                }
            }
            code = "${code})"
        }
        return code
    }

}