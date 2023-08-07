package com.smart.zwj.zroute_annotation

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.validate
import com.google.gson.Gson
import com.smart.zwj.zroute_annotation.bean.ZRouteItem
import com.smart.zwj.zroute_processor.Route
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo


class ZRouteProcessor(private val environment: SymbolProcessorEnvironment) : SymbolProcessor {

    private val zRouteItem = ZRouteItem()

    @KspExperimental
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val zRouteSymbol = Route::class.qualifiedName?.let { resolver.getSymbolsWithAnnotation(it) }
        val ret = zRouteSymbol?.filter { !it.validate() }?.toList() ?: emptyList()

        val list = zRouteSymbol
            ?.filter { it is KSFunctionDeclaration && it.validate() }
            ?.map { it as KSFunctionDeclaration }?.toList()

        val fileName = environment.options[Config.MODULE_NAME] + "$" + Config.FILE_NAME

        val fileSpecBuilder = FileSpec.builder(
            Config.PACKAGE_NAME,
            fileName
        )
        zRouteItem.className = Config.PACKAGE_NAME + "." + fileName
        zRouteItem.funName = Config.FUN_NAME
        zRouteItem.paths = mutableListOf()
        val funSpecBuilder = FunSpec.builder(Config.FUN_NAME)
            .addParameter(
                Config.NAV_HOST_CONTROLLER,
                ClassName("androidx.navigation", "NavHostController")
            )
            .addParameter(
                Config.NAV_GRAPH_BUILDER,
                ClassName("androidx.navigation", "NavGraphBuilder")
            )


        list?.forEach { ksFunction ->

            val route = ksFunction.getAnnotationsByType(Route::class).toList()[0]
            zRouteItem.paths.add(route.path)
            funSpecBuilder.addStatement(
                "${Config.NAV_GRAPH_BUILDER}.%T(\"${route.path}\"){${Config.NAV_BACK_STACK_ENTRY}->\n" +
                        writeContent(ksFunction) +
                        "}",
                ClassName("com.google.accompanist.navigation.animation", "composable"),
                getClassName(ksFunction),
            )

        }
        fileSpecBuilder
            .addType(
                TypeSpec.classBuilder(fileName)
                    .addAnnotation(
                        ClassName(
                            "androidx.compose.animation",
                            "ExperimentalAnimationApi"
                        )
                    )
                    .addFunction(funSpecBuilder.build()).build()
            )

        try {
            fileSpecBuilder.build().writeTo(environment.codeGenerator, false)
            environment.codeGenerator.createNewFileByPath(
                Dependencies.ALL_FILES,
                Config.META_PATH,
                ""
            ).use {
                it.write(Gson().toJson(zRouteItem).toByteArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return ret
    }

    private fun writeContent(ksFunction: KSFunctionDeclaration): String {
        var contentCode = ""
        if (ksFunction.parent is KSFile) {
            contentCode += "%T("
        } else if (ksFunction.parent is KSClassDeclaration) {
            contentCode += "%T().${ksFunction.simpleName.asString()}("
        }

        ksFunction.parameters.forEach {
            contentCode += getKSValueParameter(it)
        }
        contentCode += ")"
        return contentCode
    }

    private fun getKSValueParameter(
        ksValueParameter: KSValueParameter,
        codeItem: String = "${Config.NAV_BACK_STACK_ENTRY}.arguments?."
    ): String {
        var contentCode = ""

        if (!ksValueParameter.hasDefault) {
            var typeName = ksValueParameter.type.toTypeName().toString()
            if (typeName.endsWith("?")) {
                typeName = typeName.substring(0, typeName.length - 1)
            }
            when (typeName) {
                String::class.asTypeName().toString() -> {
                    contentCode += if (ksValueParameter.type.toTypeName().isNullable) {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getString(\"${
                            ksValueParameter.name?.asString().toString()
                        }\"),"
                    } else {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getString(\"${
                            ksValueParameter.name?.asString().toString()
                        }\",\"\")?:\"\","
                    }
                }

                Int::class.asTypeName().toString() -> {
                    contentCode += if (ksValueParameter.type.toTypeName().isNullable) {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getInt(\"${ksValueParameter.name?.asString().toString()}\"),"
                    } else {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getInt(\"${
                            ksValueParameter.name?.asString().toString()
                        }\",0)?:0,"
                    }
                }

                Float::class.asTypeName().toString() -> {
                    contentCode += if (ksValueParameter.type.toTypeName().isNullable) {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getFloat(\"${ksValueParameter.name?.asString().toString()}\"),"
                    } else {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getFloat(\"${
                            ksValueParameter.name?.asString().toString()
                        }\",0F)?:0F,"
                    }
                }

                Long::class.asTypeName().toString() -> {
                    contentCode += if (ksValueParameter.type.toTypeName().isNullable) {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getLong(\"${ksValueParameter.name?.asString().toString()}\"),"
                    } else {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getLong(\"${
                            ksValueParameter.name?.asString().toString()
                        }\",0L)?:0L,"
                    }
                }

                "kotlin.collections.List<kotlin.String>",
                "kotlin.collections.ArrayList<kotlin.String>" -> {
                    contentCode += if (ksValueParameter.type.toTypeName().isNullable) {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getStringArrayList(\"${
                            ksValueParameter.name?.asString().toString()
                        }\"),"
                    } else {
                        "${
                            ksValueParameter.name?.asString().toString()
                        }=${codeItem}getStringArrayList(\"${
                            ksValueParameter.name?.asString().toString()
                        }\")?:emptyList(),"
                    }
                }

                "androidx.navigation.NavHostController" -> {
                    contentCode += "${
                        ksValueParameter.name?.asString().toString()
                    }=${Config.NAV_HOST_CONTROLLER}"
                }

                "androidx.navigation.NavGraphBuilder" -> {
                    contentCode += "${
                        ksValueParameter.name?.asString().toString()
                    }=${Config.NAV_GRAPH_BUILDER}"
                }
                "androidx.navigation.NavBackStackEntry"->{
                    contentCode += "${
                        ksValueParameter.name?.asString().toString()
                    }=${Config.NAV_BACK_STACK_ENTRY}"
                }
            }
        }
        return contentCode
    }

    private fun getClassName(ksFunction: KSFunctionDeclaration): ClassName {

        return when (ksFunction.parent) {
            is KSFile -> {
                ClassName(
                    (ksFunction.parent as KSFile).packageName.asString(),
                    ksFunction.simpleName.asString()
                )
            }

            is KSClassDeclaration -> {
                (ksFunction.parent as KSClassDeclaration).toClassName()
            }

            else -> {
                ClassName("", "")
            }
        }
    }

}