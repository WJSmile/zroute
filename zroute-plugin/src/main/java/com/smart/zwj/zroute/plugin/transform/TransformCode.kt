package com.smart.zwj.zroute.plugin.transform

import com.smart.zwj.zroute.plugin.bean.ZRouteItem
import com.smart.zwj.zroute.plugin.utils.Config
import com.smart.zwj.zroute_processor.Route
import javassist.ClassClassPath
import javassist.ClassPool
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


class TransformCode {
    private val pool = ClassPool.getDefault()

    fun appendClassPath(path: String) {
        pool.appendClassPath(path)
    }

    fun insertion(routeList: List<ZRouteItem>, jarOutput: JarOutputStream, jarEntry: JarEntry) {

        val writePath = File(Config.build_path)
        val filePath = File(writePath, "/" + jarEntry.name)
        writePath.deleteRecursively()

        val ctClass = pool.get(Config.MODULE_CLASS)
        val personFly = ctClass.getDeclaredMethod(Config.MODULE_FUN_NAME)
        routeList.forEach {
            personFly.insertBefore("{new ${it.className}().${it.funName}($1,$2);}")
        }

        Files.createDirectories(Paths.get(writePath.toURI()))
        ctClass.writeFile(writePath.absolutePath)


        jarOutput.putNextEntry(JarEntry(jarEntry.name))
        filePath.inputStream().use { inputStream ->
            inputStream.copyTo(jarOutput)
        }

        ctClass.detach()
    }

    fun collectClass(jars: ListProperty<RegularFile>, dirs: ListProperty<Directory>) {


        jars.get().forEach { regularFile ->
            pool.appendClassPath(regularFile.asFile.absolutePath)
            val jarFile = JarFile(regularFile.asFile)
            jarFile.entries().iterator().forEach { jarEntry ->
                if (jarEntry.name.endsWith(".class")) {
                    try {
                        val ctClass =
                            pool.get(jarEntry.name.replace("/", ".").replace(".class", ""))
                        ctClass.methods.forEach { ctMethod ->
                            if (ctMethod.hasAnnotation(Route::class.java.name)) {

                                val route = ctMethod.getAnnotation(Route::class.java)
                                if (route != null && route is Route) {
                                    System.err.println(">>>>" + ctMethod.name)
                                }
                            }
                        }
                        ctClass.detach()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
            jarFile.close()
        }
        dirs.get().forEach { directory ->
            pool.appendClassPath(directory.asFile.absolutePath)
            directory.asFile.walk().forEach { file ->
                if (file.isFile) {
                    val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                    if (relativePath.endsWith(".class")) {
                        try {
                            val ctClass =
                                pool.get(relativePath.replace("/", ".").replace(".class", ""))
                            ctClass.methods.forEach { ctMethod ->

                                if (ctMethod.hasAnnotation(Route::class.java)) {
                                    val route = ctMethod.getAnnotation(Route::class.java)
                                    if (route != null && route is Route) {

                                        ctMethod.parameterTypes.forEachIndexed {index,name->
                                          System.err.println(  name)
                                            ctMethod.parameterAnnotations[index].forEach {
                                                System.err.println(  it.toString())
                                            }
                                        }
                                    }
                                }
                            }
                            ctClass.detach()
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }

                    }
                }
            }
        }


    }

}