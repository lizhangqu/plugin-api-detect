package io.github.lizhangqu.api.detect

import com.android.build.gradle.BaseExtension
import com.android.build.gradle.api.BaseVariant
import com.android.build.gradle.internal.pipeline.TransformTask
import groovy.io.FileType
import io.github.lizhangqu.dexdeps.ClassRef
import io.github.lizhangqu.dexdeps.DexData
import io.github.lizhangqu.dexdeps.FieldRef
import io.github.lizhangqu.dexdeps.MethodRef
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.TaskCollection

/**
 * @author lizhangqu
 */
class ApiDetectPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.getExtensions().create("api", ApiExtension.class)
        BaseExtension android = project.getExtensions().getByType(BaseExtension.class)
        def transform = new CustomClassTransform(project, ApiDetectTransform.class, false)
        android.registerTransform(transform)


        def variants = null;
        if (project.plugins.hasPlugin('com.android.application')) {
            variants = project.android.getApplicationVariants()
        } else if (project.plugins.hasPlugin('com.android.library')) {
            variants = project.android.getLibraryVariants()
        }


        variants?.all { BaseVariant variant ->
            def variantName = variant.getName()
            def task = findTransformTaskByTransformName(project, variantName, ApiDetectTransform.class.getName().replace('.', '_'))
            //always execute
            task?.outputs?.upToDateWhen { false }

            def mergeJniLibsTask = findTransformTaskByTransformName(project, variantName, "mergeJniLibs")
            if (mergeJniLibsTask == null) {
                mergeJniLibsTask = project.getTasks().findByName(("merge${variantName.capitalize()}NativeLibs"))
            }
            if (mergeJniLibsTask) {
                mergeJniLibsTask.doLast {
                    Map<String, File> bundles = new HashMap<>()
                    mergeJniLibsTask.getOutputs().files.each {
                        if (it.isFile()) {
                            collectBundle(project, it, bundles)
                        } else if (it.isDirectory()) {
                            it.eachFileRecurse(FileType.FILES) {
                                collectBundle(project, it, bundles)
                            }
                        }
                    }
                    ApiExtension apiExtension = project.getExtensions().findByType(ApiExtension.class)
                    bundles.each { String md5, File bundleFile ->
                        List<DexData> dexDataList = DexData.open(bundleFile)
                        if (dexDataList != null && dexDataList.size() > 0) {
                            for (DexData dexData : dexDataList) {
                                ClassRef[] classRefs = dexData.getReferences()
                                for (ClassRef classRef : classRefs) {
                                    if (apiExtension.checkInPattern(classRef.toString())) {
                                        project.logger.error("----------------------------------------Class Reference Start----------------------------------------")
                                        project.logger.error("Class Reference: ")
                                        project.logger.error("        └> [Detect Class: ${classRef}]")
                                        project.logger.error("        └> [Detect File: ${bundleFile}]")
                                        project.logger.error("----------------------------------------Class Reference End------------------------------------------\n\n")
                                    }

                                    FieldRef[] fieldArray = classRef.getFieldArray()
                                    for (FieldRef fieldRef : fieldArray) {
                                        if (apiExtension.checkInPattern(fieldRef.getDeclaredClassDescriptorName())) {
                                            project.logger.error("----------------------------------------Field Reference Start----------------------------------------")
                                            project.logger.error("Class Reference: ")
                                            project.logger.error("        └> [Detect Class: ${fieldRef.getDeclaredClassName()}]")
                                            project.logger.error("        └> [Detect Field: ${fieldRef}]")
                                            project.logger.error("        └> [Detect File: ${bundleFile}]")
                                            project.logger.error("----------------------------------------Field Reference End------------------------------------------\n\n")
                                        }
                                    }
                                    MethodRef[] methodArray = classRef.getMethodArray()
                                    for (MethodRef methodRef : methodArray) {
                                        if (apiExtension.checkInMethodPattern(methodRef.toString())) {
                                            project.logger.error("----------------------------------------Method Reference Start----------------------------------------")
                                            project.logger.error("Class Reference: ")
                                            project.logger.error("        └> [Detect Class: ${methodRef.getDeclaredClassName()}]")
                                            project.logger.error("        └> [Detect Method: ${methodRef}]")
                                            project.logger.error("        └> [Detect File: ${bundleFile}]")
                                            project.logger.error("----------------------------------------Method Reference End------------------------------------------\n\n")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


    }

    static boolean collectBundle(Project project, File file, Map<String, File> bundles) {
        if (file.getParentFile().getName() != 'zip-cache' && isBundle(file)) {
            String bundleMd5 = file.withInputStream {
                //noinspection UnnecessaryQualifiedReference
                new java.security.DigestInputStream(it, java.security.MessageDigest.getInstance('MD5')).withStream {
                    it.eachByte {}
                    it.messageDigest.digest().encodeHex() as String
                }
            }
            bundles.put(bundleMd5, file)
            return true
        }
        return false
    }

    static Task findTransformTaskByTransformName(Project project, String variantName, String name) {
        TaskCollection<TransformTask> transformTasks = project.getTasks().withType(TransformTask.class)
        Task transformTask = null
        transformTasks.each {
            if (it.getVariantName() == variantName && it.getTransform().getName() == name) {
                transformTask = it
            }
        }
        return transformTask
    }

    static boolean isBundle(File file) {
        if (file == null) {
            return false
        }
        if (file.length() < 4) {
            return false
        }
        FileInputStream fileInputStream = null
        try {
            fileInputStream = new FileInputStream(file)
            byte[] buffer = new byte[4];
            int read = fileInputStream.read(buffer, 0, 4)
            //0x504b0304
            if (read == 4
                    && buffer[0] == (byte) 0x50
                    && buffer[1] == (byte) 0x4b
                    && buffer[2] == (byte) 0x03
                    && buffer[3] == (byte) 0x04) {
                return true
            }
        } catch (IOException e) {
            e.printStackTrace()
        } finally {
            fileInputStream?.close()
        }
        return false
    }

}