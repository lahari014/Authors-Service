package services;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import play.api.Configuration;
import play.inject.ApplicationLifecycle;
import play.libs.Json;


import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.mongodb.client.model.Filters.eq;

@Singleton
public class MongoService {

    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> authors;
    private final MongoCollection<Document> publishers;

    @Inject
    public MongoService(Configuration configuration, ApplicationLifecycle lifecycle) {
        String uri = "mongodb+srv://laharikommineni:Lahari%40p2002@cluster0.8zlltkz.mongodb.net/";
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase("Books-Authors");
        authors=database.getCollection("authors");
        publishers=database.getCollection("publishers");

        // Registering a shutdown hook to close the MongoClient
        lifecycle.addStopHook(() -> {
            mongoClient.close();
            return CompletableFuture.completedFuture(null);
        });
    }

    public MongoDatabase getDatabase() {
        return database;
    }

    //To create Author
    public void createAuthor(Document doc){
        authors.insertOne(doc);
    }


    //To update Author
    public void updateAuthor(String name, Document updatedDoc) {
        authors.updateOne(eq("authorName", name), new Document("$set", updatedDoc));
    }


    //To delete Author From Collection
    public JsonNode deleteAuthor(Integer id){
        authors.deleteOne(new Document("id",id));
        return Json.newObject().put("message", "Author Deleted successfully");
    }

    //To get all the documents
    public JsonNode getAllAuthors(){
        List<Document> documentsList = new ArrayList<>();
        authors.find().projection(new Document("_id", 0)).into(documentsList);
        List<JsonNode> jsonList = new ArrayList<>();
        for (Document doc : documentsList) {
            JsonNode jsonNode = Json.parse(doc.toJson());
            jsonList.add(jsonNode);
        }
        return Json.toJson(jsonList);
    }

    public JsonNode getAuthor(String name){
        Document doc = authors.find(eq("authorName", name)).first();
        if (doc == null) {
            return null;
        }
        else{
            return Json.parse(doc.toJson());
        }
    }

    public JsonNode getPublisher(String name) {
        Document doc = publishers.find(eq("name", name)).first();
        if (doc == null) {
            return null;
        } else {
            return Json.parse(doc.toJson());
        }
    }

}


