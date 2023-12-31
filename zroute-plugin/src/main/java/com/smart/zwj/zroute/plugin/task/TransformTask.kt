package com.smart.zwj.zroute.plugin.task

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smart.zwj.zroute.plugin.bean.ZRouteItem
import com.smart.zwj.zroute.plugin.transform.TransformCode
import com.smart.zwj.zroute.plugin.utils.Config
import org.gradle.api.DefaultTask
import org.gradle.api.file.Directory
import org.gradle.api.file.RegularFile
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipFile


abstract class TransformTask : DefaultTask() {

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val jars: ListProperty<RegularFile>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val dirs: ListProperty<Directory>

    @get:OutputFile
    abstract val output: RegularFileProperty


    private val transformCode = TransformCode()

    private fun String.toInternalName() = replace('.', '/')



    @TaskAction
    fun taskAction() {

        val routeList = mutableListOf<ZRouteItem>()

        jars.get().forEach { file ->
            transformCode.appendClassPath(file.asFile.absolutePath)
            ZipFile(file.asFile).use { zipFile ->
                zipFile.getEntry(Config.META_PATH)?.let { e ->
                    val zRouteItems: ZRouteItem= Gson().fromJson(
                        zipFile.getInputStream(e).reader(),
                        object : TypeToken<ZRouteItem>() {}.type
                    )
                    routeList.add(zRouteItems)
                }
            }
        }
        dirs.get().forEach { directory ->
            transformCode.appendClassPath(directory.asFile.absolutePath)
            val metaFile = File(directory.asFile, Config.META_PATH.replace('/', File.separatorChar))
            if (metaFile.exists()) {
                metaFile.reader().use {
                    val zipFileBean:ZRouteItem =
                        Gson().fromJson(it, object : TypeToken<ZRouteItem>() {}.type)
                    routeList.add(zipFileBean)
                }
            }
        }

        JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile))).use { jarOutput ->
            jars.get().forEach { file ->
                transformCode.appendClassPath(file.asFile.absolutePath)
                val jarFile = JarFile(file.asFile)

                jarFile.entries().iterator().forEach { jarEntry ->
                    if (jarEntry.isDirectory.not() &&
                        jarEntry.name.contains(Config.MODULE_CLASS.toInternalName(), true)
                    ) {
                       transformCode.insertion(routeList, jarOutput, jarEntry)
                    } else {
                        if (!jarEntry.name.endsWith(Config.META_PATH)){
                            kotlin.runCatching {
                                jarOutput.putNextEntry(JarEntry(jarEntry.name))
                                jarFile.getInputStream(jarEntry).use {
                                    it.copyTo(jarOutput)
                                }
                            }
                        }

                    }

                    jarOutput.closeEntry()
                }
                jarFile.close()
            }

            dirs.get().forEach { directory ->
                directory.asFile.walk().forEach { file ->
                    if (file.isFile&&!file.name.endsWith(Config.META_PATH)) {
                        val relativePath = directory.asFile.toURI().relativize(file.toURI()).path
                        jarOutput.putNextEntry(
                            JarEntry(
                                relativePath
                            )
                        )
                        file.inputStream().use { inputStream ->
                            inputStream.copyTo(jarOutput)
                        }
                        jarOutput.closeEntry()
                    }
                }
            }

        }

    }


}