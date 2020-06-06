/**
 * Copyright 2001-2005 The Apache Software Foundation.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scleropages.kapuas.openapi.provider.swagger;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.Graphs;
import com.google.common.graph.MutableGraph;
import com.google.common.graph.MutableValueGraph;
import com.google.common.graph.ValueGraphBuilder;
import com.google.common.primitives.Primitives;
import io.swagger.v3.oas.models.media.ArraySchema;
import io.swagger.v3.oas.models.media.BooleanSchema;
import io.swagger.v3.oas.models.media.DateTimeSchema;
import io.swagger.v3.oas.models.media.IntegerSchema;
import io.swagger.v3.oas.models.media.NumberSchema;
import io.swagger.v3.oas.models.media.ObjectSchema;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.media.StringSchema;
import org.apache.commons.lang3.ArrayUtils;
import org.scleropages.core.util.GenericTypes;
import org.scleropages.kapuas.openapi.annotation.ApiIgnore;
import org.scleropages.kapuas.openapi.annotation.ApiModel;
import org.scleropages.kapuas.openapi.annotation.ApiStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.core.MethodParameter;
import org.springframework.core.ResolvableType;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.beans.PropertyDescriptor;
import java.beans.Transient;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

/**
 * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#schema 工具类，提供java class metadata->oai schema转换.
 *
 * @author <a href="mailto:martinmao@icloud.com">Martin Mao</a>
 */
public abstract class SchemaUtil {

    private static final String[] IGNORE_PROPERTY_NAMES = new String[]{"class"};

    private static final String DEFAULT_SCHEMAS_PATH = "#/components/schemas/";

    private static final Logger logger = LoggerFactory.getLogger(SchemaUtil.class);


    public static final Schema createSchema(MethodParameter methodParameter, ResolveContext resolveContext) {
        ApiStrategy apiStrategy = AnnotationUtils.findAnnotation(methodParameter.getParameterType(), ApiStrategy.class);
        if (null != apiStrategy) {
            return createSchema(methodParameter, apiStrategy.ignorePropertyFieldNotFound(), resolveContext);
        }
        return createSchema(methodParameter, true, resolveContext);
    }


    public static final Schema createSchema(MethodParameter methodParameter, boolean ignorePropertyFieldNotFound, ResolveContext resolveContext) {
        Assert.notNull(methodParameter, "methodParameter must not be null.");
        Class<?> javaType = methodParameter.getParameterType();
        if (javaType.isInterface()) {
            ApiModel apiModel;
            if (methodParameter.getParameterIndex() > -1) {//参数实现类需要从参数注解上获取
                apiModel = methodParameter.getParameterAnnotation(ApiModel.class);
            } else {
                //返回值实现类需要从方法注释上获取
                apiModel = methodParameter.getMethodAnnotation(ApiModel.class);
            }
            Class concreteType = null != apiModel ? apiModel.value() : null;
            if (null != concreteType && ClassUtils.isAssignable(javaType, concreteType))
                javaType = apiModel.value();
        }
        return createSchema(javaType, methodParameter, null, ignorePropertyFieldNotFound, resolveContext, GraphBuilder.directed().allowsSelfLoops(false).build());
    }


    public static final Schema createSchema(Class javaType, boolean ignorePropertyFieldNotFound, ResolveContext resolveContext) {
        return createSchema(javaType, null, null, ignorePropertyFieldNotFound, resolveContext, GraphBuilder.directed().allowsSelfLoops(false).build());
    }

    public static final Schema createSchema(Class javaType, MethodParameter methodParameter, Field field, ResolveContext resolveContext) {
        return createSchema(javaType, methodParameter, field, true, resolveContext, GraphBuilder.directed().allowsSelfLoops(false).build());
    }


