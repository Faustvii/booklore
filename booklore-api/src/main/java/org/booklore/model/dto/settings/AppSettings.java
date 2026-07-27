package org.booklore.model.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class AppSettings {
    private boolean autoBookSearch;
    private boolean similarBookRecommendation;
    private boolean autoFetchAuthorMetadata;
    private boolean opdsServerEnabled;
    private boolean komgaApiEnabled;
    private boolean komgaGroupUnknown;
    private String uploadPattern;
    private Integer pdfCacheSizeInMb;
    private boolean remoteAuthEnabled;
    private boolean oidcEnabled;
    private OidcProviderDetails oidcProviderDetails;
    private OidcAutoProvisionDetails oidcAutoProvisionDetails;
    private MetadataMatchWeights metadataMatchWeights;
    private MetadataPersistenceSettings metadataPersistenceSettings;
    private KoboSettings koboSettings;
    private CoverCroppingSettings coverCroppingSettings;
    private Integer oidcSessionDurationHours;
    private String oidcGroupSyncMode;
    private boolean oidcForceOnlyMode;
    private String diskType;
}