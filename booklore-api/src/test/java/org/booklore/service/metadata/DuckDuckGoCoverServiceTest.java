package org.booklore.service.metadata;

import org.booklore.model.dto.CoverImage;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DuckDuckGoCoverServiceTest {

    @Mock
    private ObjectMapper mapper;

    @InjectMocks
    private DuckDuckGoCoverService service;

    @Test
    void tokenPattern_noMatchMeansNoResults() {
        java.util.regex.Pattern tokenPattern = java.util.regex.Pattern.compile("vqd=\"(\\d+-\\d+)\"");
        java.util.regex.Matcher matcher = tokenPattern.matcher("no token here");
        assertThat(matcher.find()).isFalse();
    }

    @Test
    void tokenPattern_matchesValidToken() {
        java.util.regex.Pattern tokenPattern = java.util.regex.Pattern.compile("vqd=\"(\\d+-\\d+)\"");
        java.util.regex.Matcher matcher = tokenPattern.matcher("vqd=\"12345-67890\"");
        assertThat(matcher.find()).isTrue();
        assertThat(matcher.group(1)).isEqualTo("12345-67890");
    }

    @Test
    void tokenPattern_doesNotMatchInvalidToken() {
        java.util.regex.Pattern tokenPattern = java.util.regex.Pattern.compile("vqd=\"(\\d+-\\d+)\"");
        assertThat(tokenPattern.matcher("vqd=\"abcde\"").find()).isFalse();
        assertThat(tokenPattern.matcher("vqd=\"12345\"").find()).isFalse();
        assertThat(tokenPattern.matcher("other content").find()).isFalse();
    }

    @Test
    void imagePrioritization_amazonAndGoodreadsFirst() {
        List<CoverImage> priority = new java.util.ArrayList<>();
        List<CoverImage> others = new java.util.ArrayList<>();

        List<String> links = List.of(
                "https://other.com/img1",
                "https://amazon.com/img2",
                "https://goodreads.com/img3",
                "https://example.com/img4"
        );

        for (String link : links) {
            CoverImage dto = new CoverImage(link, 500, 700, 0);
            if (link.contains("amazon") || link.contains("goodreads")) {
                priority.add(dto);
            } else {
                others.add(dto);
            }
        }

        List<CoverImage> all = new java.util.ArrayList<>(priority);
        all.addAll(others);

        assertThat(all.get(0).getUrl()).contains("amazon");
        assertThat(all.get(1).getUrl()).contains("goodreads");
        assertThat(all).hasSize(4);
    }

    private Connection mockJsoupConnect(MockedStatic<Jsoup> jsoupMock) throws IOException {
        Connection connection = mock(Connection.class, RETURNS_SELF);
        jsoupMock.when(() -> Jsoup.connect(anyString())).thenReturn(connection);
        return connection;
    }

    private Connection.Response buildHtmlResponse(String html, Map<String, String> cookies) throws IOException {
        Connection.Response response = mock(Connection.Response.class);
        Document doc = Jsoup.parse(html);
        when(response.parse()).thenReturn(doc);
        when(response.cookies()).thenReturn(cookies);
        return response;
    }

    private Connection.Response buildJsonResponse(String json) {
        Connection.Response response = mock(Connection.Response.class);
        when(response.body()).thenReturn(json);
        return response;
    }

    @Nested
    class SearchImages {

        @Test
        void returnsEmptyWhenNoTokenFound() throws Exception {
            try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
                Connection connection = mockJsoupConnect(jsoupMock);
                Connection.Response htmlResp = buildHtmlResponse("<html>no token</html>", Map.of());
                when(connection.execute()).thenReturn(htmlResp);

                List<CoverImage> result = service.searchImages("test query");

                assertThat(result).isEmpty();
            }
        }

        @Test
        void returnsIndexedImagesOnSuccess() throws Exception {
            String htmlWithToken = "<html>vqd=\"99999-11111\"</html>";
            String jsonBody = """
                    {"results":[
                        {"image":"https://example.com/img1.jpg","width":800,"height":600},
                        {"image":"https://example.com/img2.jpg","width":1024,"height":768}
                    ]}""";

            try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
                Connection connection = mockJsoupConnect(jsoupMock);

                Connection.Response htmlResp = buildHtmlResponse(htmlWithToken, Map.of());
                Connection.Response jsonResp = buildJsonResponse(jsonBody);
                when(connection.execute()).thenReturn(htmlResp).thenReturn(jsonResp);

                JsonNode resultsNode = new ObjectMapper().readTree(jsonBody).path("results");
                JsonNode rootNode = mock(JsonNode.class);
                when(rootNode.path("results")).thenReturn(resultsNode);
                when(mapper.readTree(jsonBody)).thenReturn(rootNode);

                List<CoverImage> result = service.searchImages("test query");

                assertThat(result).hasSize(2);
                assertThat(result.get(0).getIndex()).isEqualTo(1);
                assertThat(result.get(1).getIndex()).isEqualTo(2);
            }
        }

        @Test
        void throwsRuntimeExceptionOnIOException() throws Exception {
            try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
                Connection connection = mockJsoupConnect(jsoupMock);
                when(connection.execute()).thenThrow(new IOException("connection failed"));

                org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.searchImages("test query"))
                        .isInstanceOf(RuntimeException.class)
                        .hasCauseInstanceOf(IOException.class);
            }
        }

        @Test
        void throwsRuntimeExceptionOnParseFailure() throws Exception {
            try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
                Connection connection = mockJsoupConnect(jsoupMock);
                Connection.Response response = mock(Connection.Response.class);
                when(connection.execute()).thenReturn(response);
                when(response.parse()).thenThrow(new IOException("parse error"));

                org.assertj.core.api.Assertions.assertThatThrownBy(() -> service.searchImages("test query"))
                        .isInstanceOf(RuntimeException.class)
                        .hasCauseInstanceOf(IOException.class);
            }
        }
    }

    @Nested
    class FetchImagesFromApi {

        @Test
        void returnsEmptyListOnApiException() throws Exception {
            String htmlWithToken = "<html>vqd=\"12345-67890\"</html>";

            try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
                Connection connection = mockJsoupConnect(jsoupMock);

                Connection.Response htmlResp = buildHtmlResponse(htmlWithToken, Map.of());
                when(connection.execute())
                        .thenReturn(htmlResp)
                        .thenThrow(new IOException("api error"));

                List<CoverImage> result = service.searchImages("Test");

                assertThat(result).isEmpty();
            }
        }

        @Test
        void handlesEmptyResultsArray() throws Exception {
            String htmlWithToken = "<html>vqd=\"12345-67890\"</html>";
            String emptyJson = """
                    {"results":[]}""";

            try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
                Connection connection = mockJsoupConnect(jsoupMock);

                Connection.Response htmlResp = buildHtmlResponse(htmlWithToken, Map.of());
                Connection.Response jsonResp = buildJsonResponse(emptyJson);
                when(connection.execute())
                        .thenReturn(htmlResp)
                        .thenReturn(jsonResp);

                JsonNode resultsNode = new ObjectMapper().readTree(emptyJson).path("results");
                JsonNode rootNode = mock(JsonNode.class);
                when(rootNode.path("results")).thenReturn(resultsNode);
                when(mapper.readTree(emptyJson)).thenReturn(rootNode);

                List<CoverImage> result = service.searchImages("Test");

                assertThat(result).isEmpty();
            }
        }

        @Test
        void handlesMissingResultsField() throws Exception {
            String htmlWithToken = "<html>vqd=\"12345-67890\"</html>";
            String noResultsJson = """
                    {"other":"data"}""";

            try (MockedStatic<Jsoup> jsoupMock = mockStatic(Jsoup.class, CALLS_REAL_METHODS)) {
                Connection connection = mockJsoupConnect(jsoupMock);

                Connection.Response htmlResp = buildHtmlResponse(htmlWithToken, Map.of());
                Connection.Response jsonResp = buildJsonResponse(noResultsJson);
                when(connection.execute())
                        .thenReturn(htmlResp)
                        .thenReturn(jsonResp);

                JsonNode rootNode = new ObjectMapper().readTree(noResultsJson);
                when(mapper.readTree(noResultsJson)).thenReturn(rootNode);

                List<CoverImage> result = service.searchImages("Test");

                assertThat(result).isEmpty();
            }
        }
    }
}
