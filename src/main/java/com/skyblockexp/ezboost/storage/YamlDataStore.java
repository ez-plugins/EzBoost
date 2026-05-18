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
 * Each record is stored as a YAML section keyed by its path (e.g. "leaderboard.uuid").
 *
 * <p>Implements {@link QueryableStorage} so that {@link com.github.ezframework.jaloquent.model.ModelRepository#query}
 * works without a SQL backend. The {@link #query(Query)} method scans all sections in the YAML
 * file, evaluates the query's conditions in Java, applies ordering and limit, and returns
 * matching full paths (e.g. {@code "boost_states.abc-uuid"}).
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
     * Scan every two-level key (prefix → id) in the YAML file, apply the query's
     * conditions in Java, sort and limit, then return the matching full paths
     * (e.g. {@code "boost_states.abc-uuid"}).
     *
     * <p>The returned paths are handed back to {@link com.github.ezframework.jaloquent.model.ModelRepository}
     * which then loads each record by path and instantiates the model.
     */
    @Override
    public List<String> query(Query query) {
        List<String> conditions = new ArrayList<>(); // not used — see below

        // Collect all candidate {path, dataMap} pairs
        record Entry(String path, Map<String, Object> data) {}
        List<Entry> candidates = new ArrayList<>();

        for (String topKey : yaml.getKeys(false)) {
            ConfigurationSection prefixSection = yaml.getConfigurationSection(topKey);
            if (prefixSection == null) continue;
            for (String id : prefixSection.getKeys(false)) {
                String fullPath = topKey + "." + id;
                ConfigurationSection section = yaml.getConfigurationSection(fullPath);
                if (section == null) continue;
                Map<String, Object> data = new LinkedHashMap<>();
                for (String k : section.getKeys(false)) {
                    data.put(k, section.get(k));
                }
                candidates.add(new Entry(fullPath, data));
            }
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

        // Return only the paths
        List<String> result = new ArrayList<>(limited.size());
        for (Entry e : limited) result.add(e.path());
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
