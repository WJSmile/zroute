package com.smart.zwj.zroute.plugin

import com.android.build.api.artifact.ScopedArtifact
import com.android.build.api.variant.AndroidComponentsExtension
import com.android.build.api.variant.ScopedArtifacts
import com.smart.zwj.zroute.plugin.task.TransformTask
import com.smart.zwj.zroute.plugin.utils.Config
import org.gradle.api.Plugin
import org.gradle.api.Project
import java.util.Locale

class ZRoutePlugin : Plugin<Project> {


    override fun apply(project: Project) {

        Config.build_path = project.buildDir.absolutePath+"/intermediates/zroute"
        project.extensions.getByType(AndroidComponentsExtension::class.java).onVariants { variant ->
            val name = "gather${variant.name}RouteTables"

            val taskProvider =
                project.tasks.register(name, TransformTask::class.java)

            variant.artifacts.forScope(ScopedArtifacts.Scope.ALL)
                .use(taskProvider)
                .toTransform(
                    ScopedArtifact.CLASSES, TransformTask::jars,
                    TransformTask::dirs,
                    TransformTask::output
                )
        }
    }

}