package com.wyj.alipay.controller;

import com.alipay.api.AlipayApiException;
import com.wyj.alipay.model.PayCallbackDto;
import com.wyj.alipay.model.PayDto;
import com.wyj.alipay.model.ViewData;
import com.wyj.alipay.service.IAlipayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author: yijun.wen
 * @date: 2022/3/14 10:57 上午
 * @description:
 */
@RestController
@RequestMapping("/api/pay/alipay/")
public class AlipayController {
    @Autowired
    private IAlipayService alipayService;

    /**
     * 生成支付宝支付二维码
     *
     * @param payInfo
     * @return
     */
    @PostMapping("/qr_code")
    public ViewData alipay(@RequestBody PayDto payInfo) {
        return alipayService.alipay(payInfo);
    }

    /**
     * alipay 异步通知
     * 参考地址：https://opendocs.alipay.com/support/01ravg
     */
    @ResponseBody
    @PostMapping("/notifyUrl")
    public String notify_url(HttpServletRequest request) {
        boolean result = alipayService.alipayCallback(request);
        if (result) {
            // alipay 规范，请不要修改或删除
            return "success";
        } else {
            // 验证失败
            return "fail";
        }
    }

    /**
     * alipay 监听支付状态的接口
     *
     * @param PayCallbackInfo
     * @return
     */
    @PostMapping("/alipaycallback")
    public ViewData alipayCallback(@RequestBody PayCallbackDto PayCallbackInfo) throws AlipayApiException {
        return alipayService.alipayCallback(PayCallbackInfo);
    }

}
