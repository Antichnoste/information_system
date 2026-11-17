package org.example.lab.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistoryPageResponse {
    private List<ImportHistoryResponse> items;
    private long total;
    private int page;
    private int size;
}
