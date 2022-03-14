package com.wyj.alipay.model;

import lombok.Data;

/**
 * @author: yijun.wen
 * @date: 2022/3/14 10:21 上午
 * @description:
 */
@Data
public class QrCodeVo {
    private Long UserId;

    private String payNumber;

    private String qrCode;
}
