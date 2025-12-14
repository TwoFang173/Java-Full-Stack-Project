package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import dto.ConversationDto;
import org.bson.Document;
import com.mongodb.client.model.Filters;

import java.util.function.Supplier;

public class ConversationDao extends BaseDao<ConversationDto> {

    private static ConversationDao instance;
    private static Supplier<ConversationDao> instanceSupplier = () -> {
        return new ConversationDao(MongoConnection.getCollection("ConversationDao"));
    };

    private ConversationDao(MongoCollection<Document> collection) {
        super(collection);
    }

    public static ConversationDao getInstance() {
        if (instance != null) return instance;
        instance = instanceSupplier.get();
        return instance;
    }

    // Allows injection of mocks in tests
    public static void setInstanceSupplier(Supplier<ConversationDao> supplier) {
        instanceSupplier = supplier;
        instance = null;
    }

    @Override
    Supplier<ConversationDto> getFromDocument(Document document) {
        ConversationDto dto = new ConversationDto();
        dto.fromDocument(document);
        return () -> dto;
    }

    // Deletes the conversation document by conversationId. Returns true if deleted.
    public boolean deleteByConversationId(String conversationId) {
        DeleteResult result = collection.deleteOne(Filters.eq("conversationId", conversationId));
        return result.getDeletedCount() > 0;
    }
}