package com.wyj.alipay.util;

import lombok.Data;

/**
 * @author: yijun.wen
 * @date: 2022/3/11 5:39 下午
 * @description:
 */
@Data
public class QrCodeResponse {
    /**
     * 返回的状态码
     */
    private String code;

    /**
     * 返回的信息
     */
    private String msg;

    /**
     * 交易的流水号
     */
    private String out_trade_no;

    /**
     * 生成二维码的内容
     */
    private String qr_code;
}
