package com.wyj.alipay.util;

import lombok.Data;

/**
 * @author: yijun.wen
 * @date: 2022/3/11 5:38 下午
 * @description:
 */
@Data
public class QrResponse {

    private QrCodeResponse alipay_trade_precreate_response;

    private String sign;

    public QrCodeResponse getAlipay_trade_precreate_response() {
        return alipay_trade_precreate_response;
    }

    public void setAlipay_trade_precreate_response(QrCodeResponse alipay_trade_precreate_response) {
        this.alipay_trade_precreate_response = alipay_trade_precreate_response;
    }
}
