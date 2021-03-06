package com.zjb.ruleengine.core.value;

import cn.hutool.core.text.StrFormatter;
import com.fasterxml.jackson.databind.JsonNode;
import com.sun.javafx.binding.StringConstant;
import com.zjb.ruleengine.core.Context;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;


/**
 * @author 赵静波
 * @date 2020-09-15 11:30:29
 */
public class Constant extends Value {

    private static final Logger log = LogManager.getLogger();
    /**
     * 常量值
     */
    private Object value;

    /**
     * 数据类型
     */
    private DataTypeEnum dataType;

    public Constant(DataTypeEnum dataType, Object value) {
        super(dataType);
        Validate.notNull(dataType);
        Validate.notNull(value);
        Validate.isAssignableFrom(dataType.getClazz(), value.getClass(), value.getClass() + " not cast to " + dataType.getClazz());
        this.value = value;
        this.dataType = dataType;
    }

    @Override
    public int getWeight() {
        return HIGH;
    }

    @Override
    public Collection<Element> collectParameter() {
        return Collections.EMPTY_SET;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Constant)) {
            return false;
        }
        Constant constant = (Constant) other;
        if (this.dataType != constant.dataType) {
            return false;
        }
        Object curValue = this.value;
        Object constantValue = constant.getValue();
        return curValue.equals(constantValue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.dataType, this.value);
    }

    @Override
    public String toString() {
        return StrFormatter.format("{} : {}", dataType.getClazz().getSimpleName(), value);
    }

    /**
     * constant类型
     *
     * @param context 上下文
     * @return List 常量提前被加载，故直接返回值
     */
    @Override
    public Object getValue(Context context) {
        return value;
    }

    public Object getValue() {
        return value;
    }

    public static Constant stringValue(String value) {
        return new Constant(DataTypeEnum.STRING, value);
    }

    public static Constant booleanValue(Boolean value) {
        return new Constant(DataTypeEnum.BOOLEAN, value);
    }

    public static Constant numberValue(Boolean value) {
        return new Constant(DataTypeEnum.NUMBER, value);
    }

    public static Constant collectionValue(Collection value) {
        return new Constant(DataTypeEnum.COLLECTION, value);
    }

    public static Constant pojoValue(Object value) {
        return new Constant(DataTypeEnum.POJO, value);
    }

    public static Constant jsonObjectValue(JsonNode value) {
        return new Constant(DataTypeEnum.JSONOBJECT, value);
    }

    public static Constant objectValue(Object value) {
        return new Constant(DataTypeEnum.OBJECT, value);
    }

    public static Constant mapValue(Map value) {
        return new Constant(DataTypeEnum.MAP, value);
    }
}
