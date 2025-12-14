
package handler;

import com.google.gson.JsonObject;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import dao.MongoConnection;
import org.bson.Document;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlockHandler implements BaseHandler {

    private static final String BLOCKS_COLLECTION = "blocks";

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {
        // Add CORS headers
        Map<String, String> corsHeaders = new HashMap<>();
        corsHeaders.put("Access-Control-Allow-Origin", "http://localhost:3000");
        corsHeaders.put("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");
        corsHeaders.put("Access-Control-Allow-Headers", "Content-Type");
        corsHeaders.put("Content-Type", "application/json");

        // Handle OPTIONS preflight
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return new ResponseBuilder()
                    .setHeaders(corsHeaders)
                    .setStatus("204 No Content")
                    .setBody(new RestApiAppResponse(true, null, null));
        }

        String method = request.getMethod();

        try {
            if ("POST".equalsIgnoreCase(method)) {
                return handleBlock(request, corsHeaders);
            } else if ("DELETE".equalsIgnoreCase(method)) {
                return handleUnblock(request, corsHeaders);
            } else if ("GET".equalsIgnoreCase(method)) {
                return handleGetBlocks(request, corsHeaders);
            }
        } catch (Exception e) {
            return new ResponseBuilder()
                    .setHeaders(corsHeaders)
                    .setStatus("500 Internal Server Error")
                    .setBody(new RestApiAppResponse(false, null, e.getMessage()));
        }

        return new ResponseBuilder()
                .setHeaders(corsHeaders)
                .setStatus("405 Method Not Allowed")
                .setBody(new RestApiAppResponse(false, null, "Method not allowed"));
    }

    private ResponseBuilder handleBlock(ParsedRequest request, Map<String, String> headers) {
        JsonObject body = GsonTool.GSON.fromJson(request.getBody(), JsonObject.class);
        
        if (!body.has("blocker") || !body.has("blocked")) {
            return new ResponseBuilder()
                    .setHeaders(headers)
                    .setStatus("400 Bad Request")
                    .setBody(new RestApiAppResponse(
                            false,
                            null,
                            "Missing blocker or blocked field"));
        }

        String blocker = body.get("blocker").getAsString();
        String blocked = body.get("blocked").getAsString();

        // Check if the block already exists
        MongoCollection<Document> collection = MongoConnection.getCollection(BLOCKS_COLLECTION);
        Document query = new Document("blocker", blocker).append("blocked", blocked);
        
        if (collection.find(query).first() != null) {
            return new ResponseBuilder()
                    .setHeaders(headers)
                    .setStatus("200 OK")
                    .setBody(new RestApiAppResponse(true, null, "Already blocked"));
        }

        // Insert a new a block
        Document blockDoc = new Document("blocker", blocker)
                .append("blocked", blocked)
                .append("timestamp", System.currentTimeMillis());
        
        collection.insertOne(blockDoc);

        return new ResponseBuilder()
                .setHeaders(headers)
                .setStatus("200 OK")
                .setBody(new RestApiAppResponse(true, null, "Block added successfully"));
    }

    private ResponseBuilder handleUnblock(ParsedRequest request, Map<String, String> headers) {
        JsonObject body = GsonTool.GSON.fromJson(request.getBody(), JsonObject.class);
        
        if (!body.has("blocker") || !body.has("blocked")) {
            return new ResponseBuilder()
                    .setHeaders(headers)
                    .setStatus("400 Bad Request")
                    .setBody(new RestApiAppResponse(
                            false,
                            null,
                            "Missing blocker or blocked field"));
        }

        String blocker = body.get("blocker").getAsString();
        String blocked = body.get("blocked").getAsString();

        MongoCollection<Document> collection = MongoConnection.getCollection(BLOCKS_COLLECTION);
        Document query = new Document("blocker", blocker).append("blocked", blocked);
        
        collection.deleteOne(query);

        return new ResponseBuilder()
                .setHeaders(headers)
                .setStatus("200 OK")
                .setBody(new RestApiAppResponse(true, null, "Block removed successfully"));
    }

    private ResponseBuilder handleGetBlocks(ParsedRequest request, Map<String, String> headers) {
        String user = request.getQueryParam("user");
        
        if (user == null || user.isEmpty()) {
            return new ResponseBuilder()
                    .setHeaders(headers)
                    .setStatus("400 Bad Request")
                    .setBody(new RestApiAppResponse(false, null, "Missing user query parameter"));
        }

        MongoCollection<Document> collection = MongoConnection.getCollection(BLOCKS_COLLECTION);
        Document query = new Document("blocker", user);
        
        List<String> blockedUsers = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(query).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                blockedUsers.add(doc.getString("blocked"));
            }
        }

        // Return the list as JSON array manually in the message field
        // Since RestApiAppResponse expects List<BaseDto>, we'll use the raw response approach
        headers.put("Content-Type", "application/json");
        String jsonResponse = GsonTool.GSON.toJson(blockedUsers);
        
        return new ResponseBuilder()
                .setHeaders(headers)
                .setStatus("200 OK")
                .setBody(new RestApiAppResponse(true, null, jsonResponse));
    }
}