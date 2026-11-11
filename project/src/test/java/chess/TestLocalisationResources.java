package chess;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

/** Ensures our shipped Simplified Chinese locale stays aligned with English. */
class TestLocalisationResources {
    private static final String LANG_PATH = "assets/chessmod/lang/";
    private static final Pattern FORMAT = Pattern.compile(
            "%(?:\\d+\\$)?[-#+ 0,(]*\\d*(?:\\.\\d+)?[a-zA-Z%]");

    private JsonObject readLocale(String locale) throws IOException {
        try (InputStream stream = getClass().getClassLoader()
                .getResourceAsStream(LANG_PATH + locale + ".json")) {
            assertNotNull(stream, "Missing packaged locale: " + locale);
            JsonElement json = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
            assertTrue(json.isJsonObject(), "Locale must be a JSON object: " + locale);
            return json.getAsJsonObject();
        }
    }

    private List<String> formatArguments(String value) {
        List<String> arguments = new ArrayList<>();
        Matcher matcher = FORMAT.matcher(value);
        while (matcher.find()) {
            String token = matcher.group();
            if (!token.equals("%%")) {
                arguments.add(token.replaceAll("^%(?:\\d+\\$)?", "%")
                        .replaceAll("^%[-#+ 0,(]*\\d*(?:\\.\\d+)?", ""));
            }
        }
        return arguments;
    }

    @Test
    void simplifiedChineseMatchesEnglishKeysAndFormatTypes() throws IOException {
        JsonObject english = readLocale("en_us");
        JsonObject chinese = readLocale("zh_cn");
        assertEquals(english.keySet(), chinese.keySet(), "Translation keys must match exactly");
        for (Map.Entry<String, JsonElement> entry : english.entrySet()) {
            String key = entry.getKey();
            assertTrue(entry.getValue().isJsonPrimitive() && entry.getValue().getAsJsonPrimitive().isString());
            JsonElement translated = chinese.get(key);
            assertTrue(translated.isJsonPrimitive() && translated.getAsJsonPrimitive().isString(), key);
            assertEquals(formatArguments(entry.getValue().getAsString()),
                    formatArguments(translated.getAsString()), "Format arguments differ for " + key);
        }
    }
}
