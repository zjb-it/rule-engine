package com.zjb.ruleengine.rule;

import com.google.common.collect.Maps;
import com.zjb.ruleengine.BaseTest;
import com.zjb.ruleengine.core.BaseContextImpl;
import com.zjb.ruleengine.core.DefaultRuleEngine;
import com.zjb.ruleengine.core.condition.DefaultCondition;
import com.zjb.ruleengine.core.config.FunctionHolder;
import com.zjb.ruleengine.core.enums.DataTypeEnum;
import com.zjb.ruleengine.core.enums.RuleResultEnum;
import com.zjb.ruleengine.core.enums.Symbol;
import com.zjb.ruleengine.core.function.Function;
import com.zjb.ruleengine.core.function.GetObjectPropertyFunction;
import com.zjb.ruleengine.core.function.HttpFunction;
import com.zjb.ruleengine.core.rule.AbstractRule;
import com.zjb.ruleengine.core.rule.Rule;
import com.zjb.ruleengine.core.value.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 赵静波
 * Created on 2021-01-02
 */
public class SimpleRule extends BaseTest {

    private static String rule_id = "simple_rule";
    private static String element_code = "age";
    private static final Logger log = LogManager.getLogger();
    /**
     * 规则测试类
     * 简单的规则（如果年龄>=18就是成年人）
     * 条件（age>=18）：左值age为元素{@link Element}，即入参, 右值为常量{@link Constant}等于18
     * 结果 true
     */
    @Test
    public void isAdult() throws Exception {

        DefaultRuleEngine ruleEngine = new DefaultRuleEngine();
        ruleEngine.addRule(getRule());
        final BaseContextImpl context = new BaseContextImpl();
        context.put(element_code, 20);
        final Object result = ruleEngine.execute(rule_id, context);
        Assert.assertTrue((Boolean) result);
    }

    /**
     * 规则测试类（带http函数的规则）
     * 规则：如果 年龄>=法定年龄 就是成年人
     * 条件（张三的年龄>=法定年龄）：
     * 左值为 张三的age：
     * 1: 通过 http函数  {@link PersonHttpFunction}获取张三的个人信息
     * 2：通过 function {@link GetObjectPropertyFunction.FunctionParameter}从张三的个人信息中获取张三的年龄
     * 右值法定年龄，age为元素{@link Element}，即入参，
     */
    @Test
    public void isAdult1() throws Exception {
        DefaultRuleEngine ruleEngine = new DefaultRuleEngine();
        //如果想调用某个函数，需要先把函数注册进规则引擎
        registerFunction(ruleEngine);
        final FunctionHolder functionHolder = ruleEngine.getFunctionHolder();

        /**
         * functionName默认为类名
         * 获取张三的信息
         * {@link PersonHttpFunction}
         */
        final Variable personVariable = new Variable(DataTypeEnum.POJO,new VariableFunction("PersonHttpFunction", Maps.newHashMap(), functionHolder));
        //获取张三的age
        final String functionName = GetObjectPropertyFunction.class.getSimpleName();
        //获取GetObjectPropertyFunction的参数
        final List<Function.Parameter> paramters = functionHolder.getFunction(functionName).getParamters();
        System.out.println(paramters);
        Map<String, Value> ageVariableArgs = new HashMap<>();
        ageVariableArgs.put("object", personVariable);
        ageVariableArgs.put("fieldName", new Constant(DataTypeEnum.STRING, "age"));

        final Variable ageVariable = new Variable(DataTypeEnum.NUMBER,new VariableFunction(GetObjectPropertyFunction.class.getSimpleName(), ageVariableArgs, functionHolder));


        final DefaultCondition adult = new DefaultCondition("", ageVariable, Symbol.number_ge, new Element(DataTypeEnum.NUMBER, element_code));

        final Rule rule = new Rule(rule_id, adult, new Constant(DataTypeEnum.BOOLEAN, true));

        ruleEngine.addRule(rule);
        final BaseContextImpl context = new BaseContextImpl();
        //法定年龄18
        context.put(element_code, 18);
        final Object result = ruleEngine.execute(rule_id, context);
        System.out.println(result);
        Assert.assertTrue((Boolean) result);
    }


