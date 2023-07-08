package com.smart.zwj.zroute_annotation

import com.google.auto.service.AutoService
import com.smart.zwj.zroute_processor.Route
import com.squareup.kotlinpoet.metadata.KotlinPoetMetadataPreview
import javax.annotation.processing.*
import javax.lang.model.SourceVersion
import javax.lang.model.element.TypeElement

@SupportedOptions(Config.MODULE_NAME)
@AutoService(Processor::class)
class ZRouteProcessor : AbstractProcessor() {

    private val buildRouteFiles: BuildRouteFiles = BuildRouteFiles()


    override fun init(processingEnvironment: ProcessingEnvironment?) {
        super.init(processingEnvironment)
    }

    override fun process(p0: MutableSet<out TypeElement>?, roundEnv: RoundEnvironment?): Boolean {
        if (roundEnv?.processingOver() == false) {
            buildRouteFiles.build(roundEnv, processingEnv)
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