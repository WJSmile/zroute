package com.smart.zwj.zroute.plugin.task

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
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream


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

        transformCode.collectClass(jars, dirs)


        JarOutputStream(BufferedOutputStream(FileOutputStream(output.get().asFile))).use { jarOutput ->
            jars.get().forEach { file ->

                val jarFile = JarFile(file.asFile)

                jarFile.entries().iterator().forEach { jarEntry ->
                    if (jarEntry.isDirectory.not() &&
                        jarEntry.name.contains(Config.MODULE_CLASS.toInternalName(), true)
                    ) {
                       transformCode.insertion(routeList, jarOutput, jarEntry)
                    } else {
                        kotlin.runCatching {
                            jarOutput.putNextEntry(JarEntry(jarEntry.name))
                            jarFile.getInputStream(jarEntry).use {
                                it.copyTo(jarOutput)
                            }
                        }
                    }

                    jarOutput.closeEntry()
                }
                jarFile.close()
            }

            dirs.get().forEach { directory ->
                directory.asFile.walk().forEach { file ->
                    if (file.isFile) {
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