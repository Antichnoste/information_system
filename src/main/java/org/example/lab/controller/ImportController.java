package org.example.lab.controller;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.example.lab.dto.ImportHistoryPageResponse;
import org.example.lab.exception.GlobalExceptionHandler;
import org.example.lab.model.ImportHistory;
import org.example.lab.service.ImportFileParser;
import org.example.lab.service.ImportService;

@Path("imports")
@RequestScoped
@Produces(MediaType.APPLICATION_JSON)
public class ImportController {
    private static final int MAX_BYTES = 2_000_000;

    @Context
    HttpServletRequest request;
    @Inject
    ImportFileParser parser;
    @Inject
    ImportService imports;

    @GET
    public ImportHistoryPageResponse history(
            @DefaultValue("0") @QueryParam("page") int page,
            @DefaultValue("10") @QueryParam("size") int size) {
        return imports.history(username(), page, size);
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public Response upload(@FormParam("file") EntityPart file) {
        String username = username();

        try {
            byte[] bytes = readFile(file);
            ImportHistory history = imports.importHumans(username, parser.parse(bytes));
            return Response.status(201)
                    .entity(Map.of("id", history.id, "addedCount", history.addedCount))
                    .build();
        } catch (WebApplicationException error) {
            Object body = error.getResponse().getEntity();
            String message = body instanceof Map<?, ?> map && map.get("message") instanceof String text ? text : "Не удалось импортировать файл";
            imports.recordFailure(username, message);
            throw error;
        } catch (RuntimeException error) {
            imports.recordFailure(username, "Не удалось сохранить файл");
            throw error;
        }
    }

    private String username() {
        HttpSession session = request.getSession(false);
        if (session == null || !(session.getAttribute("username") instanceof String username)) {
            throw new WebApplicationException(GlobalExceptionHandler.response(401, "Войдите в систему"));
        }
        return username;
    }

    private byte[] readFile(EntityPart file) {
        if (file == null) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Выберите JSON-файл"));
        }
        try (InputStream input = file.getContent()) {
            byte[] bytes = input.readNBytes(MAX_BYTES + 1);
            if (bytes.length > MAX_BYTES) {
                throw new WebApplicationException(GlobalExceptionHandler.response(413, "Файл больше 2 МБ"));
            }
            if (bytes.length == 0) {
                throw new WebApplicationException(GlobalExceptionHandler.response(400, "Файл пустой"));
            }
            return bytes;
        } catch (IOException error) {
            throw new WebApplicationException(GlobalExceptionHandler.response(400, "Не удалось прочитать файл"));
        }
    }
}
