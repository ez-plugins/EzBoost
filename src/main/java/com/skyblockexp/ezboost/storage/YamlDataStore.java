package com.skyblockexp.ezboost.storage;

import com.github.ezframework.jaloquent.store.DataStore;
import com.github.ezframework.javaquerybuilder.query.Query;
import com.github.ezframework.javaquerybuilder.query.QueryableStorage;
import com.github.ezframework.javaquerybuilder.query.condition.ConditionEntry;
import com.github.ezframework.javaquerybuilder.query.condition.Connector;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Jaloquent {@link DataStore} backed by a single YAML file.
 * Each record is stored as a YAML section keyed by its storage path (e.g. {@code "boost_states/uuid"}).
 * Jaloquent's {@link com.github.ezframework.jaloquent.model.BaseModel#getStoragePath} produces
 * paths in the form {@code prefix + "/" + id}, so the YAML file has a flat top-level structure
 * where each key is the full storage path of a record.
 *
 * <p>Implements {@link QueryableStorage} so that {@link com.github.ezframework.jaloquent.model.ModelRepository#query}
 * works without a SQL backend. The {@link #query(Query)} method scans all flat keys in the YAML
 * file, evaluates the query's conditions in Java, applies ordering and limit, and returns
 * the matching record IDs (the portion of the key after the last {@code /}).
 */
public final class YamlDataStore implements DataStore, QueryableStorage {

    private final File file;
    private final Logger logger;
    private YamlConfiguration yaml;

    public YamlDataStore(File file, Logger logger) {
        this.file = file;
        this.logger = logger;
        reload();
    }

    public void reload() {
        if (file.exists()) {
            yaml = YamlConfiguration.loadConfiguration(file);
        } else {
            yaml = new YamlConfiguration();
        }
    }

    @Override
    public void save(String path, Map<String, Object> data) {
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            yaml.set(path + "." + entry.getKey(), entry.getValue());
        }
        persist();
    }

    @Override
    public Optional<Map<String, Object>> load(String path) {
        ConfigurationSection section = yaml.getConfigurationSection(path);
        if (section == null) {
            return Optional.empty();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        for (String key : section.getKeys(false)) {
            result.put(key, section.get(key));
        }
        return Optional.of(result);
    }

    @Override
    public void delete(String path) {
        yaml.set(path, null);
        persist();
    }

    @Override
    public boolean exists(String path) {
        return yaml.isConfigurationSection(path) || yaml.contains(path);
    }

    // ── QueryableStorage ──────────────────────────────────────────────────────

    /**
     * Scan every record in the YAML file (top-level keys have the form {@code prefix/id}),
     * apply the query's conditions in Java, sort and limit, then return the matching
     * record IDs (the portion of the key after the last {@code /}).
     *
     * <p>The returned IDs are handed to {@link com.github.ezframework.jaloquent.model.ModelRepository}
     * which calls {@code find(id)} for each, reconstructing the model from storage.
     */
    @Override
    public List<String> query(Query query) {
        // Each top-level key is a storage path of the form "prefix/id".
        record Entry(String id, Map<String, Object> data) {}
        List<Entry> candidates = new ArrayList<>();

        for (String storageKey : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(storageKey);
            if (section == null) continue;
            // Extract the record ID (everything after the last '/')
            int slashIdx = storageKey.lastIndexOf('/');
            String id = slashIdx >= 0 ? storageKey.substring(slashIdx + 1) : storageKey;
            Map<String, Object> data = new LinkedHashMap<>();
            for (String k : section.getKeys(false)) {
                data.put(k, section.get(k));
            }
            candidates.add(new Entry(id, data));
        }

        // Apply WHERE conditions using Jaloquent's Condition.matches()
        List<ConditionEntry> whereEntries = query.getConditions();
        List<Entry> filtered = new ArrayList<>();
        outer:
        for (Entry e : candidates) {
            if (whereEntries == null || whereEntries.isEmpty()) {
                filtered.add(e);
                continue;
            }
            boolean result = true;
            for (ConditionEntry ce : whereEntries) {
                boolean matches = ce.getCondition().matches(e.data(), ce.getColumn());
                if (ce.getConnector() == Connector.OR) {
                    result = result || matches;
                } else { // AND
                    result = result && matches;
                    if (!result) continue outer;
                }
            }
            if (result) filtered.add(e);
        }

        // Apply ORDER BY (single column, first entry wins)
        List<String> orderCols = query.getOrderBy();
        List<Boolean> orderAsc = query.getOrderByAsc();
        if (orderCols != null && !orderCols.isEmpty()) {
            String col = orderCols.get(0);
            boolean asc = (orderAsc == null || orderAsc.isEmpty()) || Boolean.TRUE.equals(orderAsc.get(0));
            Comparator<Entry> cmp = Comparator.comparing(
                    e -> toComparable(e.data().get(col)),
                    Comparator.nullsFirst(Comparator.naturalOrder()));
            filtered.sort(asc ? cmp : cmp.reversed());
        }

        // Apply LIMIT
        Integer limit = query.getLimit();
        List<Entry> limited = (limit != null && limit > 0 && filtered.size() > limit)
                ? filtered.subList(0, limit)
                : filtered;

        // Return record IDs (not full storage paths)
        List<String> result = new ArrayList<>(limited.size());
        for (Entry e : limited) result.add(e.id());
        return result;
    }

    @SuppressWarnings("unchecked")
    private static Comparable<Object> toComparable(Object value) {
        if (value == null) return null;
        if (value instanceof Comparable) return (Comparable<Object>) value;
        return (Comparable<Object>) (Object) value.toString();
    }

    private void persist() {
        try {
            file.getParentFile().mkdirs();
            yaml.save(file);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Failed to save " + file.getName(), ex);
        }
    }
}
