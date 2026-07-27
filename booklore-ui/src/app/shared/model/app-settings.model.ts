export interface MetadataMatchWeights {
  title: number;
  subtitle: number;
  description: number;
  authors: number;
  publisher: number;
  publishedDate: number;
  seriesName: number;
  seriesNumber: number;
  seriesTotal: number;
  isbn13: number;
  isbn10: number;
  language: number;
  pageCount: number;
  categories: number;
  amazonRating: number;
  amazonReviewCount: number;
  goodreadsRating: number;
  goodreadsReviewCount: number;
  hardcoverRating: number;
  hardcoverReviewCount: number;
  doubanRating: number;
  doubanReviewCount: number;
  ranobedbRating: number;
  audibleRating: number;
  audibleReviewCount: number;
  coverImage: number;
}

export interface OidcProviderDetails {
  providerName: string;
  clientId: string;
  clientSecret?: string;
  issuerUri: string;
  scopes?: string;
  claimMapping: {
    username: string;
    email: string;
    name: string;
    groups: string;
  };
}

export interface OidcAutoProvisionDetails {
  enableAutoProvisioning: boolean;
  allowLocalAccountLinking: boolean;
  defaultPermissions: string[];
  defaultLibraryIds: number[];
}

export interface MetadataPersistenceSettings {
}

export interface KoboSettings {
  convertToKepub: boolean;
  conversionLimitInMb: number;
  conversionImageCompressionPercentage: number;
  convertCbxToEpub: boolean;
  conversionLimitInMbForCbx: number;
  forceEnableHyphenation: boolean;
}

export interface CoverCroppingSettings {
  verticalCroppingEnabled: boolean;
  horizontalCroppingEnabled: boolean;
  aspectRatioThreshold: number;
  smartCroppingEnabled: boolean;
}

export interface OidcTestCheck {
  name: string;
  status: 'PASS' | 'FAIL' | 'WARN' | 'SKIP';
  message: string;
}

export interface OidcTestResult {
  success: boolean;
  checks: OidcTestCheck[];
}

export interface AppSettings {
  autoBookSearch: boolean;
  similarBookRecommendation: boolean;
  autoFetchAuthorMetadata: boolean;
  uploadPattern: string;
  opdsServerEnabled: boolean;
  komgaApiEnabled: boolean;
  komgaGroupUnknown: boolean;
  remoteAuthEnabled: boolean;
  oidcEnabled: boolean;
  oidcProviderDetails: OidcProviderDetails;
  oidcAutoProvisionDetails: OidcAutoProvisionDetails;
  metadataMatchWeights: MetadataMatchWeights;
  metadataPersistenceSettings: MetadataPersistenceSettings;
  koboSettings: KoboSettings;
  coverCroppingSettings: CoverCroppingSettings;
  oidcSessionDurationHours: number | null;
  oidcGroupSyncMode: string | null;
  oidcForceOnlyMode: boolean;
  diskType: string;
}

export enum AppSettingKey {
  UPLOAD_FILE_PATTERN = 'UPLOAD_FILE_PATTERN',
  OPDS_SERVER_ENABLED = 'OPDS_SERVER_ENABLED',
  KOMGA_API_ENABLED = 'KOMGA_API_ENABLED',
  KOMGA_GROUP_UNKNOWN = 'KOMGA_GROUP_UNKNOWN',
  OIDC_ENABLED = 'OIDC_ENABLED',
  OIDC_PROVIDER_DETAILS = 'OIDC_PROVIDER_DETAILS',
  OIDC_AUTO_PROVISION_DETAILS = 'OIDC_AUTO_PROVISION_DETAILS',
  AUTO_BOOK_SEARCH = 'AUTO_BOOK_SEARCH',
  SIMILAR_BOOK_RECOMMENDATION = 'SIMILAR_BOOK_RECOMMENDATION',
  AUTO_FETCH_AUTHOR_METADATA = 'AUTO_FETCH_AUTHOR_METADATA',
  METADATA_MATCH_WEIGHTS = 'METADATA_MATCH_WEIGHTS',
  METADATA_PERSISTENCE_SETTINGS = 'METADATA_PERSISTENCE_SETTINGS',
  KOBO_SETTINGS = 'KOBO_SETTINGS',
  COVER_CROPPING_SETTINGS = 'COVER_CROPPING_SETTINGS',
  OIDC_SESSION_DURATION_HOURS = 'OIDC_SESSION_DURATION_HOURS',
  OIDC_GROUP_SYNC_MODE = 'OIDC_GROUP_SYNC_MODE',
  OIDC_FORCE_ONLY_MODE = 'OIDC_FORCE_ONLY_MODE',
}