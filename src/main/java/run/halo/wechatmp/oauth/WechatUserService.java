package run.halo.wechatmp.oauth;

/**
 * @Author zhp
 */
public interface WechatUserService {

    void checkSignature(String signature, String timestamp, String nonce);

    String handleWechatMsg(String requestBody);
}
