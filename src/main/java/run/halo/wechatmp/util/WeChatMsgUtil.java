package run.halo.wechatmp.util;

import cn.hutool.core.util.XmlUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;
import java.util.Map;

/**
 * @author zhp
 */
public class WeChatMsgUtil {

    // 事件-关注
    private static String EVENT_SUBSCRIBE = "subscribe";

    /**
     * 微信消息转对象
     *
     * @param xml
     * @return
     */
    public static WeChatReceiveMessage msgToReceiveMessage(String xml) {
        Map<String, Object> receiverMap = XmlUtil.xmlToMap(xml);
        WeChatReceiveMessage  receiveMessage = new WeChatReceiveMessage();
        receiveMessage.setToUserName(String.valueOf(receiverMap.get("ToUserName")));
        receiveMessage.setFromUserName(String.valueOf(receiverMap.get("FromUserName")));
        receiveMessage.setCreateTime(String.valueOf(receiverMap.get("CreateTime")));
        receiveMessage.setMsgType(String.valueOf(receiverMap.get("MsgType")));
        receiveMessage.setContent(String.valueOf(receiverMap.get("Content")));
        receiveMessage.setMsgId(String.valueOf(receiverMap.get("MsgId")));
        receiveMessage.setEvent(String.valueOf(receiverMap.get("Event")));
        receiveMessage.setTicket(String.valueOf(receiverMap.get("Ticket")));
        return receiveMessage;
    }

    /**
     * 是否是订阅事件
     *
     * @param receiveMessage
     * @return
     */
    public static boolean isEventAndSubscribe(WeChatReceiveMessage receiveMessage) {
        return StringUtils.equals(receiveMessage.getEvent(), EVENT_SUBSCRIBE);
    }

    /**
     * 是否是二维码扫描事件
     *
     * @param receiveMessage
     * @return
     */
    public static boolean isScanQrCode(WeChatReceiveMessage receiveMessage) {
        return StringUtils.isNotEmpty(receiveMessage.getTicket());
    }

    /**
     * 获取扫描的二维码 Ticket
     *
     * @param receiveMessage
     * @return
     */
    public static String getQrCodeTicket(WeChatReceiveMessage receiveMessage) {
        return receiveMessage.getTicket();
    }

}