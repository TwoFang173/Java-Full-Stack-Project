package handler;

import auth.AuthFilter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import request.ParsedRequest;
import response.ResponseBuilder;
import response.RestApiAppResponse;
import response.StatusCodes;

public class SendGroupMessageHandler implements BaseHandler {

    private final GroupChatHandler groupChat =
            GroupChatRegistry.getInstance();

    @Override
    public ResponseBuilder handleRequest(ParsedRequest request) {

        // ---------- AUTH ----------
        AuthFilter.AuthResult auth = AuthFilter.doFilter(request);
        if (!auth.isLoggedIn) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.UNAUTHORIZED);
        }

        // ---------- BODY ----------
        String body = request.getBody();
        if (body == null || body.isBlank()) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST);
        }

        JsonObject json;
        try {
            json = JsonParser.parseString(body).getAsJsonObject();
        } catch (Exception e) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST);
        }

        String groupId =
                json.has("groupId") ? json.get("groupId").getAsString() : null;
        String message =
                json.has("message") ? json.get("message").getAsString() : null;

        if (groupId == null || message == null || message.isBlank()) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST);
        }

        boolean ok = groupChat.sendMessage(
                auth.userName,   // ✅ correct field
                groupId,
                message
        );

        if (!ok) {
            return new ResponseBuilder()
                    .setStatus(StatusCodes.BAD_REQUEST);
        }

        return new ResponseBuilder()
                .setStatus(StatusCodes.OK)
                .setBody(new RestApiAppResponse<>(
                        true,
                        null,
                        null
                ));
    }
}
