package applogic;

import dto.AuthDto;
import dto.ConversationDto;
import handler.GsonTool;
import handler.HandlerFactory;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import request.ParsedRequest;
import response.CustomHttpResponse;
import response.ResponseBuilder;
import response.StatusCodes;
import util.MockTestUtils;

import java.util.List;

public class DeleteConversationTest {

    private MockTestUtils testUtils;
    private final String FIXED_CONVERSATION_ID = "test-convo-1"; // fixed ID to avoid mismatch

    @BeforeMethod
    public void setup() {
        testUtils = new MockTestUtils();

        Mockito.reset(
                testUtils.mockConversationDao,
                testUtils.mockMessageDao,
                testUtils.mockAuthDao
        );

        // Inject mocks into DAO singletons
        dao.ConversationDao.setInstanceSupplier(() -> testUtils.mockConversationDao);
        dao.MessageDao.setInstanceSupplier(() -> testUtils.mockMessageDao);
    }

    @Test(singleThreaded = true)
    public void deleteConversationSuccessTest() {
        // --- 1. Prepare conversation ---
        ConversationDto conversation = new ConversationDto();
        conversation.setConversationId(FIXED_CONVERSATION_ID);
        conversation.setMessageCount(3);

        // --- 2. Mock DAO query ---
        Mockito.doReturn(List.of(conversation))
                .when(testUtils.mockConversationDao)
                .query("conversationId", FIXED_CONVERSATION_ID);

        // --- 3. Mock conversation deletion ---
        Mockito.doReturn(true)
                .when(testUtils.mockConversationDao)
                .deleteByConversationId(FIXED_CONVERSATION_ID);

        // --- 4. Mock message deletion ---
        Mockito.doReturn(conversation.getMessageCount().longValue())
                .when(testUtils.mockMessageDao)
                .deleteByConversationId(FIXED_CONVERSATION_ID);

        // --- 5. Mock auth ---
        AuthDto auth = new AuthDto();
        auth.setUserName("tester");
        auth.setHash("auth-hash-1");
        Mockito.doReturn(List.of(auth))
                .when(testUtils.mockAuthDao)
                .query("hash", auth.getHash());

        // --- 6. Build request (body-based) ---
        ParsedRequest parsedRequest = new ParsedRequest();
        parsedRequest.setPath("/deleteConversation");
        parsedRequest.setMethod("POST");
        parsedRequest.setBody(GsonTool.GSON.toJson(conversation));

        // Set auth cookie
        parsedRequest.setCookieValue("auth", auth.getHash());

        // --- 7. Call handler ---
        var handler = HandlerFactory.getHandler(parsedRequest);
        ResponseBuilder builder = handler.handleRequest(parsedRequest);
        CustomHttpResponse res = builder.build();

        // --- 8. Verify success ---
        Assert.assertEquals(res.status, StatusCodes.OK);

        // --- 9. Verify DAO deletion calls ---
        ArgumentCaptor<String> conversationIdCaptor = ArgumentCaptor.forClass(String.class);
        Mockito.verify(testUtils.mockConversationDao)
                .deleteByConversationId(conversationIdCaptor.capture());
        Assert.assertEquals(conversationIdCaptor.getValue(), FIXED_CONVERSATION_ID);

        Mockito.verify(testUtils.mockMessageDao)
                .deleteByConversationId(FIXED_CONVERSATION_ID);
    }

    @Test(singleThreaded = true)
    public void deleteConversationNotFoundTest() {
        // --- Prepare conversation with fixed ID ---
        ConversationDto conversation = new ConversationDto();
        conversation.setConversationId(FIXED_CONVERSATION_ID);

        // --- Mock DAO deletion to return false ---
        Mockito.doReturn(false)
                .when(testUtils.mockConversationDao)
                .deleteByConversationId(FIXED_CONVERSATION_ID);

        // --- Mock auth ---
        AuthDto auth = new AuthDto();
        auth.setUserName("tester");
        auth.setHash("auth-hash-1");
        Mockito.doReturn(List.of(auth))
                .when(testUtils.mockAuthDao)
                .query("hash", auth.getHash());

        // --- Build request ---
        ParsedRequest parsedRequest = new ParsedRequest();
        parsedRequest.setPath("/deleteConversation");
        parsedRequest.setMethod("POST");
        parsedRequest.setBody(GsonTool.GSON.toJson(conversation));
        parsedRequest.setCookieValue("auth", auth.getHash());

        // --- Call handler ---
        var handler = HandlerFactory.getHandler(parsedRequest);
        ResponseBuilder builder = handler.handleRequest(parsedRequest);
        CustomHttpResponse res = builder.build();

        // --- Verify 404 response ---
        Assert.assertEquals(res.status, "404 Not Found");
    }

    @Test(singleThreaded = true)
    public void deleteConversationUnauthorizedTest() {
        // --- Prepare conversation ---
        ConversationDto conversation = new ConversationDto();
        conversation.setConversationId(FIXED_CONVERSATION_ID);

        // --- Build request without auth cookie ---
        ParsedRequest parsedRequest = new ParsedRequest();
        parsedRequest.setPath("/deleteConversation");
        parsedRequest.setMethod("POST");
        parsedRequest.setBody(GsonTool.GSON.toJson(conversation));

        // --- Call handler ---
        var handler = HandlerFactory.getHandler(parsedRequest);
        ResponseBuilder builder = handler.handleRequest(parsedRequest);
        CustomHttpResponse res = builder.build();

        // --- Verify unauthorized ---
        Assert.assertEquals(res.status, StatusCodes.UNAUTHORIZED);
    }
}
