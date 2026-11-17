package org.example.lab.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.lab.model.ImportStatus;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ImportHistoryResponse {
    private Integer id;
    private ImportStatus status;
    private String username;
    private Integer addedCount;
    private String errorMessage;
}
