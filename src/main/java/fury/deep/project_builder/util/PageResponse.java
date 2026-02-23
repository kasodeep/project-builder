package fury.deep.project_builder.util;

import java.util.List;

/**
 * Generic paginated response wrapper.
 *
 * @param <T> the type of content items
 */
public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int page,
        int size,
        int totalPages
) {
    public PageResponse(List<T> content, long totalElements, int page, int size) {
        this(
                content,
                totalElements,
                page,
                size,
                size == 0 ? 0 : (int) Math.ceil((double) totalElements / size)
        );
    }
}