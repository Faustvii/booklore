package org.booklore.service;

import org.booklore.model.dto.BookloreTelemetry;
import org.booklore.model.dto.Installation;
import org.booklore.model.dto.InstallationPing;
import org.booklore.model.dto.settings.AppSettings;
import org.booklore.model.dto.settings.UserSettingKey;
import org.booklore.model.entity.LibraryEntity;
import org.booklore.model.enums.BookFileType;
import org.booklore.model.enums.ProvisioningMethod;
import org.booklore.repository.*;
import org.booklore.service.appsettings.AppSettingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TelemetryService {

    private final VersionService versionService;
    private final LibraryRepository libraryRepository;
    private final BookRepository bookRepository;
    private final BookMarkRepository bookMarkRepository;
    private final BookNoteRepository bookNoteRepository;
    private final BookAdditionalFileRepository bookAdditionalFileRepository;
    private final AuthorRepository authorRepository;
    private final ShelfRepository shelfRepository;
    private final MagicShelfRepository magicShelfRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final MoodRepository moodRepository;
    private final UserRepository userRepository;
    private final EmailProviderV2Repository emailProviderV2Repository;
    private final EmailRecipientV2Repository emailRecipientV2Repository;
    private final AppSettingService appSettingService;
    private final KoboUserSettingsRepository koboUserSettingsRepository;
    private final UserSettingRepository userSettingRepository;
    private final KoreaderUserRepository koreaderUserRepository;
    private final OpdsUserV2Repository opdsUserV2Repository;
    private final InstallationService installationService;

    public InstallationPing getInstallationPing() {
        Installation installation = installationService.getOrCreateInstallation();

        return InstallationPing.builder()
                .pingVersion(1)
                .appVersion(versionService.appVersion)
                .installationId(installation.getId())
                .installationDate(installation.getDate())
                .build();
    }

    public BookloreTelemetry collectTelemetry() {
        long totalUsers = userRepository.count();
        long localUsers = userRepository.countByProvisioningMethod(ProvisioningMethod.LOCAL);
        long oidcUsers = userRepository.countByProvisioningMethod(ProvisioningMethod.OIDC);

        AppSettings settings = appSettingService.getAppSettings();
        Installation installation = installationService.getOrCreateInstallation();

        BookloreTelemetry.BookStatistics bookStatistics = BookloreTelemetry.BookStatistics.builder()
                .totalBooks(bookRepository.count())
                .bookCountByType(getBookFileTypeCounts())
                .build();

        List<BookloreTelemetry.LibraryStatistics> libraryStatisticsList = libraryRepository.findAll().stream()
                .map(this::mapLibraryStatistics)
                .collect(Collectors.toList());

        return BookloreTelemetry.builder()
                .telemetryVersion(2)
                .installationId(installation.getId())
                .installationDate(installation.getDate() != null ? installation.getDate().toString() : null)
                .appVersion(versionService.appVersion)
                .totalLibraries((int) libraryRepository.count())
                .totalBooks(bookRepository.count())
                .totalAdditionalBookFiles(bookAdditionalFileRepository.count())
                .totalAuthors(authorRepository.count())
                .totalBookmarks(bookMarkRepository.count())
                .totalBookNotes(bookNoteRepository.count())
                .totalShelves((int) shelfRepository.count())
                .totalMagicShelves((int) magicShelfRepository.count())
                .totalCategories((int) categoryRepository.count())
                .totalTags((int) tagRepository.count())
                .totalMoods((int) moodRepository.count())
                .totalKoreaderUsers((int) koreaderUserRepository.count())
                .userStatistics(BookloreTelemetry.UserStatistics.builder()
                        .totalUsers((int) totalUsers)
                        .totalLocalUsers((int) localUsers)
                        .totalOidcUsers((int) oidcUsers)
                        .oidcEnabled(oidcUsers > 0)
                        .build())
                .opdsStatistics(BookloreTelemetry.OpdsStatistics.builder()
                        .opdsEnabled(settings.isOpdsServerEnabled())
                        .totalOpdsUsers((int) opdsUserV2Repository.count())
                        .build())
                .emailStatistics(BookloreTelemetry.EmailStatistics.builder()
                        .totalEmailProviders((int) emailProviderV2Repository.count())
                        .totalEmailRecipients((int) emailRecipientV2Repository.count())
                        .build())
                .koboStatistics(BookloreTelemetry.KoboStatistics.builder()
                        .convertToKepubEnabled(settings.getKoboSettings().isConvertToKepub())
                        .totalKoboUsers((int) koboUserSettingsRepository.count())
                        .totalHardcoverSyncEnabled((int) userSettingRepository.countBySettingKeyAndSettingValue(
                                UserSettingKey.HARDCOVER_SYNC_ENABLED.getDbKey(), "true"))
                        .totalAutoAddToShelf((int) koboUserSettingsRepository.countByAutoAddToShelfTrue())
                        .build())
                .bookStatistics(bookStatistics)
                .libraryStatisticsList(libraryStatisticsList)
                .build();
    }

    private Map<String, Long> getBookFileTypeCounts() {
        Map<String, Long> countByType = new HashMap<>();
        for (BookFileType type : BookFileType.values()) {
            countByType.put(type.name(), bookRepository.countByBookType(type));
        }
        return countByType;
    }

    private BookloreTelemetry.LibraryStatistics mapLibraryStatistics(LibraryEntity lib) {
        return BookloreTelemetry.LibraryStatistics.builder()
                .totalLibraryPaths(lib.getLibraryPaths() != null ? lib.getLibraryPaths().size() : 0)
                .bookCount(bookRepository.countByLibraryId(lib.getId()))
                .watchEnabled(lib.isWatch())
                .iconType(lib.getIconType() != null ? lib.getIconType().name() : null)
                .build();
    }
}
