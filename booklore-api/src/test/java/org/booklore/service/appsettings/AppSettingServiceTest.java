package org.booklore.service.appsettings;

import org.booklore.config.AppProperties;
import org.booklore.config.security.service.AuthenticationService;
import org.booklore.model.dto.BookLoreUser;
import org.booklore.model.dto.settings.AppSettingKey;
import org.booklore.model.dto.settings.AppSettings;
import org.booklore.model.dto.settings.OidcProviderDetails;
import org.booklore.model.entity.AppSettingEntity;
import org.booklore.model.enums.PermissionType;
import org.booklore.repository.AppSettingsRepository;
import org.booklore.service.audit.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppSettingServiceTest {

    @Mock private AppSettingsRepository appSettingsRepository;
    @Mock private AuthenticationService authenticationService;
    @Mock private AuditService auditService;

    private AppSettingService service;

    @BeforeEach
    void setUp() {
        AppProperties appProperties = new AppProperties();
        appProperties.setRemoteAuth(new AppProperties.RemoteAuth());
        SettingPersistenceHelper persistenceHelper = new SettingPersistenceHelper(appSettingsRepository, new ObjectMapper());
        service = new AppSettingService(appProperties, persistenceHelper, authenticationService, auditService);

        BookLoreUser.UserPermissions permissions = new BookLoreUser.UserPermissions();
        permissions.setAdmin(true);
        BookLoreUser adminUser = BookLoreUser.builder().permissions(permissions).build();
        lenient().when(authenticationService.getAuthenticatedUser()).thenReturn(adminUser);
    }

    private AppSettingEntity storedOidcProviderDetails(String json) {
        AppSettingEntity entity = new AppSettingEntity();
        entity.setName(AppSettingKey.OIDC_PROVIDER_DETAILS.toString());
        entity.setVal(json);
        return entity;
    }

    private AppSettingEntity storedSetting(AppSettingKey key, String json) {
        AppSettingEntity entity = new AppSettingEntity();
        entity.setName(key.toString());
        entity.setVal(json);
        return entity;
    }

    @Test
    void getAppSettings_populatesOidcAutoProvisionDetails() {
        String stored = "{\"enableAutoProvisioning\":true,\"allowLocalAccountLinking\":true,"
                + "\"defaultPermissions\":[\"permissionRead\"],\"defaultLibraryIds\":[1]}";
        when(appSettingsRepository.findAll()).thenReturn(
                java.util.List.of(storedSetting(AppSettingKey.OIDC_AUTO_PROVISION_DETAILS, stored)));

        AppSettings settings = service.getAppSettings();

        assertThat(settings.getOidcAutoProvisionDetails()).isNotNull();
        assertThat(settings.getOidcAutoProvisionDetails().isEnableAutoProvisioning()).isTrue();
        assertThat(settings.getOidcAutoProvisionDetails().isAllowLocalAccountLinking()).isTrue();
        assertThat(settings.getOidcAutoProvisionDetails().getDefaultPermissions()).containsExactly("permissionRead");
        assertThat(settings.getOidcAutoProvisionDetails().getDefaultLibraryIds()).containsExactly(1L);
    }

    @Test
    void getAppSettings_keepsRealClientSecret_forInternalUseByOidcAuthService() {
        // AppSettingService.getAppSettings() is the internal singleton OidcAuthService/
        // OidcTokenClient use to actually authenticate with the IdP - it must never have the
        // secret stripped, or every OIDC login silently sends no client_secret. Only the
        // HTTP-facing AppSettingController.getAppSettings() sanitizes a copy for the API response.
        String stored = "{\"providerName\":\"Authentik\",\"clientId\":\"abc\",\"clientSecret\":\"super-secret\",\"issuerUri\":\"https://idp.example.com\"}";
        when(appSettingsRepository.findAll()).thenReturn(java.util.List.of(storedOidcProviderDetails(stored)));

        AppSettings settings = service.getAppSettings();

        assertThat(settings.getOidcProviderDetails()).isNotNull();
        assertThat(settings.getOidcProviderDetails().getProviderName()).isEqualTo("Authentik");
        assertThat(settings.getOidcProviderDetails().getClientId()).isEqualTo("abc");
        assertThat(settings.getOidcProviderDetails().getIssuerUri()).isEqualTo("https://idp.example.com");
        assertThat(settings.getOidcProviderDetails().getClientSecret()).isEqualTo("super-secret");
    }

    @Test
    void updateSetting_preservesExistingClientSecret_whenIncomingSecretBlank() throws Exception {
        String stored = "{\"providerName\":\"Authentik\",\"clientId\":\"abc\",\"clientSecret\":\"super-secret\",\"issuerUri\":\"https://idp.example.com\"}";
        when(appSettingsRepository.findAll()).thenReturn(java.util.List.of(storedOidcProviderDetails(stored)));
        when(appSettingsRepository.findByName(AppSettingKey.OIDC_PROVIDER_DETAILS.toString()))
                .thenReturn(storedOidcProviderDetails(stored));

        Map<String, Object> incoming = new LinkedHashMap<>();
        incoming.put("providerName", "Authentik");
        incoming.put("clientId", "abc");
        incoming.put("clientSecret", ""); // admin didn't retype the secret
        incoming.put("issuerUri", "https://idp.example.com");
        incoming.put("scopes", "openid profile email groups");

        service.updateSetting(AppSettingKey.OIDC_PROVIDER_DETAILS, incoming);

        String saved = capturedOidcProviderDetailsValue();
        assertThat(saved).contains("\"clientSecret\":\"super-secret\"");
        assertThat(saved).contains("\"scopes\":\"openid profile email groups\"");
    }

    @Test
    void updateSetting_usesNewClientSecret_whenAdminProvidesOne() throws Exception {
        String stored = "{\"providerName\":\"Authentik\",\"clientId\":\"abc\",\"clientSecret\":\"old-secret\",\"issuerUri\":\"https://idp.example.com\"}";
        when(appSettingsRepository.findByName(AppSettingKey.OIDC_PROVIDER_DETAILS.toString()))
                .thenReturn(storedOidcProviderDetails(stored));

        Map<String, Object> incoming = new LinkedHashMap<>();
        incoming.put("providerName", "Authentik");
        incoming.put("clientId", "abc");
        incoming.put("clientSecret", "brand-new-secret");
        incoming.put("issuerUri", "https://idp.example.com");

        service.updateSetting(AppSettingKey.OIDC_PROVIDER_DETAILS, incoming);

        assertThat(capturedOidcProviderDetailsValue()).contains("\"clientSecret\":\"brand-new-secret\"");
    }

    @Test
    void updateSetting_leavesSecretBlank_whenNothingWasStoredBefore() throws Exception {
        when(appSettingsRepository.findByName(AppSettingKey.OIDC_PROVIDER_DETAILS.toString())).thenReturn(null);

        Map<String, Object> incoming = new LinkedHashMap<>();
        incoming.put("providerName", "Authentik");
        incoming.put("clientId", "abc");
        incoming.put("clientSecret", "");
        incoming.put("issuerUri", "https://idp.example.com");

        service.updateSetting(AppSettingKey.OIDC_PROVIDER_DETAILS, incoming);

        assertThat(capturedOidcProviderDetailsValue()).contains("\"clientSecret\":\"\"");
    }

    private String capturedOidcProviderDetailsValue() {
        ArgumentCaptor<AppSettingEntity> captor = ArgumentCaptor.forClass(AppSettingEntity.class);
        verify(appSettingsRepository, atLeastOnce()).save(captor.capture());
        return captor.getAllValues().stream()
                .filter(e -> e.getName().equals(AppSettingKey.OIDC_PROVIDER_DETAILS.toString()))
                .reduce((first, second) -> second) // last write wins
                .map(AppSettingEntity::getVal)
                .orElseThrow();
    }
}
