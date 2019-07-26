package com.testContractBuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jeasy.random.EasyRandom;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

abstract class AbstractTestContractBuilder {
    private Map<String, String> contracts = new HashMap<>();
    Map<String, String> build(String packageToScan) {
        Reflections reflections = new Reflections(packageToScan, new SubTypesScanner(false));
        Set<Class<? extends Object>> allClasses = reflections.getSubTypesOf(Object.class);

        allClasses.forEach((Object clazz) -> {
            this.contracts.putAll(
                    generateContracts(
                            clazz.toString()
                                    .replace("class ","")));
        });
        return this.contracts;
    }

    private Map<String, String> generateContracts(String clazz) {
        String[] endPointUrlOfTheClass = getAnnotationFromTheClassLevel(clazz);

        Method[] methods = prepareClass(clazz).getDeclaredMethods();
        for (Method method : methods) {
            this.contracts = getEndpointUrlOfTheMethodAndPopulateContracts(method, endPointUrlOfTheClass);
        }
        return this.contracts;
    }

    private Map<String, String> getEndpointUrlOfTheMethodAndPopulateContracts(Method method, String[] endPointUrlOfTheClass) {
        String[] endPointUrlOfTheMethod =  getMethodAnnotationValue(method);
        if (endPointUrlOfTheMethod != null) {
            this.contracts = populateContract(endPointUrlOfTheMethod, endPointUrlOfTheClass, method);
        }
        return this.contracts;
    }

    private String[] getMethodAnnotationValue(Method method) {
        RequestMapping endPointUrlRequestMappingOfMethod = method.getAnnotation(RequestMapping.class);
        if (endPointUrlRequestMappingOfMethod != null){
            return endPointUrlRequestMappingOfMethod.value();
        }

        PostMapping endPointUrlPostMappingOfMethod = method.getAnnotation(PostMapping.class);
        if (endPointUrlPostMappingOfMethod != null){
            return endPointUrlPostMappingOfMethod.value();
        }

        GetMapping endPointUrlGetMappingOfMethod = method.getAnnotation(GetMapping.class);
        if (endPointUrlGetMappingOfMethod != null){
            return endPointUrlGetMappingOfMethod.value();
        }

        PutMapping endPointUrlPutMappingOfMethod = method.getAnnotation(PutMapping.class);
        if (endPointUrlPutMappingOfMethod != null){
            return endPointUrlPutMappingOfMethod.value();
        }

        return null;
    }

    private Map<String, String> populateContract(String[] endPointUrlOfTheMethod, String[] endPointUrlOfTheClass, Method method) {
        if (endPointUrlOfTheMethod != null && !"/tc".equals(endPointUrlOfTheMethod[0])) {
            Type returnParam = method.getGenericReturnType();
            this.contracts.put(endPointUrlOfTheClass[0] + endPointUrlOfTheMethod[0], getResponseJson(returnParam.getTypeName()));
        }
        return this.contracts;
    }

    private String[] getAnnotationFromTheClassLevel(String clazz) {
        String[] endPointUrlOfTheClass = {""};
        RequestMapping annotation = (RequestMapping) prepareClass(clazz).getAnnotation(RequestMapping.class);
        if (annotation != null)
            endPointUrlOfTheClass = annotation.value();
        return endPointUrlOfTheClass;
    }

    private Class prepareClass(String packageToScan) {
        Class aClass = null;
        try {
            aClass = Class.forName(packageToScan);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return aClass;
    }

    private String getResponseJson(String typeName) {
        EasyRandom generator = new EasyRandom();
        String[] responseElements = typeName.replace(">", "").split("<");

        if (getResponseElement(responseElements, 1).equals("java.util.List")) {
            return createJson(generator.objects(prepareClass(getResponseElement(responseElements, 2)), 1)
                    .collect(Collectors.toList()));
        }
        return createJson(generator.nextObject(prepareClass(getResponseElement(responseElements, 2))));
    }

    //ToDo: make this method more robust and generic
    private String getResponseElement(String[] responseElements, int i) {
        String collection;
        String object;

        if (responseElements != null && responseElements.length == 3) {
            collection = responseElements[1];
            object = responseElements[2];
        } else if (responseElements != null && responseElements.length == 2) {
            collection = responseElements[0];
            object = responseElements[1];
        } else if (responseElements != null && responseElements.length == 1) {
            collection = "";
            object = responseElements[0];
        } else {
            collection = "";
            object = "";
        }

        if (i == 1)
            return collection;
        return object;
    }

    private String createJson(Object o) {
        Gson gsonBuilder = new GsonBuilder().create();
        return gsonBuilder.toJson(o);
    }
}
