package org.booklore.util;

import org.booklore.model.dto.BookLoreUser;
import org.booklore.model.entity.UserPermissionsEntity;
import org.booklore.model.enums.PermissionType;
import lombok.experimental.UtilityClass;

@UtilityClass
public class UserPermissionUtils {

    public static boolean hasPermission(UserPermissionsEntity perms, PermissionType type) {
        return switch (type) {
            case ADMIN -> perms.isPermissionAdmin();
            case DOWNLOAD -> perms.isPermissionDownload();
            case EDIT_METADATA -> perms.isPermissionEditMetadata();
            case MANAGE_LIBRARY -> perms.isPermissionManageLibrary();
            case EMAIL_BOOK -> perms.isPermissionEmailBook();
            case ACCESS_OPDS -> perms.isPermissionAccessOpds();
            case SYNC_KOREADER -> perms.isPermissionSyncKoreader();
            case SYNC_KOBO -> perms.isPermissionSyncKobo();
            case MANAGE_METADATA_CONFIG -> perms.isPermissionManageMetadataConfig();
            case ACCESS_LIBRARY_STATS -> perms.isPermissionAccessLibraryStats();
            case ACCESS_USER_STATS -> perms.isPermissionAccessUserStats();
            case ACCESS_TASK_MANAGER -> perms.isPermissionAccessTaskManager();
            case MANAGE_ICONS -> perms.isPermissionManageIcons();
            case MANAGE_FONTS -> perms.isPermissionManageFonts();
            case MANAGE_GLOBAL_PREFERENCES -> perms.isPermissionManageGlobalPreferences();
            case DEMO_USER -> perms.isPermissionDemoUser();
        };
    }

    public static boolean hasPermission(BookLoreUser.UserPermissions perms, PermissionType type) {
        return switch (type) {
            case ADMIN -> perms.isAdmin();
            case DOWNLOAD -> perms.isCanDownload();
            case EDIT_METADATA -> perms.isCanEditMetadata();
            case MANAGE_LIBRARY -> perms.isCanManageLibrary();
            case EMAIL_BOOK -> perms.isCanEmailBook();
            case ACCESS_OPDS -> perms.isCanAccessOpds();
            case SYNC_KOREADER -> perms.isCanSyncKoReader();
            case SYNC_KOBO -> perms.isCanSyncKobo();
            case MANAGE_METADATA_CONFIG -> perms.isCanManageMetadataConfig();
            case ACCESS_LIBRARY_STATS -> perms.isCanAccessLibraryStats();
            case ACCESS_USER_STATS -> perms.isCanAccessUserStats();
            case ACCESS_TASK_MANAGER -> perms.isCanAccessTaskManager();
            case MANAGE_ICONS -> perms.isCanManageIcons();
            case MANAGE_FONTS -> perms.isCanManageFonts();
            case MANAGE_GLOBAL_PREFERENCES -> perms.isCanManageGlobalPreferences();
            case DEMO_USER -> perms.isDemoUser();
        };
    }
}
