package io.github.lizhangqu.api.detect

import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * @author lizhangqu* @version V1.0* @since 2019-03-22 12:52
 */
class ApiDetectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.getExtensions().create("api", ApiExtension.class)
        BaseExtension android = project.getExtensions().getByType(BaseExtension.class)
        def transform = new CustomClassTransform(project, ApiDetectTransform.class)
        android.registerTransform(transform)

    }
}