package run.halo.wechatmp.oauth;

import static io.swagger.v3.oas.annotations.media.Schema.RequiredMode.REQUIRED;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import run.halo.app.extension.AbstractExtension;
import run.halo.app.extension.GVK;

/**
 * Oauth2 client registration extension.
 *
 * @author guqing
 * @since 2.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@GVK(group = "wechatmp.halo.run", version = "v1alpha1", kind = "Oauth2ClientRegistration",
    singular = "oauth2clientregistration", plural = "oauth2clientregistrations")
public class Oauth2ClientRegistration extends AbstractExtension {

    @Schema(requiredMode = REQUIRED)
    private Oauth2ClientRegistrationSpec spec;

    @Data
    @ToString
    public static class Oauth2ClientRegistrationSpec {

        private String clientAuthenticationMethod;

        private String authorizationGrantType;

        private String redirectUri;

        private Set<String> scopes;

        private String authorizationUri;

        @Schema(requiredMode = REQUIRED, minLength = 1)
        private String tokenUri;

        private String userInfoUri;

        private String userInfoAuthenticationMethod;

        private String userNameAttributeName;

        private String jwkSetUri;

        private String issuerUri;

        private Map<String, Object> configurationMetadata;

        private String clientName;
    }
}
