package com.zjb.ruleengine.core.rule;

import com.google.common.collect.Lists;
import com.zjb.ruleengine.core.Context;
import com.zjb.ruleengine.core.value.Element;
import com.zjb.ruleengine.core.value.Value;

import java.util.Collection;
import java.util.Collections;

/**
 * @author 赵静波
 * Created on 2021-01-02
 * 没有条件的规则，规则永远执行action并返回结果
 */
public class ActionRule extends Rule {


    public ActionRule(String id, Value action) {
        super(id, null, action);
    }


    @Override
    public int getWeight() {
        return Integer.MAX_VALUE;
    }

    @Override
    public Collection<Element> collectParameter() {

        return Collections.EMPTY_LIST;
    }
}
