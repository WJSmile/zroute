package com.smart.zwj.zroute.plugin.transform

import com.smart.zwj.zroute.plugin.bean.ZRouteItem
import com.smart.zwj.zroute.plugin.utils.Config
import javassist.ClassPool
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.jar.JarEntry
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



}