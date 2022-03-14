package com.wyj.alipay.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * @author: yijun.wen
 * @date: 2022/3/11 5:32 下午
 * @description: alipay 配置类
 */
@Data
@Component
public class AlipayConfig {

    /**
     * 应用ID,您的APPID，收款账号既是您的APPID对应支付宝账号
     */

    @Value("${alipay.app_id}")
    public String app_id;

    /**
     * 商户私钥，您的PKCS8格式RSA2私钥
     */

    @Value("${alipay.merchant_private_key}")
    public String merchant_private_key;

    /**
     * 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
     */

    @Value("${alipay.alipay_public_key}")
    public String alipay_public_key;

    /**
     * 服务器异步通知页面路径  需http://格式的完整路径，不能加参数，必须外网可以正常访问
     */
    @Value("${alipay.notify_url}")
    public String notify_url;

    /**
     * 页面跳转同步通知页面路径 需http://格式的完整路径，不能加参数，必须外网可以正常访问（我们这里没用这个）
     */

    @Value("${alipay.return_url}")
    public String return_url;

    /**
     * 签名方式
     */

    @Value("${alipay.sign_type}")
    public String sign_type;

    /**
     * 字符编码格式
     */

    @Value("${alipay.charset}")
    public String charset;

    /**
     * 支付宝网关
     */
    @Value("${alipay.gatewayUrl}")
    public String gatewayUrl;

}
