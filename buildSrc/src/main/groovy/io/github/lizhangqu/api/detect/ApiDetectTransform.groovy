package io.github.lizhangqu.api.detect

import javassist.CannotCompileException
import javassist.ClassPool
import javassist.CtClass
import javassist.expr.ExprEditor
import javassist.expr.MethodCall
import org.gradle.api.Project

/**
 * api detect
 */
class ApiDetectTransform implements Consumer<InputStream, OutputStream> {

    private Project project
    private Map<String, ClassPool> classPoolMap = new HashMap<>()
    private ApiExtension apiExtension = null

    ApiDetectTransform(Project project) {
        this.project = project
    }

    @Override
    void accept(String variantName, String path, InputStream inputStream, OutputStream outputStream) {


        ClassPool classPool = classPoolMap.get(variantName)
        if (classPool == null) {
            classPool = new ClassPool(true)
            TransformHelper.updateClassPath(classPool, project, variantName)
            classPoolMap.put(variantName, classPool)
        }
        if (apiExtension == null) {
            apiExtension = project.getExtensions().findByType(ApiExtension.class)
        }

        CtClass ctClass = classPool.makeClass(inputStream, false)
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }

        detect(ctClass)

        TransformHelper.copy(new ByteArrayInputStream(ctClass.toBytecode()), outputStream)
    }

    void detect(CtClass ctClass) {
        try {
            //方法调用
            ctClass?.getDeclaredMethods()?.each {
                it.instrument(new ExprEditor() {
                    @Override
                    void edit(MethodCall methodCall) throws CannotCompileException {
                        super.edit(methodCall)
                        boolean result = apiExtension.checkInMethodPattern("${methodCall.getClassName()}.${methodCall.getMethodName()}${methodCall.getSignature()}")
                        if (result) {
                            project.logger.error("----------------------------------------Method Reference Start----------------------------------------")
                            project.logger.error("Method Reference: ")
                            project.logger.error("        └> [Detect Class: ${methodCall.getClassName()}]")
                            project.logger.error("        └> [Detect Method: ${methodCall.getMethodName()}${methodCall.getSignature()}]")
                            project.logger.error("        └> [Referenced By Class: ${ctClass.getName()}, Line: ${methodCall.getLineNumber()}]")
                            project.logger.error("----------------------------------------Method Reference End------------------------------------------\n\n")
                        }
                    }
                })
            }

            //不是自己的包中调用再检测类引用
            if (!apiExtension.checkInPattern(ctClass.getName())) {
                ctClass?.getRefClasses()?.each { String name ->
                    if (apiExtension.checkInPattern(name)) {
                        project.logger.error("----------------------------------------Class Reference Start----------------------------------------")
                        project.logger.error("Class Reference: ")
                        project.logger.error("        └> [Detect Class: ${name}]")
                        project.logger.error("        └> [Referenced By Class: ${ctClass.getName()}")
                        project.logger.error("----------------------------------------Class Reference End------------------------------------------\n\n")
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace()
        }
    }

}
