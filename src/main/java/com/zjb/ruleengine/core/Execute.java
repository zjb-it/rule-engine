package com.zjb.ruleengine.core;

/**
 * @author 赵静波
 * @date 2020-08-25 10:29:09
 */
public interface Execute {
    /**
     * @return
     * @Author 执行
     * @Description
     * @Date 09:11 2019-06-24
     * @Param [context]
     **/
    Object execute(Context context);
}
