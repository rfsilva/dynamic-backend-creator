package com.rfsilva.jcodemodel.service.generator.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.rfsilva.jcodemodel.dto.ChildEntityDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition;
import com.rfsilva.jcodemodel.dto.FieldDefinition.FieldType;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Generates a Postman collection (v2.1) with success-case CRUD requests
 * for the parent entity and each child entity.
 */
@Component
public class PostmanCollectionFileGenerator implements FileGenerator {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String generate(GenerationContext ctx, String outputDir) throws IOException {
        String fileName = ctx.entityName().toLowerCase() + ".collection.json";
        new File(outputDir + "/postman").mkdirs();

        ObjectNode root = buildCollection(ctx);

        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(new File(outputDir + "/postman/" + fileName), root);

        return "postman/" + fileName;
    }

    // -------------------------------------------------------------------------

    private ObjectNode buildCollection(GenerationContext ctx) {
        String entity = ctx.entityName();
        List<ChildEntityDefinition> children = ctx.getDefinition().getChildren();

        ObjectNode root = mapper.createObjectNode();

        // info
        ObjectNode info = root.putObject("info");
        info.put("_postman_id", UUID.randomUUID().toString());
        info.put("name", entity + " Service");
        info.put("schema", "https://schema.getpostman.com/json/collection/v2.1.0/collection.json");

        ArrayNode items = root.putArray("item");

        // ── Parent entity folder ─────────────────────────────────────────────
        items.add(buildParentFolder(ctx, entity));

        // ── Child entity folders ─────────────────────────────────────────────
        if (children != null) {
            for (ChildEntityDefinition child : children) {
                items.add(buildChildFolder(child, entity));
            }
        }

        return root;
    }

    // ── Parent folder ────────────────────────────────────────────────────────

    private ObjectNode buildParentFolder(GenerationContext ctx, String entity) {
        ObjectNode folder = mapper.createObjectNode();
        folder.put("name", entity);
        ArrayNode items = folder.putArray("item");

        String kebab      = toKebabCase(entity);
        String camel      = toCamelCase(entity);
        String basePath   = "/api/" + kebab + "s";
        String idVar      = "{{" + camel + "Id}}";
        String idCapture  = captureIdScript(camel + "Id");

        items.add(buildRequest("GET " + entity + "s",
                "GET", basePath, null,
                testStatus(200)));

        items.add(buildRequest("GET " + entity + " by id",
                "GET", basePath + "/" + idVar, null,
                testStatus(200)));

        items.add(buildRequest("POST " + entity,
                "POST", basePath, buildBody(ctx.fields()),
                testStatus(201) + "\n" + idCapture));

        items.add(buildRequest("PUT " + entity,
                "PUT", basePath + "/" + idVar, buildBody(ctx.fields()),
                testStatus(200)));

        items.add(buildRequest("DELETE " + entity,
                "DELETE", basePath + "/" + idVar, null,
                testStatus(204)));

        return folder;
    }

    // ── Child folder ─────────────────────────────────────────────────────────

    private ObjectNode buildChildFolder(ChildEntityDefinition child, String parentName) {
        String childName   = child.getEntityName();
        String childKebab  = toKebabCase(childName);
        String childCamel  = toCamelCase(childName);
        String parentKebab = toKebabCase(parentName);
        String parentCamel = toCamelCase(parentName);
        String parentIdVar = "{{" + parentCamel + "Id}}";
        String basePath    = "/api/" + parentKebab + "s/" + parentIdVar + "/" + childKebab + "s";
        String idVar       = "{{" + childCamel + "Id}}";
        String idCapture   = captureIdScript(childCamel + "Id");

        ObjectNode folder = mapper.createObjectNode();
        folder.put("name", childName);
        ArrayNode items = folder.putArray("item");

        items.add(buildRequest("GET " + childName + "s",
                "GET", basePath, null,
                testStatus(200)));

        items.add(buildRequest("GET " + childName + " by id",
                "GET", basePath + "/" + idVar, null,
                testStatus(200)));

        items.add(buildRequest("POST " + childName,
                "POST", basePath, buildBody(child.getFields()),
                testStatus(201) + "\n" + idCapture));

        items.add(buildRequest("PUT " + childName,
                "PUT", basePath + "/" + idVar, buildBody(child.getFields()),
                testStatus(200)));

        items.add(buildRequest("DELETE " + childName,
                "DELETE", basePath + "/" + idVar, null,
                testStatus(204)));

        return folder;
    }

