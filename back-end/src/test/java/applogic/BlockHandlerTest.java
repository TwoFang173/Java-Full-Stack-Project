package applogic;

import handler.BlockHandler;
import org.testng.Assert;
import org.testng.annotations.Test;
import request.ParsedRequest;
import response.CustomHttpResponse;
import response.ResponseBuilder;

public class BlockHandlerTest {

    @Test
    public void testBlockUserWithValidData() {
        String requestBody = "{\"blocker\":\"marco\",\"blocked\":\"testUser\"}";
        ParsedRequest request = new ParsedRequest();
        request.setMethod("POST");
        request.setBody(requestBody);

        BlockHandler handler = new BlockHandler();
        ResponseBuilder responseBuilder = handler.handleRequest(request);
        CustomHttpResponse response = responseBuilder.build();

        Assert.assertTrue(response.status.contains("200"));
        Assert.assertNotNull(response.body);
    }

    @Test
    public void testUnblockUser() {
        String requestBody = "{\"blocker\":\"marco\",\"blocked\":\"testUser\"}";
        ParsedRequest request = new ParsedRequest();
        request.setMethod("DELETE");
        request.setBody(requestBody);

        BlockHandler handler = new BlockHandler();
        ResponseBuilder responseBuilder = handler.handleRequest(request);
        CustomHttpResponse response = responseBuilder.build();

        Assert.assertTrue(response.status.contains("200"));
        Assert.assertTrue(response.body.contains("Block removed successfully"));
    }

    @Test
    public void testGetBlocksWithUser() {
        ParsedRequest request = new ParsedRequest();
        request.setMethod("GET");
        request.setQueryParam("user", "marco");

        BlockHandler handler = new BlockHandler();
        ResponseBuilder responseBuilder = handler.handleRequest(request);
        CustomHttpResponse response = responseBuilder.build();

        Assert.assertTrue(response.status.contains("200"));
        Assert.assertNotNull(response.body);
    }

    @Test
    public void testBlockUserMissingFields() {
        String requestBody = "{\"blocker\":\"marco\"}";
        ParsedRequest request = new ParsedRequest();
        request.setMethod("POST");
        request.setBody(requestBody);

        BlockHandler handler = new BlockHandler();
        ResponseBuilder responseBuilder = handler.handleRequest(request);
        CustomHttpResponse response = responseBuilder.build();

        Assert.assertTrue(response.status.contains("400"));
        Assert.assertTrue(response.body.contains("Missing blocker or blocked field"));
    }

    @Test
    public void testGetBlocksMissingUserParam() {
        ParsedRequest request = new ParsedRequest();
        request.setMethod("GET");

        BlockHandler handler = new BlockHandler();
        ResponseBuilder responseBuilder = handler.handleRequest(request);
        CustomHttpResponse response = responseBuilder.build();

        Assert.assertTrue(response.status.contains("400"));
        Assert.assertTrue(response.body.contains("Missing user query parameter"));
    }

    @Test
    public void testOptionsRequest() {
        ParsedRequest request = new ParsedRequest();
        request.setMethod("OPTIONS");

        BlockHandler handler = new BlockHandler();
        ResponseBuilder responseBuilder = handler.handleRequest(request);
        CustomHttpResponse response = responseBuilder.build();

        Assert.assertTrue(response.status.contains("204"));
    }
}
