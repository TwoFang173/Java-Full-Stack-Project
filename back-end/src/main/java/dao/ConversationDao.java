package dao;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.result.DeleteResult;
import dto.ConversationDto;
import org.bson.Document;
import com.mongodb.client.model.Filters;

import java.util.function.Supplier;

// TODO fill this out
public class ConversationDao extends BaseDao<ConversationDto> {

    private static ConversationDao instance;
    private static Supplier<ConversationDao> instanceSupplier = () -> {
        return new ConversationDao(MongoConnection.getCollection("ConversationDao"));
    };

    private ConversationDao(MongoCollection<Document> collection) {
        super(collection);
    }

    public static ConversationDao getInstance() {
        if (instance != null) {
            return instance;
        }
        instance = instanceSupplier.get();
        return instance;
    }

    public static void setInstanceSupplier(Supplier<ConversationDao> instanceSupplier){
        ConversationDao.instanceSupplier = instanceSupplier;
    }

    @Override
    Supplier<ConversationDto> getFromDocument(Document document) {
        var auth = new ConversationDto();
        auth.fromDocument(document);
        return () -> auth;
    }

    // Nicholas Blackson
    // Deletes the conversation document with the given conversationId.
    // Returns true if a document was deleted.
    public boolean deleteByConversationId(String conversationId) {
        DeleteResult result = collection.deleteOne(Filters.eq("conversationId", conversationId));
        return result.getDeletedCount() > 0;
    }
}