    // ── Request builder ───────────────────────────────────────────────────────

    private ObjectNode buildRequest(String name, String method, String path,
                                    String body, String testScript) {
        ObjectNode item = mapper.createObjectNode();
        item.put("name", name);

        // test event
        ObjectNode event = mapper.createObjectNode();
        event.put("listen", "test");
        ObjectNode script = event.putObject("script");
        script.put("type", "text/javascript");
        ArrayNode exec = script.putArray("exec");
        for (String line : testScript.split("\n")) {
            exec.add(line);
        }
        item.putArray("event").add(event);

        // request
        ObjectNode request = item.putObject("request");
        request.put("method", method);
        ArrayNode headers = request.putArray("header");
        if (body != null) {
            ObjectNode h = headers.addObject();
            h.put("key", "Content-Type");
            h.put("value", "application/json");
        }

        // url
        ObjectNode url = request.putObject("url");
        url.put("raw", "{{baseUrl}}" + path);
        url.putArray("host").add("{{baseUrl}}");
        ArrayNode pathArr = url.putArray("path");
        for (String segment : path.replaceAll("^/", "").split("/")) {
            pathArr.add(segment);
        }

        // body
        if (body != null) {
            ObjectNode bodyNode = request.putObject("body");
            bodyNode.put("mode", "raw");
            bodyNode.put("raw", body);
            bodyNode.putObject("options").putObject("raw").put("language", "json");
        }

        return item;
    }

    // ── Body generation ───────────────────────────────────────────────────────

    private String buildBody(List<FieldDefinition> fields) {
        ObjectNode node = mapper.createObjectNode();
        for (FieldDefinition f : fields) {
            putSampleValue(node, f);
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node);
        } catch (IOException e) {
            return "{}";
        }
    }

    private void putSampleValue(ObjectNode node, FieldDefinition f) {
        String name = f.getName();
        FieldType type = f.getType();
        switch (type) {
            case STRING       -> node.put(name, sampleString(name, f.getMaxLength()));
            case INTEGER      -> node.put(name, 1);
            case LONG         -> node.put(name, 1);
            case DOUBLE       -> node.put(name, 1.0);
            case BIG_DECIMAL  -> node.put(name, "10.00");
            case BOOLEAN      -> node.put(name, true);
            case LOCAL_DATE   -> node.put(name, "2025-01-01");
            case LOCAL_DATE_TIME -> node.put(name, "2025-01-01T10:00:00");
        }
    }

    private String sampleString(String fieldName, Integer maxLength) {
        String base = switch (fieldName.toLowerCase()) {
            case "name", "nome"          -> "Sample Name";
            case "email"                 -> "sample@example.com";
            case "description", "descricao" -> "Sample description";
            case "status"                -> "ACTIVE";
            case "cpf"                   -> "000.000.000-00";
            case "cep"                   -> "00000-000";
            case "telefone", "numero"    -> "(11) 90000-0000";
            case "tipo"                  -> "MOBILE";
            case "logradouro"            -> "Rua das Flores";
            case "cidade"                -> "São Paulo";
            case "estado"                -> "SP";
            default                      -> "sample";
        };
        if (maxLength != null && base.length() > maxLength) {
            return base.substring(0, maxLength);
        }
        return base;
    }

    // ── Test script helpers ───────────────────────────────────────────────────

    private String testStatus(int status) {
        return "pm.test('Status " + status + "', () => pm.response.to.have.status(" + status + "));";
    }

    private String captureIdScript(String varName) {
        return "if (pm.response.code === 201) { pm.environment.set('" + varName + "', pm.response.json().id); }";
    }

    /** "SalesOrder" → "sales-order" */
    private static String toKebabCase(String name) {
        return name.replaceAll("([A-Z])", "-$1").toLowerCase().replaceFirst("^-", "");
    }

    /** "SalesOrder" → "salesOrder" */
    private static String toCamelCase(String name) {
        if (name == null || name.isEmpty()) return name;
        return Character.toLowerCase(name.charAt(0)) + name.substring(1);
    }
}