    private static final Schema createSchema(Class javaType, MethodParameter methodParameter, Field field, boolean ignorePropertyFieldNotFound, ResolveContext resolveContext, MutableGraph<Class> graph) {
        Assert.notNull(javaType, "javaType is required.");
        if (isBasicType(javaType)) {
            return createPrimitiveSchema(javaType);
        } else if (ClassUtils.isPrimitiveArray(javaType) || ClassUtils.isPrimitiveWrapperArray(javaType)) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setItems(createPrimitiveSchema(javaType.getComponentType()));
            return arraySchema;
        } else if (javaType.isArray()) {
            ArraySchema arraySchema = new ArraySchema();
            arraySchema.setItems(createSchema(javaType.getComponentType(), methodParameter, field, ignorePropertyFieldNotFound, resolveContext, graph));
            return arraySchema;
        } else if (ClassUtils.isAssignable(Collection.class, javaType)) {
            ArraySchema arraySchema = new ArraySchema();
            Class<?> itemType = null;
            if (null != field) {//从field获取集合泛型
                itemType = ResolvableType.forField(field).asCollection().resolveGeneric(0);
            }
            if (null == itemType && null != methodParameter) {//从方法参数获取集合泛型
                itemType = ResolvableType.forMethodParameter(methodParameter).asCollection().resolveGeneric(0);
            }
            if (null == itemType) {
                logger.warn("can not resolve generic type of parameter [{}] or field [{}] use object schema directly.", methodParameter, field);
                ObjectSchema objectSchema = new ObjectSchema();
                objectSchema.setFormat("object");
                return objectSchema;
            } else {
                arraySchema.setItems(createSchema(itemType, methodParameter, field, ignorePropertyFieldNotFound, resolveContext, graph));
            }
            return arraySchema;
        } else if (ClassUtils.isAssignable(Map.class, javaType)) {
            logger.warn("can not resolve java.util.Map of parameter [{}] or field [{}] use object schema directly.", methodParameter, field);
            ObjectSchema objectSchema = new ObjectSchema();
            objectSchema.setFormat("map");
            return objectSchema;
        } else {
            for (SchemaResolver schemaResolver : resolveContext.getSchemaResolvers()) {
                if (schemaResolver.support(javaType, methodParameter, field, resolveContext)) {
                    return schemaResolver.resolve(javaType, methodParameter, field, resolveContext);
                }
            }
            if (javaType.isInterface()) {
                ApiModel apiModel = AnnotationUtils.findAnnotation(javaType, ApiModel.class);
                if (null != apiModel) {
                    javaType = apiModel.value();
                }
            }
            //otherwise resolve as pojo bean.
            return computeObjectSchemaIfAbsent(javaType, readRuleInterfaceType(javaType, methodParameter), methodParameter, ignorePropertyFieldNotFound, resolveContext, graph);
        }
    }


    private static Schema computeObjectSchemaIfAbsent(Class javaType, Class ruleInterfaceType, MethodParameter methodParameter, boolean ignorePropertyFieldNotFound, ResolveContext resolveContext, MutableGraph<Class> graph) {

        Schema targetSchema = resolveContext.getSwaggerOpenApi().computeSchemaIfAbsent(javaType, ruleInterfaceType, (cls1, cls2) -> {
            ObjectSchema objectSchema = new ObjectSchema();
            PropertyDescriptor[] propertyDescriptors = BeanUtils.getPropertyDescriptors(javaType);
            for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
                String propertyName = propertyDescriptor.getName();
                Field propertyField = ReflectionUtils.findField(javaType, propertyName);
                if (readable(propertyField, propertyDescriptor, methodParameter, ignorePropertyFieldNotFound)) {
                    Class<?> propertyType = propertyDescriptor.getPropertyType();
                    if (isCycleDepends(graph, javaType, propertyType, propertyDescriptor)) {
                        logger.warn("detected cycle depends from {}.{} to {}. ignore to process.", javaType.getSimpleName(), propertyName, propertyType.getSimpleName());
                        continue;
                    }
                    if (propertyType.isInterface()) {
                        ApiModel apiModel = findFieldAnnotation(propertyField, propertyDescriptor, ApiModel.class);
                        if (null != apiModel) {
                            propertyType = apiModel.value();
                        }
                    }
                    Schema propertySchema = createSchema(propertyType, methodParameter, propertyField, ignorePropertyFieldNotFound, resolveContext, graph);
                    postSchemaCreation(propertySchema, methodParameter, propertyField, propertyDescriptor);
                    objectSchema.addProperties(propertyName, propertySchema);
                }
            }
            return objectSchema;
        });
        String targetSchemaName = javaType.equals(ruleInterfaceType) ? javaType.getName() : javaType.getName() + "." + ruleInterfaceType.getSimpleName();
        targetSchema.setName(targetSchemaName);
        ObjectSchema schemaRef = new ObjectSchema();
        schemaRef.$ref(DEFAULT_SCHEMAS_PATH + targetSchemaName);
        return schemaRef;
    }

    private static void postSchemaCreation(Schema schema, MethodParameter methodParameter, Field propertyField, PropertyDescriptor propertyDescriptor) {
        Class[] ignoreClasses = getIgnoreClasses(methodParameter);
        Class[] fieldRequired = null;
        NotNull notNull = findFieldAnnotation(propertyField, propertyDescriptor, NotNull.class);
        if (null != notNull) {
            if (ArrayUtils.isEmpty(notNull.groups())) {
                schema.nullable(false);
            } else {
                fieldRequired = notNull.groups();
            }
        } else {
            NotEmpty notEmpty = findFieldAnnotation(propertyField, propertyDescriptor, NotEmpty.class);
            if (null != notEmpty) {
                if (ArrayUtils.isEmpty(notEmpty.groups())) {
                    schema.nullable(false);
                } else {
                    fieldRequired = notEmpty.groups();
                }
            }
        }
        if (null != ignoreClasses && null != fieldRequired) {
            if (rulesMatch(fieldRequired, ignoreClasses)) {
                schema.nullable(false);
            }
        }
    }

    private static Class readRuleInterfaceType(Class javaType, MethodParameter methodParameter) {
        Class ruleInterfaceType = javaType;
        if (null != methodParameter && methodParameter.getParameterIndex() > -1) {//方法参数规则限定从参数注解获取
            ApiIgnore apiIgnore = methodParameter.getParameterAnnotation(ApiIgnore.class);
            if (null != apiIgnore) {
                Class<?>[] ruleInterfaceTypes = apiIgnore.value();
                if (ArrayUtils.isNotEmpty(ruleInterfaceTypes))
                    ruleInterfaceType = ruleInterfaceTypes[0];
            }
        } else if (null != methodParameter && methodParameter.getParameterIndex() == -1) {//方法返回值规则限定从方法注解获取
            ApiIgnore apiIgnore = methodParameter.getMethodAnnotation(ApiIgnore.class);
            if (null != apiIgnore) {
                Class<?>[] ruleInterfaceTypes = apiIgnore.value();
                if (ArrayUtils.isNotEmpty(ruleInterfaceTypes))
                    ruleInterfaceType = ruleInterfaceTypes[0];
            }
        }
        return ruleInterfaceType;
    }


    private static boolean readable(Field propertyField, PropertyDescriptor propertyDescriptor, MethodParameter methodParameter, boolean ignorePropertyFieldNotFound) {

        for (String ignoreProperty : IGNORE_PROPERTY_NAMES) {
            if (Objects.equals(propertyDescriptor.getName(), ignoreProperty))
                return false;
        }
        if (null != findFieldAnnotation(propertyField, propertyDescriptor, Transient.class))
            return false;
        if (!ignorePropertyFieldNotFound && null == propertyField)
            return false;
        ApiIgnore fieldIgnore = findFieldAnnotation(propertyField, propertyDescriptor, ApiIgnore.class);
        if (null != fieldIgnore && ArrayUtils.isEmpty(fieldIgnore.value()))//field上没有设置@ApiIgnore.values直接忽略.
            return false;

        Class[] ignoreClasses = getIgnoreClasses(methodParameter);

        //匹配参数@ApiIgnore 配置以及 field @ApiIgnore配置
        if (null != fieldIgnore && null != ignoreClasses) {
            if (rulesMatch(fieldIgnore.value(), ignoreClasses))
                return false;
        }

        //兼容 jsr303匹配参数@ApiIgnore 配置以及 field @Null配置
        Null nill = findFieldAnnotation(propertyField, propertyDescriptor, Null.class);
        if (null != nill && ArrayUtils.isEmpty(nill.groups()))//field上没有设置groups直接忽略.
            return false;
        if (null != nill && null != ignoreClasses) {
            if (rulesMatch(nill.groups(), ignoreClasses))
                return false;
        }
        return true;
    }

    private static Class[] getIgnoreClasses(MethodParameter methodParameter) {
        if (null != methodParameter) {
            ApiIgnore parameterIgnore =
                    methodParameter.getParameterIndex() > -1 ?
                            methodParameter.getParameterAnnotation(ApiIgnore.class) : //方法参数
                            methodParameter.getMethodAnnotation(ApiIgnore.class);//方法返回值
            if (null != parameterIgnore)
                return parameterIgnore.value();
        }
        return null;
    }

    private static boolean rulesMatch(Class<?>[] matchFrom, Class<?>[] matchTo) {
        for (Class from : matchFrom) {
            for (Class to : matchTo) {
                if (from.equals(to)) {//只要存在交集就匹配
                    return true;
                }
            }
        }
        return false;
    }


    private static <A> A findFieldAnnotation(Field field, PropertyDescriptor propertyDescriptor, Class<? extends Annotation> annotationClazz) {
        Annotation annotation = null;
        if (field != null)
            annotation = AnnotationUtils.findAnnotation(field, annotationClazz);
        if (annotation == null) {
            annotation = AnnotationUtils.findAnnotation(propertyDescriptor.getReadMethod(), annotationClazz);
        }
        return (A) annotation;
    }

    /**
     * https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.3.md#data-types
     *
     * @param javaType
     * @return
     */
    public static final Schema createPrimitiveSchema(Class javaType) {
        javaType = Primitives.wrap(javaType);
        if (javaType.isAssignableFrom(Long.class)) {
            IntegerSchema integerSchema = new IntegerSchema();
            integerSchema.setFormat("int64");
            return integerSchema;
        }
        if (javaType.isAssignableFrom(Integer.class)) {
            return new IntegerSchema();
        }
        if (javaType.isAssignableFrom(Float.class)) {
            NumberSchema numberSchema = new NumberSchema();
            numberSchema.setFormat("float");
            return numberSchema;
        }
        if (javaType.isAssignableFrom(Double.class)) {
            NumberSchema numberSchema = new NumberSchema();
            numberSchema.setFormat("double");
            return numberSchema;
        }
        if (javaType.isAssignableFrom(String.class)) {
            return new StringSchema();
        }
        if (javaType.isAssignableFrom(Boolean.class)) {
            return new BooleanSchema();
        }
        if (javaType.isAssignableFrom(Date.class)) {
            return new DateTimeSchema();
        }
        if (javaType.isAssignableFrom(Byte.class)) {
            return new StringSchema();
        }
        throw new IllegalArgumentException("unsupported primitive type: " + javaType);
    }


    private static boolean isCycleDepends(MutableGraph<Class> dependsGraph, Class refFrom, Class refTo, PropertyDescriptor propertyDescriptor) {

        if (isBasicType(refTo))
            return false;
        if (refTo.isArray()) {
            return isCycleDepends(dependsGraph, refFrom, refTo.getComponentType(), propertyDescriptor);
        }
        if (ClassUtils.isAssignable(Collection.class, refTo)) {
            Class<?> resolveGeneric = GenericTypes.getMethodReturnGenericType(refFrom, propertyDescriptor, 0);
            if (null != resolveGeneric)
                return isCycleDepends(dependsGraph, refFrom, resolveGeneric, propertyDescriptor);
        }
        if (ClassUtils.isAssignable(Map.class, refTo)) {
            return false;
        }
        try {
            if (!dependsGraph.putEdge(refFrom, refTo)) {
                return true;
            }
        } catch (UnsupportedOperationException | IllegalArgumentException e) {//allowsSelfLoops=false
            return true;
        }
        if (Graphs.hasCycle(dependsGraph)) {
            dependsGraph.removeEdge(refFrom, refTo);
            return true;
        }
        return false;
    }


    public static boolean isBasicType(Class javaType) {
        return ClassUtils.isPrimitiveOrWrapper(javaType) || ClassUtils.isAssignable(String.class, javaType) || ClassUtils.isAssignable(Date.class, javaType);
    }


    public static void main(String[] args) {

        MutableGraph<Object> mutableGraph = GraphBuilder.directed().allowsSelfLoops(false).build();

        System.out.println(mutableGraph.addNode("root"));
        System.out.println(mutableGraph.putEdge("root", "a"));
        System.out.println(mutableGraph.putEdge("a", "b"));
        System.out.println(mutableGraph.putEdge("a", "c"));
        System.out.println(mutableGraph.putEdge("b", "c"));
        System.out.println(mutableGraph.putEdge("c", "d"));
        System.out.println(mutableGraph.putEdge("d", "b"));
        System.out.println(mutableGraph.putEdge("d", "e"));
        System.out.println(mutableGraph.putEdge("b", "e"));
        System.out.println(mutableGraph.putEdge("e", "a"));
        System.out.println(Graphs.hasCycle(mutableGraph));
        System.out.println(mutableGraph.hasEdgeConnecting("a", "c"));//a,c是否存在直接连接
        System.out.println(mutableGraph.adjacentNodes("a"));//返回a所有相邻节点
        System.out.println(mutableGraph.incidentEdges("a"));//返回a所有直接连线
        System.out.println("-------------");
        System.out.println(mutableGraph.predecessors("b"));//返回b直接前置节点
        System.out.println(mutableGraph.successors("b"));//返回b直接后续节点
        System.out.println(mutableGraph.inDegree("d"));//返回d入口连线数
        System.out.println(mutableGraph.outDegree("d"));//返回d出口连线数
        System.out.println("-------------");
        System.out.println(Graphs.reachableNodes(mutableGraph, "b"));//返回b所有可以达到的节点


        MutableValueGraph<String, String> mutableValueGraph = ValueGraphBuilder.directed().allowsSelfLoops(false).build();

        System.out.println(mutableValueGraph.putEdgeValue("a", "b", "a->b"));
        System.out.println(mutableValueGraph.putEdgeValue("a", "c", "a->c"));
        System.out.println(mutableValueGraph.putEdgeValue("b", "c", "b->c"));
        System.out.println(mutableValueGraph.putEdgeValue("c", "d", "c->d"));
        System.out.println(mutableValueGraph.putEdgeValue("d", "b", "d->b"));
        System.out.println(Graphs.hasCycle(mutableValueGraph.asGraph()));
        System.out.println(mutableValueGraph.hasEdgeConnecting("a", "c"));
        System.out.println(mutableValueGraph);

    }
}
