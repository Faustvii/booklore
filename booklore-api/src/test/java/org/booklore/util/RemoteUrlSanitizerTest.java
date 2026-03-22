package org.booklore.util;

import org.booklore.exception.APIException;
import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RemoteUrlSanitizerTest {

    private final RemoteUrlSanitizer sanitizer = new RemoteUrlSanitizer();

    @Test
    void acceptsHttpsUrlFromAllowedHost() {
        URI uri = sanitizer.sanitizeHttpUrl("https://m.media-amazon.com/images/I/71+LMbpFYJL._SL1500_.jpg");

        assertThat(uri.toString()).isEqualTo("https://m.media-amazon.com/images/I/71+LMbpFYJL._SL1500_.jpg");
    }

    @Test
    void acceptsHttpUrlFromAllowedHost() {
        URI uri = sanitizer.sanitizeHttpUrl("http://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1642013459i/60111529.jpg");

        assertThat(uri.toString()).isEqualTo("http://images-na.ssl-images-amazon.com/images/S/compressed.photo.goodreads.com/books/1642013459i/60111529.jpg");
    }

    @Test
    void rejectsMalformedUrl() {
        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("https://example.com/%zz"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("Invalid remote URL input");
    }

    @Test
    void rejectsUrlWithoutHost() {
        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("https:///no-host"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("URL host is required");
    }

    @Test
    void rejectsLocalFilePathAndFileScheme() {
        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("/tmp/cover.jpg"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("URL scheme is required");

        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("file:///tmp/cover.jpg"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("file URLs are not allowed");
    }

    @Test
    void rejectsHostOutsideAllowlist() {
        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("https://example.com/image.jpg"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("URL host is not allowed");

        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("http://localhost/image.jpg"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("URL host is not allowed");
    }

    @Test
    void acceptsCloudfrontHostFromAllowlist() {
        URI uri = sanitizer.sanitizeHttpUrl("https://d28hgpri8am2if.cloudfront.net/book_images/onix/cvr9781638493297/the-primal-hunter-light-novel-vol-4-9781638493297_hr.jpg");

        assertThat(uri.toString()).isEqualTo("https://d28hgpri8am2if.cloudfront.net/book_images/onix/cvr9781638493297/the-primal-hunter-light-novel-vol-4-9781638493297_hr.jpg");
    }

    @Test
    void acceptsBaseSuffixHostsToo() {
        URI media = sanitizer.sanitizeHttpUrl("https://media-amazon.com/test.jpg");
        URI ssl = sanitizer.sanitizeHttpUrl("https://ssl-images-amazon.com/test.jpg");
        URI cloudfront = sanitizer.sanitizeHttpUrl("https://cloudfront.net/test.jpg");

        assertThat(media.toString()).isEqualTo("https://media-amazon.com/test.jpg");
        assertThat(ssl.toString()).isEqualTo("https://ssl-images-amazon.com/test.jpg");
        assertThat(cloudfront.toString()).isEqualTo("https://cloudfront.net/test.jpg");
    }

    @Test
    void rejectsSuffixSpoofingHosts() {
        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("https://media-amazon.com.evil.org/test.jpg"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("URL host is not allowed");

        assertThatThrownBy(() -> sanitizer.sanitizeHttpUrl("https://evilssl-images-amazon.com/test.jpg"))
                .isInstanceOf(APIException.class)
                .hasMessageContaining("URL host is not allowed");
    }
}
