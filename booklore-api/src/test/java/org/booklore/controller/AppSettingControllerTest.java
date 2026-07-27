package org.booklore.controller;

import org.booklore.model.dto.settings.AppSettings;
import org.booklore.model.dto.settings.OidcProviderDetails;
import org.booklore.service.appsettings.AppSettingService;
import org.booklore.service.audit.AuditService;
import org.booklore.service.oidc.OidcDiagnosticService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppSettingControllerTest {

    @Mock private AppSettingService appSettingService;
    @Mock private OidcDiagnosticService oidcDiagnosticService;
    @Mock private AuditService auditService;

    private AppSettingController controller() {
        return new AppSettingController(appSettingService, oidcDiagnosticService, auditService);
    }

    private OidcProviderDetails providerDetailsWithSecret() {
        OidcProviderDetails details = new OidcProviderDetails();
        details.setProviderName("Authentik");
        details.setClientId("abc");
        details.setClientSecret("super-secret");
        details.setIssuerUri("https://idp.example.com");
        details.setScopes("openid profile email");
        return details;
    }

    @Test
    void getAppSettings_stripsClientSecretFromResponse_withoutMutatingInternalSingleton() {
        OidcProviderDetails realDetails = providerDetailsWithSecret();
        AppSettings internal = AppSettings.builder().oidcProviderDetails(realDetails).oidcEnabled(true).build();
        when(appSettingService.getAppSettings()).thenReturn(internal);

        AppSettings response = controller().getAppSettings();

        assertThat(response.getOidcProviderDetails().getClientSecret()).isNull();
        assertThat(response.getOidcProviderDetails().getClientId()).isEqualTo("abc");
        assertThat(response.getOidcProviderDetails().getProviderName()).isEqualTo("Authentik");
        assertThat(response.getOidcProviderDetails().getIssuerUri()).isEqualTo("https://idp.example.com");
        assertThat(response.getOidcProviderDetails().getScopes()).isEqualTo("openid profile email");
        assertThat(response.isOidcEnabled()).isTrue();

        // The internal singleton (what OidcAuthService reads) must be untouched.
        assertThat(internal.getOidcProviderDetails().getClientSecret()).isEqualTo("super-secret");
        assertThat(realDetails.getClientSecret()).isEqualTo("super-secret");
    }

    @Test
    void getAppSettings_handlesNullOidcProviderDetails() {
        AppSettings internal = AppSettings.builder().oidcProviderDetails(null).build();
        when(appSettingService.getAppSettings()).thenReturn(internal);

        AppSettings response = controller().getAppSettings();

        assertThat(response.getOidcProviderDetails()).isNull();
    }
}
