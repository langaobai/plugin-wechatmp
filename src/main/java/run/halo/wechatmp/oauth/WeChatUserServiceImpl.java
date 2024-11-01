package run.halo.wechatmp.oauth;

import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;
import run.halo.wechatmp.util.WeChatMsgUtil;
import run.halo.wechatmp.util.WeChatQrCodeCacheUtil;
import run.halo.wechatmp.util.WeChatReceiveMessage;
import run.halo.wechatmp.util.WechatMpUtil;

/**
 * @Author zhp
 */
@Slf4j
@Service
public class WeChatUserServiceImpl implements WechatUserService {


    @Override
    public void checkSignature(String signature, String timestamp, String nonce) {
        String[] arr = new String[] {WechatMpUtil.token, timestamp, nonce};
        Arrays.sort(arr);
        StringBuilder content = new StringBuilder();
        for (String str : arr) {
            content.append(str);
        }
        String tmpStr = DigestUtils.sha1Hex(content.toString());
        if (tmpStr.equals(signature)) {
            log.info("check success");
            return;
        }
        log.error("check fail");
        throw new RuntimeException("check fail");
    }

    @Override
    public String handleWechatMsg(String requestBody) {
        WxMpService wxMpService = WechatMpUtil.wxMpService;
        WeChatReceiveMessage receiveMessage = WeChatMsgUtil.msgToReceiveMessage(requestBody);
        // 扫码登录
        if (WeChatMsgUtil.isScanQrCode(receiveMessage)) {
            return handleScanLogin(receiveMessage);
        }
        // 关注
        if (WeChatMsgUtil.isEventAndSubscribe(receiveMessage)) {
            return receiveMessage.getReplyTextMsg("欢迎关注【郑老师统计课程】公众号");
        }
        return receiveMessage.getReplyTextMsg("收到（自动回复）");
    }

    /**
     * 处理扫码登录
     *
     * @param receiveMessage
     * @return
     */
    private String handleScanLogin(WeChatReceiveMessage receiveMessage) {
        String qrCodeTicket = WeChatMsgUtil.getQrCodeTicket(receiveMessage);
        if (WeChatQrCodeCacheUtil.get(qrCodeTicket) == null) {
            String openId = receiveMessage.getFromUserName();
            WeChatQrCodeCacheUtil.put(qrCodeTicket, openId);
        }
        return receiveMessage.getReplyTextMsg("你已成功登录！");
    }
}
