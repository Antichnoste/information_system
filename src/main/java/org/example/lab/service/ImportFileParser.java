package org.example.lab.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.ws.rs.WebApplicationException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import org.example.lab.dto.ImportHumanRequest;
import org.example.lab.exception.GlobalExceptionHandler;

@ApplicationScoped
public class ImportFileParser {
    @Inject
    Validator validator;

    public List<ImportHumanRequest> parse(byte[] bytes) {
        String text;
        try {
            text = StandardCharsets.UTF_8.newDecoder()
                    .onMalformedInput(CodingErrorAction.REPORT)
                    .decode(ByteBuffer.wrap(bytes)).toString();
        } catch (CharacterCodingException e) {
            throw error(400, "Файл должен быть в кодировке UTF-8");
        }

        ImportHumanRequest[] records;
        try (Jsonb jsonb = JsonbBuilder.create()) {
            records = jsonb.fromJson(text, ImportHumanRequest[].class);
        } catch (Exception e) {
            throw error(400, "Не удалось прочитать JSON-файл");
        }

        if (records == null || records.length == 0) {
            throw error(400, "Файл не содержит персонажей");
        }
        if (records.length > 1000) {
            throw error(413, "В файле должно быть не больше 1000 персонажей");
        }

        for (int i = 0; i < records.length; i++) {
            ImportHumanRequest record = records[i];
            if (record == null) {
                throw error(400, "Запись " + (i + 1) + ": ожидался персонаж");
            }
            for (ConstraintViolation<ImportHumanRequest> violation : validator.validate(record)) {
                throw error(400, "Запись " + (i + 1) + ", поле "
                        + violation.getPropertyPath() + ": " + violation.getMessage());
            }
            if (record.coordinates.y != null && !Float.isFinite(record.coordinates.y)) {
                throw error(400, "Запись " + (i + 1) + ", поле coordinates.y: число должно быть конечным");
            }
        }
        return Arrays.asList(records);
    }

    private WebApplicationException error(int status, String message) {
        return new WebApplicationException(GlobalExceptionHandler.response(status, message));
    }
}
