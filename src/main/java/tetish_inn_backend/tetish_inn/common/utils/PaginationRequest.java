package tetish_inn_backend.tetish_inn.common.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    private int start = 0;     // offset
    private int limit = 10;    // page size
    private String search = ""; // search keyword
}

