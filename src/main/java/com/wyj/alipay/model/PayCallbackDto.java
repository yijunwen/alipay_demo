package com.wyj.alipay.model;

import lombok.Data;

import java.io.Serializable;

/**
 * @author: yijun.wen
 * @date: 2022/3/11 5:52 下午
 * @description:
 */
@Data
public class PayCallbackDto implements Serializable {

    private Long userId;

    private String payNumber;
}
