package cn.itcast.sms;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {
    @Autowired
    private SmsUtil smsUtil;

    @JmsListener(destination = "sms")
    public void sendSms(Map<String, String> map) {
        try {
            SendSmsResponse sendSmsResponse = smsUtil.sendSms(map.get("mobile"),
                    map.get("template_code"), map.get("sign_name"), map.get("param"));
            System.out.println("code"+sendSmsResponse.getCode());
            System.out.println("message"+sendSmsResponse.getMessage());
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

}
