package bank.card.management.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PageRequestHelper {

    private static final String DEFAULT_SORT_BY = "id";

    private PageRequestHelper() {
    }

    public static Pageable createPageable(int page, int size, String sortBy) {
        String sortField = sortBy != null && !sortBy.trim().isEmpty() ? sortBy : DEFAULT_SORT_BY;
        return PageRequest.of(page, size, Sort.by(sortField).descending());
    }
}
