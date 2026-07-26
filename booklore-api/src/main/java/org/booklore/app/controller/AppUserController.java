package org.booklore.app.controller;

import org.booklore.config.security.service.AuthenticationService;
import org.booklore.app.dto.AppUserInfo;
import org.booklore.model.dto.BookLoreUser;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/app/users")
public class AppUserController {

    private final AuthenticationService authenticationService;

    @GetMapping("/me")
    public ResponseEntity<AppUserInfo> getCurrentUser() {
        BookLoreUser user = authenticationService.getAuthenticatedUser();
        BookLoreUser.UserPermissions perms = user.getPermissions();

        AppUserInfo info = AppUserInfo.builder()
                .isAdmin(perms.isAdmin())
                .canDownload(perms.isCanDownload())
                .build();

        return ResponseEntity.ok(info);
    }
}
