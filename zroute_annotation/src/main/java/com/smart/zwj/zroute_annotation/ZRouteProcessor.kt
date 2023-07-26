package com.smart.zwj.zroute_annotation

import com.google.auto.service.AutoService
import com.google.gson.Gson
import com.smart.zwj.zroute_annotation.bean.ZRouteItem
import com.smart.zwj.zroute_processor.Route
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement
import javax.tools.StandardLocation

@SupportedOptions(Config.MODULE_NAME)
@AutoService(Processor::class)
class ZRouteProcessor : AbstractProcessor() {

    private val buildRouteFiles: BuildRouteFiles = BuildRouteFiles()


    private val zRouteList:MutableList<ZRouteItem> = mutableListOf()
    override fun init(processingEnvironment: ProcessingEnvironment?) {
        super.init(processingEnvironment)
    }

    override fun process(p0: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv?.processingOver() == false) {
            buildRouteFiles.build(roundEnv, processingEnv,zRouteList)
        }else{
            processingEnv.filer.createResource(
                StandardLocation.CLASS_OUTPUT,
                "",
                Config.META_PATH
            ).openWriter().use {
                it.write(Gson().toJson(zRouteList))
            }
        }
        return true
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return mutableSetOf(Route::class.java.canonicalName)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }
}