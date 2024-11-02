package run.halo.wechatmp;

import static run.halo.app.extension.index.IndexAttributeFactory.multiValueAttribute;
import static run.halo.app.extension.index.IndexAttributeFactory.simpleAttribute;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import run.halo.app.extension.Scheme;
import run.halo.app.extension.SchemeManager;
import run.halo.app.extension.index.IndexSpec;
import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;
import run.halo.wechatmp.oauth.AuthorizedClient;
import run.halo.wechatmp.oauth.Oauth2ClientRegistration;

/**
 * <p>Plugin main class to manage the lifecycle of the plugin.</p>
 * <p>This class must be public and have a public constructor.</p>
 * <p>Only one main class extending {@link BasePlugin} is allowed per plugin.</p>
 *
 * @author guqing
 * @since 1.0.0
 */
@Component
public class WechatmpPlugin extends BasePlugin {

    private final SchemeManager schemeManager;

    public WechatmpPlugin(PluginContext pluginContext, SchemeManager schemeManager) {
        super(pluginContext);
        this.schemeManager = schemeManager;
    }

    @Override
    public void start() {
        schemeManager.register(AuthorizedClient.class);
        schemeManager.register(Oauth2ClientRegistration.class);
        System.out.println("插件启动成功！");
    }

    @Override
    public void stop() {
        schemeManager.unregister(Scheme.buildFromType(AuthorizedClient.class));
        schemeManager.unregister(Scheme.buildFromType(Oauth2ClientRegistration.class));
        // System.out.println("插件停止！");
    }
}