package com.zjb.ruleengine.core.value;

import cn.hutool.core.text.StrFormatter;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.google.common.collect.Sets;
import com.zjb.ruleengine.core.Context;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.exception.RuleEngineException;
import com.zjb.ruleengine.core.exception.RuleExecuteException;
import com.zjb.ruleengine.core.function.Function;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * @author 赵静波
 * @date 2020-09-30 16:15:29
 */
public class Variable extends Value {

    private static final Logger log = LogManager.getLogger();
    /**
     * 变量的方法
     */
    private VariableFunction function;

    private FunctionHolder functionHolder;

    public Variable(VariableFunction function, FunctionHolder functionHolder) {
        super();
        Validate.notNull(function);
        Validate.notNull(functionHolder);
        this.functionHolder = functionHolder;
        if (!functionHolder.containFunction(function.getFunctionName())) {
            final String message = StrFormatter.format("not found function:{}", function.getFunctionName());
            log.error(message);
            throw new RuleEngineException(message);
        }
        final Function functionBean = functionHolder.getFunction(function.getFunctionName());
        final Class functionParamType = functionBean.getFunctionParamType();
        //functionHolder.getFunction(((Variable) function.getParameter()).function.getFunctionName()).getFunctionResultType()
        final Object parameter = function.getParameter();
        if (parameter instanceof Value) {
            if (!functionParamType.isAssignableFrom(((Value) parameter).getResultType())) {
                final String message = StrFormatter.format("function:{} parameter {} not cast to {}", function.getFunctionName(), ((Value) parameter).getResultType(), functionParamType);
                log.error(message);
                throw new RuleEngineException(message);
            }

        } else if (!parameter.getClass().isAssignableFrom(functionParamType)) {
            final String message = StrFormatter.format("function:{} parameter {} not cast to {}", function.getFunctionName(), function.getParameter().getClass().getSimpleName(), functionParamType);
            log.error(message);
            throw new RuleEngineException(message);
        }
        this.function = function;
    }

    @Override
    public Class getResultType() {
        return functionHolder.getFunction(this.function.getFunctionName()).getFunctionResultType();
    }

    @Override
    public Collection<Element> collectParameter() {
        final Object parameter = function.getParameter();
        return recursion(parameter);
    }

    private Set<Element> recursion(Object parameter) {
        final Class<?> parameterClass = parameter.getClass();
        if (ClassUtil.isSimpleValueType(parameterClass)) {
            return Sets.newHashSet();
        }
        final Set<Element> result = Sets.newHashSet();
        if (parameter instanceof Map) {
            final Map parameterMap = (Map) parameter;
            parameterMap.forEach((k, v) -> {
                result.addAll(recursion(k));
                result.addAll(recursion(v));
            });
            return result;
        }
        if (parameter instanceof Collection) {
            ((Collection<?>) parameter).stream().forEach(p -> result.addAll(recursion(p)));
            return result;
        }
        if (parameter instanceof Constant || parameter instanceof Element) {
            result.addAll(((Element) parameter).collectParameter());
            return result;
        }
        if (parameter instanceof Variable) {
            final Variable variable = (Variable) parameter;
            result.addAll(recursion(variable.function.getParameter()));
            return result;
        }
        final Field[] declaredFields = parameterClass.getDeclaredFields();
        for (Field declaredField : declaredFields) {
            try {
                declaredField.setAccessible(true);
                final Object o = declaredField.get(parameter);
                declaredField.setAccessible(false);
                result.addAll(recursion(o));
            } catch (IllegalAccessException e) {
                log.error("{}", e);
                throw new RuleEngineException(e.getMessage());
            }
        }

        return result;
    }

    @Override
    public int getWeight() {
        return 2;
    }


    @Override
    public Object getValue(Context context) {
        Object parameter = function.getParameter();
        if (parameter instanceof Value) {
            parameter = ((Value) parameter).getValue(context);
        } else if (parameter instanceof AutoExecute) {
            try {
                final Field[] declaredFields = parameter.getClass().getDeclaredFields();
                for (Field declaredField : declaredFields) {
                    declaredField.setAccessible(true);
                    final Object fieldValue = declaredField.get(parameter);
                    if ((fieldValue instanceof AutoExecute) && declaredField.getType() != Object.class) {
                        final String clazzName = parameter.getClass().getSimpleName();
                        final String fieldName = declaredField.getName();
                        log.warn("因为{}的成员变量{}，类型不是java.lang.Object,所以该变量不会自动执行,可以通过{}.get{}.getValue(context)获取该变量的值", clazzName, fieldName, clazzName, StrUtil.upperFirst(fieldName));
                        continue;
                    }
                    if (fieldValue instanceof Value) {
                        //递归获取值
                        declaredField.set(parameter, ((Value) fieldValue).getValue(context));
                    }
                    declaredField.setAccessible(false);
                }
            } catch (IllegalAccessException e) {
                log.error("{}", e);
                throw new RuleExecuteException(e.getMessage());
            }
        }
        final Function function = functionHolder.getFunction(this.function.getFunctionName());
        return function.execute(context, parameter);
    }

    /**
     * 获取variable返回值的类型
     *
     * @return
     */
    public Class getValueClass() {
        return functionHolder.getFunction(this.function.getFunctionName()).getFunctionResultType();
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Variable)) {
            return false;
        }
        Variable variable = (Variable) other;

        return Objects.equals(this.function, variable.function);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.function);
    }

    @Override
    public String toString() {
        return function.getFunctionName() + "," + getId();
    }
}
