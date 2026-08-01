package io.github.ragecraft4reforged.runeforge;

import com.google.gson.Gson;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RuneCatalog {
    private static final List<RuneDefinition> ALL = load();
    private static final Map<String, RuneDefinition> BY_NAME = index();

    public static List<RuneDefinition> all() {
        return ALL;
    }

    public static RuneDefinition byName(String name) {
        RuneDefinition definition = BY_NAME.get(name);
        if (definition == null) {
            throw new IllegalArgumentException("Unknown generated rune: " + name);
        }
        return definition;
    }

    private static List<RuneDefinition> load() {
        try (InputStream stream = RuneCatalog.class.getResourceAsStream("/data/ragecraft4reforged/runes.json")) {
            if (stream == null) {
                throw new IllegalStateException("Missing generated rune catalog");
            }
            RuneDefinition[] definitions = new Gson().fromJson(
                    new InputStreamReader(stream, StandardCharsets.UTF_8), RuneDefinition[].class);
            if (definitions.length != 284) {
                throw new IllegalStateException("Expected 284 deployed RC4 runes, found " + definitions.length);
            }
            return Collections.unmodifiableList(Arrays.asList(definitions));
        } catch (java.io.IOException exception) {
            throw new IllegalStateException("Could not load generated rune catalog", exception);
        }
    }

    private static Map<String, RuneDefinition> index() {
        Map<String, RuneDefinition> result = new LinkedHashMap<>();
        for (RuneDefinition definition : ALL) {
            if (result.put(definition.registryName(), definition) != null) {
                throw new IllegalStateException("Duplicate generated rune name: " + definition.registryName());
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private RuneCatalog() {
    }
}
