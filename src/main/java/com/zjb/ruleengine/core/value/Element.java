package com.zjb.ruleengine.core.value;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.collect.Sets;
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
public class Element extends Value {

    private static final Logger log = LogManager.getLogger();
    /**
     * 元素code
     */
    private String code;

    /**
     * 数据类型
     */
    private DataTypeEnum dataType;

    public Element(DataTypeEnum dataType, String code) {
        super(dataType);
        Validate.notBlank(code);
        Validate.notNull(dataType);
        this.dataType = dataType;
        this.code = code;
    }


    @Override
    public Collection<Element> collectParameter() {
        return Collections.unmodifiableSet(Sets.newHashSet(this));
    }

    @Override
    public int getWeight() {
        return MID;
    }

    /**
     * 元素类型
     *
     * @param context 上下文
     * @return 结果从上下文中获取
     */
    @Override
    @SuppressWarnings("unchecked")
    public Object getValue(Context context) {
        Object value = context.get(code);
        Validate.isAssignableFrom(dataType.getClazz(), value.getClass(), value.getClass() + " not cast to " + dataType.getClazz());
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Element)) {
            return false;
        }
        Element element = (Element) other;
        if (this.dataType != element.dataType) {
            return false;
        }
        return this.getCode().equals(element.getCode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.code, this.dataType);
    }

    public String getCode() {
        return code;
    }

    @Override
    public String toString() {
        return this.dataType.getClazz().getSimpleName() + " : " + this.code;
    }


    public void setDataType(DataTypeEnum dataType) {
        this.dataType = dataType;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public static Element stringValue(String code) {
        return new Element(DataTypeEnum.STRING, code);
    }

    public static Element booleanValue(String code) {
        return new Element(DataTypeEnum.BOOLEAN, code);
    }

    public static Element numberValue(String code) {
        return new Element(DataTypeEnum.NUMBER, code);
    }

    public static Element collectionValue(String code) {
        return new Element(DataTypeEnum.COLLECTION, code);
    }

    public static Element pojoValue(String code) {
        return new Element(DataTypeEnum.POJO, code);
    }

    public static Element jsonObjectValue(String code) {
        return new Element(DataTypeEnum.JSONOBJECT, code);
    }

    public static Element objectValue(String code) {
        return new Element(DataTypeEnum.OBJECT, code);
    }

    public static Element mapValue(String code) {
        return new Element(DataTypeEnum.MAP, code);
    }
}
