package run.halo.wechatmp.util;

import java.net.URI;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.impl.WxMpServiceImpl;
import me.chanjar.weixin.mp.bean.result.WxMpQrCodeTicket;
import me.chanjar.weixin.mp.config.impl.WxMpDefaultConfigImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WechatMpUtil {


    public static String appId = "wxf95035afac833d68";
    public static String appSecret = "1fa77a4b927c58876598cf41e9d90199";
    public static String token = "xiaoming";

    private static String QR_CODE_URL_PREFIX = "https://mp.weixin.qq.com/cgi-bin/showqrcode?ticket=";

    private static String ACCESS_TOKEN = null;
    private static LocalDateTime ACCESS_TOKEN_EXPIRE_TIME = null;

    public static WxMpService wxMpService = null;

    /**
     * 二维码 Ticket 过期时间
     */
    private static int QR_CODE_TICKET_TIMEOUT = 10 * 60;

    public WechatMpUtil() {
        if (wxMpService == null) {
            WxMpDefaultConfigImpl config = new WxMpDefaultConfigImpl();
            config.setAppId(appId); // 设置微信公众号的appid
            config.setSecret(appSecret); // 设置微信公众号的app corpSecret
            config.setToken(token); // 设置微信公众号的token
            config.setAesKey("123"); // 设置微信公众号的EncodingAESKey
            wxMpService = new WxMpServiceImpl();// 实际项目中请注意要保持单例，不要在每次请求时构造实例，具体可以参考demo项目
            wxMpService.setWxMpConfigStorage(config);
        }
    }
    /**
     * 获取 access token
     *
     * @return
     */
    public synchronized String getAccessToken() {
        if (ACCESS_TOKEN != null && ACCESS_TOKEN_EXPIRE_TIME.isAfter(LocalDateTime.now())) {
            return ACCESS_TOKEN;
        }
        try {
            ACCESS_TOKEN = wxMpService.getAccessToken();
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
        return ACCESS_TOKEN;
    }


    public WechatQrCode getWxQrCode(){
        try {
            WxMpQrCodeTicket wxMpQrCodeTicket = wxMpService.getQrcodeService().qrCodeCreateTmpTicket(KeyUtils.uuid32(), QR_CODE_TICKET_TIMEOUT);
            WechatQrCode weixinQrCode = new WechatQrCode();
            BeanUtils.copyProperties(wxMpQrCodeTicket,weixinQrCode);
            weixinQrCode.setQrCodeUrl(QR_CODE_URL_PREFIX + URI.create(weixinQrCode.getTicket()).toASCIIString());
            return weixinQrCode;
        } catch (WxErrorException e) {
            throw new RuntimeException(e);
        }
    }
}
