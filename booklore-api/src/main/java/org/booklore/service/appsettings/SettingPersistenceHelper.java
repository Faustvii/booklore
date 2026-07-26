package org.booklore.service.appsettings;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booklore.model.dto.settings.*;
import org.booklore.model.entity.AppSettingEntity;
import org.booklore.repository.AppSettingsRepository;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class SettingPersistenceHelper {

    public final AppSettingsRepository appSettingsRepository;
    private final ObjectMapper objectMapper;

    public String getOrCreateSetting(AppSettingKey key, String defaultValue) {
        var setting = appSettingsRepository.findByName(key.toString());
        if (setting != null) return setting.getVal();

        saveDefaultSetting(key, defaultValue);
        return defaultValue;
    }

    public void saveDefaultSetting(AppSettingKey key, String value) {
        AppSettingEntity setting = new AppSettingEntity();
        setting.setName(key.toString());
        setting.setVal(value);
        appSettingsRepository.save(setting);
    }

    public <T> T getJsonSetting(Map<String, String> settingsMap, AppSettingKey key, Class<T> clazz, T defaultValue, boolean persistDefault) {
        return getJsonSettingInternal(settingsMap, key, defaultValue, persistDefault,
                json -> objectMapper.readValue(json, clazz));
    }

    public <T> T getJsonSetting(Map<String, String> settingsMap, AppSettingKey key, TypeReference<T> typeReference, T defaultValue, boolean persistDefault) {
        return getJsonSettingInternal(settingsMap, key, defaultValue, persistDefault,
                json -> objectMapper.readValue(json, typeReference));
    }

    private <T> T getJsonSettingInternal(Map<String, String> settingsMap, AppSettingKey key, T defaultValue, boolean persistDefault, JsonDeserializer<T> deserializer) {
        String json = settingsMap.get(key.toString());
        if (json != null && !json.isBlank()) {
            try {
                return deserializer.deserialize(json);
            } catch (JacksonException e) {
                log.error("Failed to parse JSON for setting key '{}'. Using default value. Error: {}", key, e.getMessage());
                return defaultValue;
            }
        }
        if (defaultValue != null && persistDefault) {
            try {
                saveDefaultSetting(key, objectMapper.writeValueAsString(defaultValue));
            } catch (JacksonException e) {
                log.error("Failed to persist default value for setting key '{}'. Error: {}", key, e.getMessage());
            }
        }
        return defaultValue;
    }

    @FunctionalInterface
    private interface JsonDeserializer<T> {
        T deserialize(String json) throws JacksonException;
    }

    public String serializeSettingValue(AppSettingKey key, Object val) throws JacksonException {
        if (val == null) {
            return null;
        }
        return key.isJson() ? objectMapper.writeValueAsString(val) : val.toString();
    }

    public MetadataMatchWeights getDefaultMetadataMatchWeights() {
        return MetadataMatchWeights.builder()
                .title(10)
                .subtitle(1)
                .description(10)
                .authors(10)
                .publisher(5)
                .publishedDate(3)
                .seriesName(2)
                .seriesNumber(2)
                .seriesTotal(1)
                .isbn13(3)
                .isbn10(5)
                .language(2)
                .pageCount(1)
                .categories(10)
                .amazonRating(3)
                .amazonReviewCount(2)
                .goodreadsRating(4)
                .goodreadsReviewCount(2)
                .hardcoverRating(2)
                .hardcoverReviewCount(1)
                .doubanRating(3)
                .doubanReviewCount(2)
                .ranobedbRating(2)
                .coverImage(5)
                .build();
    }

    public MetadataPersistenceSettings getDefaultMetadataPersistenceSettings() {
        MetadataPersistenceSettings.FormatSettings epubSettings = MetadataPersistenceSettings.FormatSettings.builder()
                .enabled(false)
                .maxFileSizeInMb(250)
                .build();

        MetadataPersistenceSettings.FormatSettings pdfSettings = MetadataPersistenceSettings.FormatSettings.builder()
                .enabled(false)
                .maxFileSizeInMb(250)
                .build();

        MetadataPersistenceSettings.FormatSettings cbxSettings = MetadataPersistenceSettings.FormatSettings.builder()
                .enabled(false)
                .maxFileSizeInMb(250)
                .build();

        MetadataPersistenceSettings.SaveToOriginalFile saveToOriginalFile = MetadataPersistenceSettings.SaveToOriginalFile.builder()
                .epub(epubSettings)
                .pdf(pdfSettings)
                .cbx(cbxSettings)
                .build();

        return MetadataPersistenceSettings.builder()
                .saveToOriginalFile(saveToOriginalFile)
                .convertCbrCb7ToCbz(false)
                .moveFilesToLibraryPattern(false)
                .build();
    }

    public KoboSettings getDefaultKoboSettings() {
        return KoboSettings.builder()
                .convertToKepub(false)
                .conversionLimitInMb(100)
                .convertCbxToEpub(false)
                .conversionLimitInMbForCbx(100)
                .conversionImageCompressionPercentage(85)
                .forceEnableHyphenation(false)
                .build();
    }

    public CoverCroppingSettings getDefaultCoverCroppingSettings() {
        return CoverCroppingSettings.builder()
                .verticalCroppingEnabled(false)
                .horizontalCroppingEnabled(false)
                .aspectRatioThreshold(2.5)
                .smartCroppingEnabled(false)
                .build();
    }
}
