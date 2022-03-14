package com.wyj.alipay.service;

import com.alipay.api.AlipayApiException;
import com.wyj.alipay.model.PayCallbackDto;
import com.wyj.alipay.model.PayDto;
import com.wyj.alipay.model.ViewData;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: yijun.wen
 * @date: 2022/3/11 5:44 下午
 * @description:
 */
public interface IAlipayService {
    /**
     * 生成支付二维码
     *
     * @param payInfo
     * @return
     */
    ViewData alipay(PayDto payInfo);

    /**
     * 支付宝回调接口
     *
     * @param request
     * @return
     */
    boolean alipayCallback(HttpServletRequest request);

    /**
     * alipay 监听支付状态的接口
     *
     * @param payCallbackInfo
     * @return
     * @throws AlipayApiException
     */
    ViewData alipayCallback(PayCallbackDto payCallbackInfo) throws AlipayApiException;

}
