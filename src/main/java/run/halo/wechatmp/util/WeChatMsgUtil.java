package run.halo.wechatmp.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.apache.commons.lang3.StringUtils;

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
        JSONObject jsonObject = JSON.parseObject(XmlUtil.xml2json(xml));
        WeChatReceiveMessage  receiveMessage = new WeChatReceiveMessage();
        receiveMessage.setToUserName(jsonObject.getString("ToUserName"));
        receiveMessage.setFromUserName(jsonObject.getString("FromUserName"));
        receiveMessage.setCreateTime(jsonObject.getString("CreateTime"));
        receiveMessage.setMsgType(jsonObject.getString("MsgType"));
        receiveMessage.setContent(jsonObject.getString("Content"));
        receiveMessage.setMsgId(jsonObject.getString("MsgId"));
        receiveMessage.setEvent(jsonObject.getString("Event"));
        receiveMessage.setTicket(jsonObject.getString("Ticket"));
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