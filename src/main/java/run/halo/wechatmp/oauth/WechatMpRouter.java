package run.halo.wechatmp.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2LoginAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.wechatmp.util.WeChatQrCodeCacheUtil;
import run.halo.wechatmp.util.WechatMpUtil;
import run.halo.wechatmp.util.WechatQrCode;


@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMpRouter {

    private final String tag = "plugin-wechatmp";
    @Autowired
    private WechatMpUtil wechatMpUtil;
    @Autowired
    private WeChatUserServiceImpl wechatUserService;


    @Bean
    RouterFunction<ServerResponse> wechatMpRoute() {
        return SpringdocRouteBuilder.route()
            .nest(RequestPredicates.path("/apis/wechatmp.halo.run/v1alpha1/plugins"),
                this::nested,
                builder -> builder.operationId("PluginOauthWechatMpEndpoints")
                    .description("Plugin OAuth wechat-mp Endpoints").tag(tag)
            )
            .build();
    }

    RouterFunction<ServerResponse> nested() {
        return SpringdocRouteBuilder.route()
            .GET("/loginQrCode", this::loginQrCode,
                builder -> builder.operationId("LoginQrCode").description("Get the QR code of the WeChat public account").tag(tag)
            )
            .GET("/userLogin",this::userLogin,
                builder -> builder.operationId("UserLogin").description("Check whether the code scanning is complete").tag(tag)
            )
            .GET("/-/wechatCheck",this::wechatCheck,
                builder -> builder.operationId("WechatCheck").description("Verify the WeChat signature").tag(tag)
            )
            .POST("/-/wechatCheck",this::wechatMsg,
                builder -> builder.operationId("WechatMsg").description("Receive WeChat messages").tag(tag)
            )
            .build();
    }

    Mono<ServerResponse> wechatCheck(ServerRequest request) {
        String signature = request.queryParams().getFirst("signature");
        String timestamp = request.queryParams().getFirst("timestamp");
        String nonce = request.queryParams().getFirst("nonce");
        String echostr = request.queryParams().getFirst("echostr");

        if (StringUtils.isEmpty(signature) || StringUtils.isEmpty(timestamp) || StringUtils.isEmpty(nonce)) {
            return ServerResponse.ok().build();
        }
        wechatUserService.checkSignature(signature, timestamp, nonce);
        return ServerResponse.ok().bodyValue(echostr);
    }

    Mono<ServerResponse> wechatMsg(ServerRequest request) {
        String signature = request.queryParams().getFirst("signature");
        String timestamp = request.queryParams().getFirst("timestamp");
        String nonce = request.queryParams().getFirst("nonce");
        log.info("requestBody:{}", request);
        log.info("signature:{}", signature);
        log.info("timestamp:{}", timestamp);
        log.info("nonce:{}", nonce);
        wechatUserService.checkSignature(signature, timestamp, nonce);
        return request.bodyToMono(String.class).flatMap(body -> {
                // 将请求体传递给 wechatUserService.handleWechatMsg()
            log.info("body:{}", body);
            return ServerResponse.ok().bodyValue(wechatUserService.handleWechatMsg(body));
        });
    }

    /**
     * 校验是否扫描完成
     * 完成，返回 JWT
     * 未完成，返回 check faild
     * @return
     */
    Mono<ServerResponse> userLogin(ServerRequest request) {
        String ticketParams = request.queryParams().getFirst("ticket");

        String openId = WeChatQrCodeCacheUtil.get(ticketParams);
        if (StringUtils.isNotEmpty(openId)) {
            log.info("微信扫码登录成功！,open id:{}", openId);
            // 获取微信用户基本信息
            try {
                WxMpUser userWxInfo = WechatMpUtil.wxMpService.getUserService().userInfo(openId);
                log.info("userWxInfo:{}", userWxInfo);
                log.debug("userWxInfo:{}", userWxInfo);
                if (userWxInfo != null) {
                    // TODO 可以添加关注用户到本地数据库
                    System.out.println("userWxInfo: "+userWxInfo.toString());
                }
            } catch (WxErrorException e) {
                if (e.getError().getErrorCode() == 48001) {
                    log.info("该公众号没有获取用户信息权限！");
                }
            }
            // ApiResultUtil.success(JwtUtil.createToken(openId));
            return ServerResponse.ok().bodyValue("校验成功!");
        }
        log.info("登录失败,ticket:{}", ticketParams);
        return ServerResponse.ok().bodyValue("校验失败!请重新扫码");
    }

    /**
     * 获取验证码
     * @param request
     * @return
     */
    Mono<ServerResponse> loginQrCode(ServerRequest request) {
        WechatQrCode wxCode = wechatMpUtil.getWxQrCode();
        return ServerResponse.ok().bodyValue(wxCode.getQrCodeUrl());
    }

}
