package com.chnword.chnword.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.app.PayTask;
import com.chnword.chnword.R;
import com.chnword.chnword.adapter.CatebuyAdapter;
import com.chnword.chnword.adapter.VerifyAdapter;
import com.chnword.chnword.alipy.PayResult;
import com.chnword.chnword.alipy.SignUtils;
import com.chnword.chnword.beans.CateBuyItem;
import com.chnword.chnword.beans.CateBuyer;
import com.chnword.chnword.beans.Category;
import com.chnword.chnword.dialogs.DialogUtil;
import com.chnword.chnword.net.AbstractNet;
import com.chnword.chnword.net.DeviceUtil;
import com.chnword.chnword.net.NetConf;
import com.chnword.chnword.net.NetParamFactory;
import com.chnword.chnword.net.VerifyNet;
import com.chnword.chnword.store.LocalStore;
import com.chnword.chnword.utils.NotificationName;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import net.sourceforge.simcpux.Constants;
import net.sourceforge.simcpux.MD5;
import net.sourceforge.simcpux.Util;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;


/**
 * Created by khtc on 15/9/19.
 */
public class ShopVerifyActivity extends Activity {
    private static final String TAG = ShopVerifyActivity.class.getSimpleName();

    private View backImageButton;
    private ListView shoplistView;

    private RadioGroup payRadioGroup;
    private RadioButton payBywexin, payByZfb;

    private ImageButton submitButton;
    private TextView totalPriceTextView;

    List<CateBuyItem> buyed = new ArrayList<CateBuyItem>();
    private VerifyAdapter adapter;
    private CateBuyer buyer = new CateBuyer(0);

    private String promoCode = "";//优惠码
//    private String couponCode = "";//提交的优惠码



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopverify);

        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);
        ImageLoader.getInstance().init(configuration);

        backImageButton = findViewById(R.id.backImageButton);
        backImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();//回退
            }
        });

        payRadioGroup = (RadioGroup) findViewById(R.id.payRadioGroup);
        payBywexin = (RadioButton) findViewById(R.id.payBywexin);
        payByZfb = (RadioButton) findViewById(R.id.payByZfb);

        submitButton = (ImageButton) findViewById(R.id.submitButton);
        totalPriceTextView = (TextView) findViewById(R.id.totalPriceTextView);

        Bundle bundle = getIntent().getBundleExtra("bundle");
        Parcelable[] parcelables = bundle.getParcelableArray("BUYITEMS");

        for (int i = 0; i < parcelables.length; i ++) {
            CateBuyItem item = (CateBuyItem) parcelables[i];
            buyed.add(item);
            buyer.add(item);
//            Log.e("ShopVerifyActivity", parcelables[i] + "");
        }


        shoplistView = (ListView) findViewById(R.id.shoplistView);
        adapter = new VerifyAdapter(this, buyed);
        shoplistView.setAdapter(adapter);
        totalPriceTextView.setText(buyer.getRealPriceText());

        payBywexin.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                payByZfb.setChecked(!isChecked);

            }
        });

        payByZfb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                payBywexin.setChecked(!isChecked);

            }
        });


        req = new PayReq();

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 15/9/20 添加微信或者支付宝的调用

                if (payBywexin.isChecked()) {
                    Log.e(TAG, "payBywexin CHECKED");
                    currentPayment = SHOP_PAYMENT_WECHAT;
                    //生成预支付订单
                    GetPrepayIdTask getPrepayId = new GetPrepayIdTask();
                    getPrepayId.execute();

                }

                if (payByZfb.isChecked()) {
                    Log.e(TAG, "payByzfb CHECKED");
                    currentPayment = SHOP_PAYMENT_ALI;
                    pay(null);
                }

            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(NotificationName.NOTIFICATION_WXPAYMENT);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null) {
            progressDialog.dismiss();
        }

        unregisterReceiver(mReceiver);
    }


    PayReq req;
    final IWXAPI msgApi = WXAPIFactory.createWXAPI(this, null);
    Map<String,String> resultunifiedorder;



    /**
     生成签名
     */

    private String genPackageSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.API_KEY);


        String packageSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        Log.e("orion",packageSign);
        return packageSign;
    }
    private String genAppSign(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < params.size(); i++) {
            sb.append(params.get(i).getName());
            sb.append('=');
            sb.append(params.get(i).getValue());
            sb.append('&');
        }
        sb.append("key=");
        sb.append(Constants.API_KEY);

