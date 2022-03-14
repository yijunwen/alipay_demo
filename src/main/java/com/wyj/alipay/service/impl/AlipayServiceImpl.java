package com.wyj.alipay.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePrecreateModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.wyj.alipay.config.AlipayConfig;
import com.wyj.alipay.model.PayCallbackDto;
import com.wyj.alipay.model.PayDto;
import com.wyj.alipay.model.QrCodeVo;
import com.wyj.alipay.model.ViewData;
import com.wyj.alipay.service.IAlipayService;
import com.wyj.alipay.util.GenerateNum;
import com.wyj.alipay.util.QrCodeResponse;
import com.wyj.alipay.util.QrCodeUtil;
import com.wyj.alipay.util.QrResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author: yijun.wen
 * @date: 2022/3/14 10:19 上午
 * @description:
 */
@Service
@Slf4j
public class AlipayServiceImpl implements IAlipayService {

    @Autowired
    private AlipayConfig alipayConfig;

    @Override
    public ViewData alipay(PayDto payInfo) {
        ViewData<QrCodeVo> viewData = new ViewData<>();
        // 1：支付的用户
        Long userId = payInfo.getUserId();
        // 2: 支付金额
        String totalAmount = payInfo.getTotalAmount();
        // 3: 支付的产品名称
        String productName = "Alipay test";
        // 4: 支付的订单编号
        String payNumber = GenerateNum.generateOrder();
        // 5: 支付方式
        int payType = payInfo.getPayType();
        // 6：支付宝携带的参数在回调中可以通过request获取 参数
        JSONObject json = JSONUtil.createObj();
        json.set("userId", userId);
        json.set("totalAmount", totalAmount);
        json.set("productName", productName);
        json.set("payNumber", payNumber);
        json.set("payType", payType);
        // 7：设置支付相关的信息
        AlipayTradePrecreateModel model = new AlipayTradePrecreateModel();
        // 自定义订单号
        model.setOutTradeNo(payNumber);
        // 支付金额
        model.setTotalAmount(totalAmount);
        // 支付的产品名称
        model.setSubject(productName);
        // 支付的请求体参数
        model.setBody(json.toString());
        // 支付的超时时间
        model.setTimeoutExpress("5m");
        // 支付的库存 id(根据 cloudPKI 业务，这里我们用用户id )
        model.setStoreId(userId + "");
        // 调用 alipay 获取二维码参数
        QrCodeResponse qrCodeResponse = qrcodePay(model);

        try {
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            // 自定义二维码logo todo: 可以在二维码中间可以加上公司 logo
            //String logoPath = ResourceUtils.getFile("classpath:favicon.png").getAbsolutePath();
            String logoPath = "";
            // 生成二维码
            BufferedImage buffImg = QrCodeUtil.encode(qrCodeResponse.getQr_code(), logoPath, false);
            ImageOutputStream imageOut = ImageIO.createImageOutputStream(output);
            ImageIO.write(buffImg, "JPEG", imageOut);
            imageOut.close();
            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
            byte[] data = FileCopyUtils.copyToByteArray(input);
            QrCodeVo qrCodeVo = new QrCodeVo();
            qrCodeVo.setQrCode(Base64.getEncoder().encodeToString(data));
            qrCodeVo.setPayNumber(payNumber);
            qrCodeVo.setUserId(userId);
            viewData.setData(qrCodeVo);
            return viewData;
        } catch (Exception ex) {
            ex.printStackTrace();
            return viewData;
        }
    }

