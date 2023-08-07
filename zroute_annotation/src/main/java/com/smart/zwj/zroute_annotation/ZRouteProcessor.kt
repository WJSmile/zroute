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
    private val builtInRoute =BuiltInRoute(environment,zRouteItem)

    @KspExperimental
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val zRouteSymbol = Route::class.qualifiedName?.let { resolver.getSymbolsWithAnnotation(it) }
        val ret = zRouteSymbol?.filter { !it.validate() }?.toList() ?: emptyList()

        val list = zRouteSymbol
            ?.filter { it is KSFunctionDeclaration && it.validate() }
            ?.map { it as KSFunctionDeclaration }?.toList()


        try {
            builtInRoute.getFunSpec(list).writeTo(environment.codeGenerator, false)
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





}