//        this.sb.append("sign str\n"+sb.toString()+"\n\n");
        Log.e(TAG, "sign str\n"+sb.toString()+"\n\n");
        String appSign = MD5.getMessageDigest(sb.toString().getBytes()).toUpperCase();
        Log.e("orion",appSign);
        return appSign;
    }
    private String toXml(List<NameValuePair> params) {
        StringBuilder sb = new StringBuilder();
        sb.append("<xml>");
        for (int i = 0; i < params.size(); i++) {
            sb.append("<"+params.get(i).getName()+">");


            sb.append(params.get(i).getValue());
            sb.append("</"+params.get(i).getName()+">");
        }
        sb.append("</xml>");

        Log.e("orion",sb.toString());
        return sb.toString();
    }

    private class GetPrepayIdTask extends AsyncTask<Void, Void, Map<String,String>> {

        @Override
        protected void onPreExecute() {
//            dialog = ProgressDialog.show(ShopVerifyActivity.this, getString(R.string.app_tip), getString(R.string.getting_prepayid));
            openDialod();
        }

        @Override
        protected void onPostExecute(Map<String,String> result) {

//            sb.append("prepay_id\n"+result.get("prepay_id")+"\n\n");
//            show.setText(sb.toString());

            Log.e(TAG, "prepay_id\n"+result.get("prepay_id")+"\n\n");

            //请求shop order 接口
            currentNumber = result.get("prepay_id");
            currentPayment = SHOP_PAYMENT_WECHAT;

            requestShopOrder(currentPayment);

            resultunifiedorder=result;

//            //2 生成参数
//            genPayReq();
//
//            //发送请求
//            sendPayReq();

        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected Map<String,String>  doInBackground(Void... params) {

            String url = String.format("https://api.mch.weixin.qq.com/pay/unifiedorder");
            String entity = genProductArgs();

            Log.e("orion",entity);

            byte[] buf = Util.httpPost(url, entity);

            String content = new String(buf);
            Log.e("orion", content);
            Map<String,String> xml=decodeXml(content);

            return xml;
        }
    }

    public void openDialod() {
//        progressDialog = ProgressDialog.show(ShopVerifyActivity.this, getString(R.string.app_tip), "正在支付");
        progressDialog = DialogUtil.createLoadingDialog(ShopVerifyActivity.this, "正在支付...");
        progressDialog.show();
    }


    public Map<String,String> decodeXml(String content) {

        try {
            Map<String, String> xml = new HashMap<String, String>();
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(new StringReader(content));
            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {

                String nodeName=parser.getName();
                switch (event) {
                    case XmlPullParser.START_DOCUMENT:

                        break;
                    case XmlPullParser.START_TAG:

                        if("xml".equals(nodeName)==false){
                            //实例化student对象
                            xml.put(nodeName,parser.nextText());
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        break;
                }
                event = parser.next();
            }

            return xml;
        } catch (Exception e) {
            Log.e("orion",e.toString());
        }
        return null;

    }


    private String genNonceStr() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }

    private long genTimeStamp() {
        return System.currentTimeMillis() / 1000;
    }



    private String genOutTradNo() {
        Random random = new Random();
        return MD5.getMessageDigest(String.valueOf(random.nextInt(10000)).getBytes());
    }


    //
    private String genProductArgs() {
        StringBuffer xml = new StringBuffer();

        try {
            String	nonceStr = genNonceStr();


            xml.append("</xml>");
            List<NameValuePair> packageParams = new LinkedList<NameValuePair>();
            packageParams.add(new BasicNameValuePair("appid", Constants.APP_ID));
            packageParams.add(new BasicNameValuePair("body", "weixin"));
            packageParams.add(new BasicNameValuePair("mch_id", Constants.MCH_ID));
            packageParams.add(new BasicNameValuePair("nonce_str", nonceStr));
            // TODO: 15/9/24 修改成自己的url
            packageParams.add(new BasicNameValuePair("notify_url", "http://121.40.35.3/test"));
            packageParams.add(new BasicNameValuePair("out_trade_no",genOutTradNo()));
            packageParams.add(new BasicNameValuePair("spbill_create_ip","127.0.0.1"));
            packageParams.add(new BasicNameValuePair("total_fee", "1"));
            packageParams.add(new BasicNameValuePair("trade_type", "APP"));


            String sign = genPackageSign(packageParams);
            packageParams.add(new BasicNameValuePair("sign", sign));


            String xmlstring =toXml(packageParams);

            return xmlstring;

        } catch (Exception e) {
            Log.e(TAG, "genProductArgs fail, ex = " + e.getMessage());
            return null;
        }


    }
    private void genPayReq() {

        req.appId = Constants.APP_ID;
        req.partnerId = Constants.MCH_ID;
        req.prepayId = resultunifiedorder.get("prepay_id");
        req.packageValue = "Sign=WXPay";
        req.nonceStr = genNonceStr();
        req.timeStamp = String.valueOf(genTimeStamp());


        List<NameValuePair> signParams = new LinkedList<NameValuePair>();
        signParams.add(new BasicNameValuePair("appid", req.appId));
        signParams.add(new BasicNameValuePair("noncestr", req.nonceStr));
        signParams.add(new BasicNameValuePair("package", req.packageValue));
        signParams.add(new BasicNameValuePair("partnerid", req.partnerId));
        signParams.add(new BasicNameValuePair("prepayid", req.prepayId));
        signParams.add(new BasicNameValuePair("timestamp", req.timeStamp));

        req.sign = genAppSign(signParams);

        Log.e(TAG, "sign\n"+req.sign+"\n\n");

        Log.e("orion", signParams.toString());

    }
    private void sendPayReq() {
        msgApi.registerApp(Constants.APP_ID);
        msgApi.sendReq(req);
    }





    //==============================================================================================
    //=============================== 支付宝支付接口         =========================================
    //==============================================================================================


    // 商户PID
    public static final String PARTNER = "2088021351529570";
    // 商户收款账号
    public static final String SELLER = "2088021351529570";
    // 商户私钥，pkcs8格式
    public static final String RSA_PRIVATE = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANRJZTucoCjQ5qaw" +
            "Pyjp6qjpO7Rv6wCkhEpeC0AmjqfGKZRm/aOXU5PMpgLQDrqeEl78lg7K7a9eWuFV" +
            "CdZ7xZNvb0IaD7240qa1wFoYdNkj1x79vpgBfCq3e7+GU4JLjK2F7Qz+R5VmrgnK" +
            "E36OWosUHJdw5KiYHn45siaMbWITAgMBAAECgYBvr+3C3zSkRMQVDsUsEWWUWKFA" +
            "3WEWhXfUaIYiyiZjvq5Bla38U7F1IUZ2VGBrbp7buqh4P+utSEcoJkV2wse/RJnY" +
            "faMcKUy65J1jTzTd/3eci8WjOH5LcbtwZymxdBDMdtfOoVFl3ON0EmL5f24+AsfG" +
            "EPfOd9IHZ5IVH1EFgQJBAO16cAj4y8KcuDomemsCzg6yK5HoNpybRCKOiRIUXrbq" +
            "TlWiq8F6fUgy463MMLfVUo+qvrbH2e3iXlYyBgJiDp0CQQDk1/mwL9GtNJf9vL19" +
            "gtBdzeeitCzmtWSfF3zCsbXzai22u6sPLyoR0dxqRgcjUoPTKW+HpB4FlVggIi+d" +
            "mXxvAkEAgzs93jdeolTomXnZ/Hi4VfavjRm91B0ZMd+Cb7NCA+LHFxulvm1p/hPh" +
            "LZHA+lWwIiRA79DQ5VxKtWc/WuHFIQJADT2x5M/fgfYZFUVmcWywQb04OeHS90Zn" +
            "nAzv2xQNQxhRrNEPBMHl3UIXTs7eety7Y+xx15dXZVtOzg0sVCIdYQJAWk7bO+Eo" +
            "nip9OM6GzUw4pGb+TR8VyFJMXWoXK7NwoTa7vT/X0mtEfbQqOy2n1m/auj8wcRPQ" +
            "veRatX1n3b/xQg==";
    // 支付宝公钥
    public static final String RSA_PUBLIC = "tqnspyb9h1068p34hm6z9lmk479kefc5";
    private static final int SDK_PAY_FLAG = 1;

    private static final int SDK_CHECK_FLAG = 2;


    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SDK_PAY_FLAG: {
                    PayResult payResult = new PayResult((String) msg.obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
//                        Toast.makeText(ShopVerifyActivity.this, "支付成功",
//                                Toast.LENGTH_SHORT).show();
                        requestShopOrderPayment();
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(ShopVerifyActivity.this, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();
                            finish();

                        } else {
                            progressDialog.dismiss();
                            progressDialog = null;
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(ShopVerifyActivity.this, "支付失败",
                                    Toast.LENGTH_SHORT).show();
                            finish();

                        }
                    }
                    break;
                }
                case SDK_CHECK_FLAG: {
                    Toast.makeText(ShopVerifyActivity.this, "检查结果为：" + msg.obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        };
    };

    /**
     * call alipay sdk pay. 调用SDK支付
     *
     */
    private String currentOrderInfo;
    public void pay(View v) {
        if (TextUtils.isEmpty(PARTNER) || TextUtils.isEmpty(RSA_PRIVATE)
                || TextUtils.isEmpty(SELLER)) {
            new AlertDialog.Builder(this)
                    .setTitle("警告")
                    .setMessage("需要配置PARTNER | RSA_PRIVATE| SELLER")
                    .setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        DialogInterface dialoginterface, int i) {
                                    //
                                    finish();
                                }
                            }).show();
            return;
        }
