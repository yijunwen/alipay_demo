package com.wyj.alipay.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

/**
 * @author: yijun.wen
 * @date: 2022/3/11 5:52 下午
 * @description:
 */
@Data
@NoArgsConstructor
public class ViewData<V> implements Serializable {

    protected int code;
    protected V data;
    protected Object error;

}
