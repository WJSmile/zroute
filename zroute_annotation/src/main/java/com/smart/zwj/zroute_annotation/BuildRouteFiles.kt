package com.smart.zwj.zroute_annotation

import com.smart.zwj.zroute_annotation.bean.ProcessorInformationBean
import com.smart.zwj.zroute_annotation.utils.Utils
import com.smart.zwj.zroute_processor.Route
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asTypeName

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
        Utils.functionsMap.clear()
        roundEnv?.getElementsAnnotatedWith(Route::class.java)?.forEachIndexed { index, element ->

            val processorInformationBean =
                Utils.getProcessorInformation(element, processingEnv)
            val route = element.getAnnotation(Route::class.java)
            funSpecBuilder.addStatement(
                "${Config.NAV_GRAPH_BUILDER}.%T(\"${route.path}\"){${Config.NAV_BACK_STACK_ENTRY}->\n${getArguments(processorInformationBean)}}",
                ClassName("com.google.accompanist.navigation.animation", "composable"),
                ClassName(processorInformationBean.packageName,
                    processorInformationBean.className.ifEmpty { processorInformationBean.funName }),
            )
        }

        fileSpecBuilder.addType(
            TypeSpec.classBuilder(Config.FILE_NAME)
                .addAnnotation(ClassName("androidx.compose.animation","ExperimentalAnimationApi"))
                .addFunction(funSpecBuilder.build()).build()
        )


        fileSpecBuilder.build().writeTo(processingEnv.filer)
    }

    private fun getArguments(processorInformationBean: ProcessorInformationBean):String{

        var code:String

        if (processorInformationBean.parameterList.isEmpty()){
            code = "%T()"
        }else{
            code = if (processorInformationBean.className.isEmpty()){
                "${Config.NAV_BACK_STACK_ENTRY}.arguments?.apply {%T("
            }else{
                "${Config.NAV_BACK_STACK_ENTRY}.arguments?.apply {\n%T().${processorInformationBean.funName}("
            }
            processorInformationBean.parameterList.forEach {
                System.err.println(it.type)
                when(it.type){
                    String::class.asTypeName().toString(),
                    java.lang.String::class.java.name.toString()->{
                        code +=  "${it.name}=getString(\"${it.name}\",\"\")"
                    }
                    Int::class.asTypeName().toString(),
                    java.lang.Integer::class.java.name.toString()->{
                        code += "${it.name}=getInt(\"${it.name}\",0)"
                    }
                    Long::class.asTypeName().toString(),
                    java.lang.Long::class.java.name.toString()->{
                        code += "${it.name}=getLong(\"${it.name}\",0L)"
                    }
                    Float::class.asTypeName().toString(),
                    java.lang.Float::class.java.name.toString()->{
                        code += "${it.name}=getFloat(\"${it.name}\",0F)"
                    }
                    Double::class.asTypeName().toString(),
                    java.lang.Double::class.java.name.toString()->{
                        code += "${it.name}=getDouble(\"${it.name}\",0.0)"
                    }
                    Boolean::class.asTypeName().toString(),
                    java.lang.Boolean::class.java.name.toString()->{
                        code += "${it.name}=geBoolean(\"${it.name}\",false)"
                    }

                    "java.util.List<java.lang.Integer>"->{
                        code += "${it.name}=getIntegerArrayList(\"${it.name}\")"
                    }
                }
            }
        }
        return "${code})}"
    }

}