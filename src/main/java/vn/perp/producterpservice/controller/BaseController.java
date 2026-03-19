package vn.perp.producterpservice.controller;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.UUID;

public abstract class BaseController {

    protected Pageable buildPageable(int page, int size, String sort) {
        String sortField = "createdAt";
        Sort.Direction direction = Sort.Direction.DESC;

        if (sort != null && !sort.isBlank()) {
            String[] values = sort.split(",");
            sortField = values[0];
            if (values.length > 1) {
                direction = Sort.Direction.fromOptionalString(values[1]).orElse(Sort.Direction.DESC);
            }
        }

        return PageRequest.of(page, size, Sort.by(direction, sortField));
    }

    protected UUID parseUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            return null;
        }
        return UUID.fromString(userIdHeader.trim());
    }
}