//        progressDialog = ProgressDialog.show(ShopVerifyActivity.this, getString(R.string.app_tip), "正在支付");
        progressDialog = DialogUtil.createLoadingDialog(ShopVerifyActivity.this, "正在支付...");
        progressDialog.show();
        // 订单
//        String orderInfo = getOrderInfo("测试的商品", "该测试商品的详细描述", "0.01");
        currentOrderInfo = getOrderInfo("测试的商品", "该测试商品的详细描述", "0.01");
        requestShopOrder(SHOP_PAYMENT_ALI);
//        // 对订单做RSA 签名
//        String sign = sign(orderInfo);
//        try {
//            // 仅需对sign 做URL编码
//            Log.e(TAG, sign + "");
//            sign = URLEncoder.encode(sign, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        // 完整的符合支付宝参数规范的订单信息
//        final String payInfo = orderInfo + "&sign=\"" + sign + "\"&"
//                + getSignType();
//
//        Runnable payRunnable = new Runnable() {
//
//            @Override
//            public void run() {
//                // 构造PayTask 对象
//                PayTask alipay = new PayTask(ShopVerifyActivity.this);
//                // 调用支付接口，获取支付结果
//                String result = alipay.pay(payInfo);
//
//                Message msg = new Message();
//                msg.what = SDK_PAY_FLAG;
//                msg.obj = result;
//                mHandler.sendMessage(msg);
//            }
//        };
//
//        // 必须异步调用
//        Thread payThread = new Thread(payRunnable);
//        payThread.start();
    }

    /**
     * check whether the device has authentication alipay account.
     * 查询终端设备是否存在支付宝认证账户
     *
     */
    public void check(View v) {
        Runnable checkRunnable = new Runnable() {

            @Override
            public void run() {
                // 构造PayTask 对象
                PayTask payTask = new PayTask(ShopVerifyActivity.this);
                // 调用查询接口，获取查询结果
                boolean isExist = payTask.checkAccountIfExist();

                Message msg = new Message();
                msg.what = SDK_CHECK_FLAG;
                msg.obj = isExist;
                mHandler.sendMessage(msg);
            }
        };

        Thread checkThread = new Thread(checkRunnable);
        checkThread.start();

    }

    /**
     * get the sdk version. 获取SDK版本号
     *
     */
    public void getSDKVersion() {
        PayTask payTask = new PayTask(this);
        String version = payTask.getVersion();
        Toast.makeText(this, version, Toast.LENGTH_SHORT).show();
    }

    /**
     * create the order info. 创建订单信息
     *
     */
    public String getOrderInfo(String subject, String body, String price) {

        // 签约合作者身份ID
        String orderInfo = "partner=" + "\"" + PARTNER + "\"";

        // 签约卖家支付宝账号
        orderInfo += "&seller_id=" + "\"" + SELLER + "\"";

        // 商户网站唯一订单号
        orderInfo += "&out_trade_no=" + "\"" + getOutTradeNo() + "\"";

        // 商品名称
        orderInfo += "&subject=" + "\"" + subject + "\"";

        // 商品详情
        orderInfo += "&body=" + "\"" + body + "\"";

        // 商品金额
        orderInfo += "&total_fee=" + "\"" + price + "\"";

        // 服务器异步通知页面路径
        orderInfo += "&notify_url=" + "\"" + "http://notify.msp.hk/notify.htm"
                + "\"";

        // 服务接口名称， 固定值
        orderInfo += "&service=\"mobile.securitypay.pay\"";

        // 支付类型， 固定值
        orderInfo += "&payment_type=\"1\"";

        // 参数编码， 固定值
        orderInfo += "&_input_charset=\"utf-8\"";

        // 设置未付款交易的超时时间
        // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
        // 取值范围：1m～15d。
        // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
        // 该参数数值不接受小数点，如1.5h，可转换为90m。
        orderInfo += "&it_b_pay=\"30m\"";

        // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
        // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

        // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
        orderInfo += "&return_url=\"m.alipay.com\"";

        // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
        // orderInfo += "&paymethod=\"expressGateway\"";

        return orderInfo;
    }


    /**
     * get the out_trade_no for an order. 生成商户订单号，该值在商户端应保持唯一（可自定义格式规范）
     *
     */
    public String getOutTradeNo() {
        SimpleDateFormat format = new SimpleDateFormat("MMddHHmmss",
                Locale.getDefault());
        Date date = new Date();
        String key = format.format(date);

        Random r = new Random();
        key = key + r.nextInt();
        key = key.substring(0, 15);
        currentNumber = key;
        return key;
    }

    /**
     * sign the order info. 对订单信息进行签名
     *
     * @param content
     *            待签名订单信息
     */
    public String sign(String content) {
        return SignUtils.sign(content, RSA_PRIVATE);
    }

    /**
     * get the sign type we use. 获取签名方式
     *
     */
    public String getSignType() {
        return "sign_type=\"RSA\"";
    }






    //==============================================================================================
    //=======================             net request for shop order ===============================
    //==============================================================================================
    public static final String SHOP_PAYMENT_WECHAT = "1";
    public static final String SHOP_PAYMENT_ALI = "2";

    private Dialog progressDialog;

    private String currentNumber = null;
    private String currentPayment = SHOP_PAYMENT_WECHAT;

    private void requestShopOrder(String payment) {
        LocalStore store = new LocalStore(this);
        String userid = store.getDefaultUser();
        String deviceId = DeviceUtil.getDeviceId(this);
        JSONObject param = NetParamFactory.shopOrderParam(userid, deviceId, buyer.getPriceText(), currentNumber, currentPayment, buyed, promoCode);
        AbstractNet net = new VerifyNet(shopOrderHandler, param, NetConf.URL_SHOP_ORDER);
        net.start();
    }

    private void requestShopOrderPayment() {
        LocalStore store = new LocalStore(this);
        String userid = store.getDefaultUser();
        String deviceId = DeviceUtil.getDeviceId(this);
        JSONObject param = NetParamFactory.shopOrderPaymentParam(userid, deviceId, buyer.getPriceText(), currentNumber, currentPayment, promoCode);
        AbstractNet net = new VerifyNet(shopOrderPaymentHandler, param, NetConf.URL_SHOP_PAYMENT);

        net.start();
    }

    Handler shopOrderHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            try {
                if (msg.what == AbstractNet.NETWHAT_SUCESS)
                {
                    Bundle b = msg.getData();
                    String str = b.getString("responseBody");
                    Log.e(TAG, str);
                    JSONObject obj = new JSONObject(str);

                    String result = obj.getString("result");
                    if (result != null && "1".equalsIgnoreCase(result)) {
                        if (SHOP_PAYMENT_WECHAT.equalsIgnoreCase(currentPayment)) {
                            //2 生成参数
                            genPayReq();

                            //发送请求
                            sendPayReq();
                        } else {

                            // 对订单做RSA 签名
                            String sign = sign(currentOrderInfo);
                            try {
                                // 仅需对sign 做URL编码
                                Log.e(TAG, sign + "");
                                sign = URLEncoder.encode(sign, "UTF-8");
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }

                            // 完整的符合支付宝参数规范的订单信息
                            final String payInfo = currentOrderInfo + "&sign=\"" + sign + "\"&"
                                    + getSignType();

                            Runnable payRunnable = new Runnable() {

                                @Override
                                public void run() {
                                    // 构造PayTask 对象
                                    PayTask alipay = new PayTask(ShopVerifyActivity.this);
                                    // 调用支付接口，获取支付结果
                                    String result = alipay.pay(payInfo);

                                    Message msg = new Message();
                                    msg.what = SDK_PAY_FLAG;
                                    msg.obj = result;
                                    mHandler.sendMessage(msg);
                                }
                            };

                            // 必须异步调用
                            Thread payThread = new Thread(payRunnable);
                            payThread.start();
//                            mHandler.post(payRunnable);
                        }
                    } else {
                        Toast.makeText(ShopVerifyActivity.this, "支付失败", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }

                if (msg.what == AbstractNet.NETWHAT_FAIL) {

                    Toast.makeText(ShopVerifyActivity.this, "支付失败", Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };

    Handler shopOrderPaymentHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if (progressDialog != null) {
                progressDialog.dismiss();
                progressDialog = null;
                finish();
            }
            try {
                if (msg.what == AbstractNet.NETWHAT_SUCESS)
                {
                    Bundle b = msg.getData();
                    String str = b.getString("responseBody");
                    Log.e(TAG, str);
                    JSONObject obj = new JSONObject(str);

                    String result = obj.getString("result");
                    if (result != null && "1".equalsIgnoreCase(result)) {
                        try {
                            LocalStore store = new LocalStore(ShopVerifyActivity.this);
                            if ("0".equalsIgnoreCase(store.getDefaultUser())) {
                                JSONObject data = obj.getJSONObject("data");
                                if (data != null) {
                                    String code = data.getString("code");
                                    String userId = data.getString("userid");
                                    store.setDefaultUser(userId);
                                    //// TODO: 15/11/21 show dialog
                                    AlertDialog dialog = DialogUtil.createAlertDialog(ShopVerifyActivity.this, "请牢记您的激活码:" + code);
                                    dialog.show();
                                }
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Toast.makeText(ShopVerifyActivity.this, "支付成功", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ShopVerifyActivity.this, "支付失败", Toast.LENGTH_LONG).show();
                        finish();
                    }

                }

                if (msg.what == AbstractNet.NETWHAT_FAIL) {
                    Toast.makeText(ShopVerifyActivity.this, "支付失败", Toast.LENGTH_LONG).show();
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "PAYMENT RECEIVER.");
            String action = intent.getAction();
            if (NotificationName.NOTIFICATION_WXPAYMENT.equalsIgnoreCase(action)) {

                int errCode = intent.getIntExtra(NotificationName.Extra_WX_ErrorCode, -1);
                if (errCode < 0) {
                    if (progressDialog != null) {
                        progressDialog.dismiss();
                        progressDialog = null;
                    }

                } else {
                    requestShopOrderPayment();
                }
            }
        }
    };


    public void showPromoDialog(View v) {
        AlertDialog.Builder customDia=new AlertDialog.Builder(ShopVerifyActivity.this);
        final View viewDia= LayoutInflater.from(ShopVerifyActivity.this).inflate(R.layout.custom_dialog, null);
        customDia.setTitle("请输入优惠码");
        customDia.setView(viewDia);
        customDia.setPositiveButton("确定", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                EditText diaInput=(EditText) viewDia.findViewById(R.id.txt_cusDiaInput);
//                showClickMessage(diaInput.getText().toString());
                promoCode = diaInput.getText().toString();
                Log.e(TAG, promoCode);
                requestCoupon(promoCode);
            }
        });
        customDia.create().show();
    }

    private Dialog couponDialog;
    private void requestCoupon(String str) {
        LocalStore store = new LocalStore(this);
        String userid = store.getDefaultUser();
        String deviceId = DeviceUtil.getDeviceId(this);
        JSONObject param = NetParamFactory.couponParam(userid, deviceId, str);
        Log.e(TAG, param.toString());
        AbstractNet net = new VerifyNet(couponHandler, param, NetConf.URL_COUPON);
        net.start();
        couponDialog = DialogUtil.createLoadingDialog(this, "数据加载中...");
        couponDialog.show();
    }
    private Handler couponHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            couponDialog.dismiss();
            couponDialog = null;
            try {
                if (msg.what == AbstractNet.NETWHAT_SUCESS)
                {

                    Bundle b = msg.getData();
                    String str = b.getString("responseBody");
                    android.util.Log.e(TAG, str);
                    JSONObject obj = new JSONObject(str);

                    String result = obj.getString("result");
                    String message = obj.getString("message");
                    JSONObject data = obj.getJSONObject("data");
                    if (result != null) {
                        if ("1".equalsIgnoreCase(result)) {
                            //优惠劵可用

                            float price = (float)data.getDouble("price");
                            Toast.makeText(ShopVerifyActivity.this, "优惠劵可用, 优惠金额 ：" + price, Toast.LENGTH_LONG).show();
//                            buyer.sub(price);
                            buyer.setCouponPrice(price);
                            totalPriceTextView.setText(buyer.getRealPriceText());
                        }else if ("0".equalsIgnoreCase(result)){
                            //优惠劵不可用
                            Toast.makeText(ShopVerifyActivity.this, "优惠劵不可用", Toast.LENGTH_LONG).show();
                        } else if ("2".equalsIgnoreCase(result)) {
                            //优惠劵已过期
                            Toast.makeText(ShopVerifyActivity.this, "优惠劵已过期", Toast.LENGTH_LONG).show();
                        }else if ("3".equalsIgnoreCase(result)) {
                            //优惠劵已使用
                            Toast.makeText(ShopVerifyActivity.this, "优惠劵已使用", Toast.LENGTH_LONG).show();
                        }else if ("4".equalsIgnoreCase(result)) {
                            //优惠劵正在使用
                            Toast.makeText(ShopVerifyActivity.this, "优惠劵正在使用", Toast.LENGTH_LONG).show();

                        }
                    } else {
                        Toast.makeText(ShopVerifyActivity.this, "网络请求失败，请检查网络!", Toast.LENGTH_LONG).show();
                    }

                }

                if (msg.what == AbstractNet.NETWHAT_FAIL) {
                    Toast.makeText(ShopVerifyActivity.this, "请求失败，请检查网络设置", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            super.handleMessage(msg);
        }
    };

}
