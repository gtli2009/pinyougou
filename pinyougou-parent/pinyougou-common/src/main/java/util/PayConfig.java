package util;

/**
 * 微信支付参数
 */
public class PayConfig {
	//企业方公众号Id
	public static String appid = "wx8397f8696b538317";
	//财付通平台的商户账号
	public static String partner = "1473426802";
	//财付通平台的商户密钥
	public static String partnerkey = "T6m9iK73b0kn9g5v426MKfHQH7X8rKwb";  
	//回调URL
	public static String notifyurl = "http://a31ef7db.ngrok.io/WeChatPay/WeChatPayNotify";
}
