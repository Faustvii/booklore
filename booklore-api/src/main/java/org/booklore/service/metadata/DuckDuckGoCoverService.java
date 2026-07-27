package org.booklore.service.metadata;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.booklore.model.dto.CoverImage;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
@AllArgsConstructor
public class DuckDuckGoCoverService {

    private static final String SEARCH_BASE_URL = "https://duckduckgo.com/?q=";
    private static final String JSON_BASE_URL = "https://duckduckgo.com/i.js?o=json&q=";

    private static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
    private static final String REFERRER = "https://duckduckgo.com/";
    private static final Map<String, String> HTML_HEADERS = Map.ofEntries(
            Map.entry("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8"),
            Map.entry("accept-language", "en-US,en;q=0.9"),
            Map.entry("sec-ch-ua", "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\""),
            Map.entry("sec-ch-ua-mobile", "?0"),
            Map.entry("sec-ch-ua-platform", "\"macOS\""),
            Map.entry("sec-fetch-dest", "document"),
            Map.entry("sec-fetch-mode", "navigate"),
            Map.entry("sec-fetch-site", "same-origin"),
            Map.entry("sec-fetch-user", "?1"),
            Map.entry("upgrade-insecure-requests", "1"),
            Map.entry("user-agent", USER_AGENT)
    );
    private static final Map<String, String> JSON_HEADERS = Map.ofEntries(
            Map.entry("accept", "application/json, text/javascript, */*; q=0.01"),
            Map.entry("accept-language", "en-US,en;q=0.9"),
            Map.entry("sec-ch-ua", "\"Google Chrome\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\""),
            Map.entry("sec-ch-ua-mobile", "?0"),
            Map.entry("sec-ch-ua-platform", "\"macOS\""),
            Map.entry("sec-fetch-dest", "empty"),
            Map.entry("sec-fetch-mode", "cors"),
            Map.entry("sec-fetch-site", "same-origin"),
            Map.entry("x-requested-with", "XMLHttpRequest"),
            Map.entry("user-agent", USER_AGENT)
    );

    private final ObjectMapper mapper;

    public List<CoverImage> searchImages(String searchTerm) {
        String searchParams = "&iar=images";
        String jsonParams = "&iar=images";

        String encodedQuery = URLEncoder.encode(searchTerm, StandardCharsets.UTF_8);
        String searchUrl = SEARCH_BASE_URL + encodedQuery + searchParams;
        Connection.Response response = getResponse(searchUrl);
        Document doc = parseResponse(response);
        Map<String, String> cookies = response.cookies();
        Pattern tokenPattern = Pattern.compile("vqd=\"(\\d+-\\d+)\"");
        Matcher matcher = tokenPattern.matcher(doc.html());
        if (!matcher.find()) {
            log.error("Could not find search token for image search");
            return Collections.emptyList();
        }
        String searchToken = matcher.group(1);
        List<CoverImage> images = fetchImagesFromApi(searchTerm, searchToken, cookies, searchUrl, jsonParams);

        for (int i = 0; i < images.size(); i++) {
            CoverImage img = images.get(i);
            images.set(i, new CoverImage(img.getUrl(), img.getWidth(), img.getHeight(), i + 1));
        }

        return images;
    }

    private List<CoverImage> fetchImagesFromApi(String query, String searchToken, Map<String, String> cookies, String referrerUrl, String jsonParams) {
        List<CoverImage> priority = new ArrayList<>();
        List<CoverImage> others = new ArrayList<>();
        try {
            String url = JSON_BASE_URL
                    + URLEncoder.encode(query, StandardCharsets.UTF_8)
                    + jsonParams
                    + "&vqd=" + searchToken;

            Connection.Response resp = Jsoup.connect(url)
                    .ignoreContentType(true)
                    .referrer(referrerUrl)
                    .followRedirects(true)
                    .headers(JSON_HEADERS)
                    .header("x-vqd-4", searchToken)
                    .cookies(cookies)
                    .method(Connection.Method.GET)
                    .execute();

            String json = resp.body();
            JsonNode results = mapper.readTree(json).path("results");
            if (results.isArray()) {
                for (JsonNode img : results) {
                    String link = img.path("image").asText();
                    int w = img.path("width").asInt();
                    int h = img.path("height").asInt();
                    CoverImage dto = new CoverImage(link, w, h, 0);
                    if (link.contains("amazon") || link.contains("goodreads")) {
                        priority.add(dto);
                    } else {
                        others.add(dto);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error fetching images from DuckDuckGo", e);
        }
        List<CoverImage> all = new ArrayList<>(priority);
        all.addAll(others);
        return all;
    }

    private Connection.Response getResponse(String url) {
        try {
            return Jsoup.connect(url)
                    .referrer(REFERRER)
                    .followRedirects(true)
                    .headers(HTML_HEADERS)
                    .method(Connection.Method.GET)
                    .execute();
        } catch (IOException e) {
            log.error("Error fetching url: {}", url, e);
            throw new RuntimeException(e);
        }
    }

    private Document parseResponse(Connection.Response response) {
        try {
            return response.parse();
        } catch (IOException e) {
            log.error("Error parsing response", e);
            throw new RuntimeException(e);
        }
    }
}