    /**
     * 支付宝回调
     *
     * @return
     * @throws Exception
     */
    @Override
    public boolean alipayCallback(HttpServletRequest request) {
        // 获取支付宝反馈信息
        Map<String, String> params = new LinkedHashMap<>();
        Map requestParams = request.getParameterMap();
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext(); ) {
            String name = (String) iter.next();
            String[] values = (String[]) requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
            }
            try {
                params.put(name, new String(valueStr.getBytes("ISO-8859-1"), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        // 计算得出通知验证结果
        log.info("1：获取支付宝回传的参数" + params);
        try {
            // 验签
            //RSA2密钥验签
            boolean checkV1 = AlipaySignature.rsaCheckV1(params, alipayConfig.alipay_public_key, alipayConfig.charset, alipayConfig.sign_type);
            log.info("验签成功");
            if (!checkV1) {
                log.info("验签失败接口参数：{}", params);
                return false;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            return false;
        }
        // 返回公共参数
        String extparamString = request.getParameter("extra_common_param");
        log.info("2：支付宝交易返回的公共参数：{}", extparamString);
        String tradeNo = params.get("trade_no");
        //交易完成
        String body = params.get("body");
        log.info("3:【支付宝】交易的参数信息是：{}，流水号是：{}", body, tradeNo);
        try {
            JSONObject bodyJson = new JSONObject(body);
            Long userId = bodyJson.getLong("userId");
            String payType = bodyJson.getStr("payType");
            String payNumber = bodyJson.getStr("payNumber");
            log.info("4:【支付宝】交易的参数信息是：payType:{}，payNumber：{}，userId：{}", payType, payNumber, userId);
            // todo 入库充值记录 修改库存等一系列 DB 操作
        } catch (Exception ex) {
            log.error("支付宝支付出现了异常,流水号是:{}", tradeNo);
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public ViewData alipayCallback(PayCallbackDto payCallbackInfo) throws AlipayApiException {
        ViewData<Object> viewData = new ViewData<>();
        // 1: 获取阿里请求客户端
        AlipayClient alipayClient = getAlipayClient();
        // 2: 获取阿里请求对象
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        // 3: 设置业务参数
        request.setBizContent(JSONUtil.toJsonStr(JSONUtil.createObj().set("out_trade_no", payCallbackInfo.getPayNumber())));
        //通过alipayClient调用API，获得对应的response类
        AlipayTradeQueryResponse response = alipayClient.execute(request);
        String body = response.getBody();
        JSONObject json = new JSONObject(new JSONObject(body).getStr("alipay_trade_query_response"));
        if ("10000".equals(json.getStr("code")) && "TRADE_SUCCESS".equals(json.getStr("trade_status"))) {
            viewData.setData("success");
        } else {
            viewData.setData("fail");
        }
        return viewData;
    }

    /**
     * 扫码运行代码
     * 验签通过返回QrResponse
     * 失败打印日志信息
     * 参考地址：https://opendocs.alipay.com/apis/api_1/alipay.trade.app.pay
     *
     * @param model
     * @return
     */
    public QrCodeResponse qrcodePay(AlipayTradePrecreateModel model) {
        // 1: 获取阿里请求客户端
        AlipayClient alipayClient = getAlipayClient();
        // 2: 获取阿里请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        // 3：设置请求参数的集合，最大长度不限
        request.setBizModel(model);
        // 设置异步回调地址
        request.setNotifyUrl(alipayConfig.getNotify_url());
        // 设置同步回调地址
        request.setReturnUrl(alipayConfig.getReturn_url());
        AlipayTradePrecreateResponse alipayTradePrecreateResponse = null;
        try {
            alipayTradePrecreateResponse = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        QrResponse qrResponse = JSON.parseObject(alipayTradePrecreateResponse.getBody(), QrResponse.class);
        return qrResponse.getAlipay_trade_precreate_response();
    }

    /**
     * 获取AlipayClient对象
     *
     * @return
     */
    private AlipayClient getAlipayClient() {
        //获得初始化的AlipayClient
        AlipayClient alipayClient =
                new DefaultAlipayClient(alipayConfig.getGatewayUrl(), alipayConfig.getApp_id(), alipayConfig.getMerchant_private_key(),
                        "JSON", alipayConfig.getCharset(), alipayConfig.getAlipay_public_key(), alipayConfig.getSign_type());
        return alipayClient;
    }
}
