package run.halo.wechatmp.oauth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springdoc.webflux.core.fn.SpringdocRouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import run.halo.app.extension.GroupVersion;
import run.halo.wechatmp.util.WeChatQrCodeCacheUtil;
import run.halo.wechatmp.util.WechatMpUtil;
import run.halo.wechatmp.util.WechatQrCode;


@Slf4j
@Component
@RequiredArgsConstructor
public class WechatMpRouter {

    private final String tag = "plugin-wechatmp";
    private final Oauth2LoginConfiguration oauth2LoginConfiguration;
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

    // @Bean
    // RouterFunction<ServerResponse> wechatCheckMsgFunction() {
    //     return RouterFunctions.route()
    //         .POST("/apis/wechatmp.halo.run/v1alpha1/plugins/wechat/checkMsg", this::wechatMsg)
    //         .build();
    // }

    RouterFunction<ServerResponse> nested() {
        return SpringdocRouteBuilder.route()
            .GET("/loginQrCode", this::loginQrCode,
                builder -> builder.operationId("LoginQrCode").description("Get the QR code of the WeChat public account").tag(tag)
            )
            .GET("/userLogin",this::userLogin,
                builder -> builder.operationId("UserLogin").description("Check whether the code scanning is complete").tag(tag)
            )
            .GET("/wechatCheck",this::wechatCheck,
                builder -> builder.operationId("WechatCheck").description("Verify the WeChat signature").tag(tag)
            )
            .POST("/wechat/checkMsg",this::wechatMsg,
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
        return ServerResponse.ok().bodyValue(wechatUserService.handleWechatMsg(""));
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
            log.info("login success,open id:{}", openId);
            // return ApiResultUtil.success(jwtUtil.createToken(openId));
            return ServerResponse.ok().bodyValue("check success");
        }
        log.info("login error,ticket:{}", ticketParams);
        return ServerResponse.ok().bodyValue("check faild");
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
