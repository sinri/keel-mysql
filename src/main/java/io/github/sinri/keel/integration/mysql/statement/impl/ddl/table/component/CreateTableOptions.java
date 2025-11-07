package io.github.sinri.keel.integration.mysql.statement.impl.ddl.table.component;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * @since 4.0.4
 */
public class CreateTableOptions {
    private final Map<String, String> options = new HashMap<>();

    public CreateTableOptions setOption(String key, String expression) {
        options.put(key, expression);
        return this;
    }

    @Override
    public String toString() {
        if (options.isEmpty()) {
            return "";
        }
        return options.entrySet().stream()
                      .map(entry -> entry.getKey() + "=" + entry.getValue())
                      .collect(Collectors.joining(", "));
    }
}
