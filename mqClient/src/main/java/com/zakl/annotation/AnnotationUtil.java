package com.zakl.annotation;


import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Zakl
 * @version 1.0
 * @className AnnotationUtil
 * @date 6/4/2021
 * @desc todo
 **/
public class AnnotationUtil {


    public static <T> List<AnnotationMethodInfo<T>> scanAnnotationMethods(Class<T> annotationType, String packageName, boolean childPackage) throws IOException, ClassNotFoundException {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<String> classNames = getClassNames(childPackage, packageName);
        List<AnnotationMethodInfo<T>> ret = new ArrayList<>();
        for (String className : classNames) {
            Class<?> aClass = loader.loadClass(className);
            Method[] declaredMethods = aClass.getDeclaredMethods();

            for (Method method : declaredMethods) {
                for (Annotation annotation : method.getAnnotations()) {
                    if (annotation.annotationType().isAssignableFrom(annotationType)) {
                        ret.add(new AnnotationMethodInfo<>(((T) annotation), method));
                        break;
                    }
                }
            }
        }
        return ret;
    }


    private static List<String> getClassNames(boolean childPackage, String packageName) throws IOException {
        List<String> fileNames = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        Enumeration<URL> urls = loader.getResources(packagePath);
        while (urls.hasMoreElements()) {
            URL url = urls.nextElement();
            if (url == null)
                continue;
            String type = url.getProtocol();
            //支持开发阶段
            if (type.equals("file")) {
                fileNames.addAll(getAllClassNameByFile(new File(url.getPath()), childPackage, packageName));
            } else {
                fileNames.addAll(getClassNameByJar(url.getPath(), childPackage, packageName));
            }
        }
        return fileNames;
    }

    //获取类信息
    private static List<String> getAllClassNameByFile(File file, boolean flag, String packageName) {
        List<String> serviceLocationList = new ArrayList<>();
        File[] files = file.listFiles();
        if (files == null) {
            return serviceLocationList;
        }
        for (File f : files) {
            if (f.isFile()) {
                String path = f.getAbsolutePath();
                path = path.replaceAll("\\\\", ".");
                String fullName = path.substring(path.indexOf(packageName), path.length() - 6);
                serviceLocationList.add(fullName);
            } else {
                if (flag) {
                    serviceLocationList.addAll(getAllClassNameByFile(f, flag, packageName));
                }
            }
        }
        return serviceLocationList;
    }

    //获取jar包中的类信息
    private static List<String> getClassNameByJar(String jarPath, boolean childPackage, String packageName) {
        List<String> myClassName = new ArrayList<>();
        String[] jarInfo = jarPath.split("!");
        String jarFilePath = jarInfo[0].substring(jarInfo[0].indexOf("/"));
        try {
            JarFile jarFile = new JarFile(jarFilePath);
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry jarEntry = entries.nextElement();
                String entryName = jarEntry.getName();
                if (entryName.endsWith(".class")) {
                    String packagePath = packageName.replaceAll("\\.", "/");
                    if (entryName.contains(packagePath)) {
                        String className = entryName.replaceAll("/", ".").substring(entryName.indexOf(packagePath));
                        myClassName.add(className.substring(0, className.length() - 6));
                    }
                } else if (childPackage) {

                    //todo jar包内部jar的解析
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myClassName;
    }

}