    /**
     * 规则测试类（多重函数的规则）
     * 规则：如果 年龄>=法定年龄 就是成年人
     * 条件（李四的年龄>=法定年龄）：
     * 左值为 张三的age：
     * 1: 通过 http函数  {@link PersonHttpFunction}获取张三的个人信息
     * 2：通过函数 {@link ParentFunction} 获取张三的父亲李四的个人信息
     * 2：通过 function {@link GetObjectPropertyFunction.FunctionParameter}从李四个人信息中获取李四的年龄
     * 右值法定年龄，age为元素{@link Element}，即入参，
     */
    @Test
    public void isAdult2() throws Exception {
        DefaultRuleEngine ruleEngine = new DefaultRuleEngine();
        //如果想调用某个函数，需要先把函数注册进规则引擎
        final FunctionHolder functionHolder = ruleEngine.getFunctionHolder();
        /**
         * functionName默认为类名
         * 获取张三的信息
         * {@link PersonHttpFunction}
         */
        final Variable personVariable = new Variable(DataTypeEnum.POJO,new VariableFunction("PersonHttpFunction", Maps.newHashMap(), functionHolder));

        HashMap<String, Value> map = Maps.newHashMap();
        map.put("param", personVariable);
        //获取张三他父亲的信息
        final Variable parentVariable = new Variable(DataTypeEnum.POJO,new VariableFunction(ParentFunction.class.getSimpleName(), map,functionHolder));

        //获取张三父亲的age
        final String functionName = GetObjectPropertyFunction.class.getSimpleName();

        final List<Function.Parameter> paramters = functionHolder.getFunction(functionName).getParamters();
        System.out.println("函数所需要参数：" + paramters);
        //map = Maps.newHashMap();
        map.put("object", parentVariable);
        map.put("fieldName", new Constant(DataTypeEnum.STRING,"age"));
        final Variable ageVariable = new Variable(DataTypeEnum.NUMBER,new VariableFunction(GetObjectPropertyFunction.class.getSimpleName(), map,functionHolder));


        final DefaultCondition adult = new DefaultCondition("", ageVariable, Symbol.number_ge, new Element(DataTypeEnum.NUMBER, element_code));

        final Rule rule = new Rule(rule_id, adult, new Constant(DataTypeEnum.BOOLEAN, true));

        ruleEngine.addRule(rule);
        final BaseContextImpl context = new BaseContextImpl();
        //法定年龄18
        context.put(element_code, 18);
        final Object result = ruleEngine.execute(rule_id, context);
        System.out.println(result);
        Assert.assertEquals(result, RuleResultEnum.NULL);

    }


    public AbstractRule getRule() {
        final DefaultCondition adult = new DefaultCondition("", new Element(DataTypeEnum.NUMBER, element_code), Symbol.number_ge, new Constant(DataTypeEnum.NUMBER, 18));
        return new Rule(rule_id, adult, new Constant(DataTypeEnum.BOOLEAN, true));
    }

    /**
     * http接口，返回张三的个人信息
     * {
     * "age": 18,
     * "name": "张三",
     * "country": "CN"
     * }
     */
    public static class PersonHttpFunction extends HttpFunction<String, Person> {


        @Override
        protected String getUrl() {
            return "http://rap2api.taobao.org/app/mock/273721/example/1608461246900";
        }
    }


    public static class ParentFunction extends Function<Person, Person> {

        @Override
        public Person execute(Person param) {
            //业务逻辑。。。

            return new Person(11, "李四", "中国");
        }
    }


    public static class Person {
        private Integer age;
        private String name;
        private String country;

        public Person() {
        }

        public Person(Integer age, String name, String country) {
            this.age = age;
            this.name = name;
            this.country = country;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }
    }


}
