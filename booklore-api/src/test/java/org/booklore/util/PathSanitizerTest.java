package org.booklore.util;

import org.booklore.exception.APIException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PathSanitizerTest {

    private final PathSanitizer sanitizer = new PathSanitizer();

    @Test
    void rejectsSeparatorsInFilenameOnlyInput() {
        assertThatThrownBy(() -> sanitizer.sanitizeFilenameComponent("folder/file.txt"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Filename must not contain path separators");

        assertThatThrownBy(() -> sanitizer.sanitizeFilenameComponent("folder\\file.txt"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Filename must not contain path separators");
    }

    @Test
    void rejectsAbsolutePathWhenRelativeExpected() {
        assertThatThrownBy(() -> sanitizer.resolveRelativePath(Path.of("/safe/base"), "/etc/passwd"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Absolute paths are not allowed");
    }

    @Test
    void rejectsParentTraversal() {
        assertThatThrownBy(() -> sanitizer.resolveRelativePath(Path.of("/safe/base"), "../escape.txt"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Parent traversal is not allowed");
    }

    @Test
    void acceptsValidRelativePathUnderBase() {
        Path resolved = sanitizer.resolveRelativePath(Path.of("/safe/base"), "covers/book.jpg");

        assertThat(resolved).isEqualTo(Path.of("/safe/base/covers/book.jpg"));
    }

    @Test
    void rejectsNormalizedEscapeFromTrustedBase() {
        assertThatThrownBy(() -> sanitizer.resolveRelativePath(Path.of("/safe/base"), "covers/../../outside.txt"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Parent traversal is not allowed");
    }
}
