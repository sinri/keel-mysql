package io.github.sinri.keel.integration.mysql.result.pagination;

import io.github.sinri.keel.integration.mysql.result.matrix.ResultMatrix;
import io.vertx.core.json.JsonObject;
import org.jspecify.annotations.NullMarked;

@NullMarked
public record PaginationResult(long total, ResultMatrix resultMatrix) {
    /**
     * @since 4.0.8
     */
    public JsonObject toJsonObject() {
        return new JsonObject()
                .put("total", total)
                .put("list", resultMatrix.toJsonArray());
    }
}
