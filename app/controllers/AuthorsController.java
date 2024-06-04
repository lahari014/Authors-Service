package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.client.MongoCollection;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Controller;
import play.mvc.Result;
import models.Author;
import services.*;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import javax.inject.Inject;
import static com.mongodb.client.model.Filters.eq;


public class AuthorsController extends Controller {

    private final FormFactory formFactory;
    private final MongoService mongoService;
    private final MongoDatabase database;
    private final WsService WsService;
    private final MongoCollection<Document> authors;


    @Inject
    public AuthorsController(FormFactory formFactory, MongoService mongoService, WsService WsService) {
        this.formFactory = formFactory;
        this.mongoService = mongoService;
        this.database = mongoService.getDatabase();
        this.WsService=WsService;
        this.authors=database.getCollection("authors");
    }


    //To create Author
    public Result create() {
        // Parse the request body as JSON
        JsonNode json = request().body().asJson();
        if (json == null || !json.has("authorName")) {
            return badRequest("Missing parameter [authorName]");
        }
        String authorName = json.get("authorName").asText();
        // Create a new Document to insert into MongoDB
        Document doc = new Document("authorName", authorName);
        if (json.has("email")) {
            doc.append("email", json.get("email").asText());
        }
        if (json.has("id")) {
            doc.append("id", json.get("id").asInt());
        }
        mongoService.createAuthor(doc);
        return ok("Author created successfully");
    }


    //to show details of each author
    public Result show() {
        JsonNode res = mongoService.getAllAuthors();
        return ok(res);
    }


    //to delete an author
    public Result delete(Integer id) {
        JsonNode res = mongoService.deleteAuthor(id);
        return ok(res);
    }


    //to edit an author
    public Result edit(String name) {
        // Bind the form from the request
        Form<Author> authorForm = formFactory.form(Author.class).bindFromRequest();
        if (authorForm.hasErrors()) {
            return badRequest("Invalid data");
        }
        Author updatedAuthor = authorForm.get();
        // Fetch the existing document by ID
        Document doc = authors.find(eq("authorName", name)).first();
        if (doc == null) {
            return notFound("Author Not Found");
        }
        Document updatedDoc = new Document();
        if (updatedAuthor.id != null) {
            updatedDoc.append("id", updatedAuthor.id);
        } else {
            updatedDoc.append("id", doc.getString("id"));
        }
        if (updatedAuthor.email != null) {
            updatedDoc.append("email", updatedAuthor.email);
        } else {
            updatedDoc.append("email", doc.getString("email"));
        }
        // Update the document in the collection
        mongoService.updateAuthor(name, updatedDoc);
        return ok("Author updated successfully");
    }


    //to get list of books written by specific author
    public CompletionStage<Result> getAuthorsBooks(String author) {
        return WsService.getAuthorsBooks(author);
    }

    //to get details of specific author
    public Result getAuthorDetails(String name) {
        JsonNode res = mongoService.getAuthor(name);
        if (res == null) {
            return notFound("Author Not Found");
        } else {
            return ok(res);
        }
    }


    //to get details of specific publisher
    public CompletionStage<Result> getPublisherDetails() {
        JsonNode requestBody = request().body().asJson();
        if (requestBody == null || !requestBody.has("name")) {
            return CompletableFuture.completedFuture(badRequest("Publisher name not found in request body"));
        }
        return WsService.getPublisherDetails(requestBody);
    }

}















/*

public CompletionStage<Result> getPublisherDetails() {
        JsonNode requestBody = request().body().asJson();
        if (requestBody == null || !requestBody.has("name")) {
            return CompletableFuture.completedFuture(badRequest("Publisher name not found in request body"));
        }
        String publisherName = requestBody.get("name").asText();
        // Fetch details of the publisher from database or any other source
        JsonNode res=mongoService.getPublisher(publisherName);
        if (res== null) {
            return CompletableFuture.completedFuture(notFound("Publisher not found"));
        }
        // Convert Document to JSON and return details of the publisher in the response
        return CompletableFuture.completedFuture(ok(res));

    }

To create Author using form data
// Bind the request data to the Author form
        Form<Author> authorForm = formFactory.form(Author.class).bindFromRequest();
        // Check for validation errors
        if (authorForm.hasErrors()) {
            return badRequest("Invalid data");
        }
        // Get the Author object from the form
        Author author = authorForm.get();
        // Create a new Document to insert into MongoDB
        Document doc = new Document("id", author.id)
                .append("authorName", author.authorName)
                .append("email",author.email);
        // Insert the document into the collection
        collection.insertOne(doc);
        // Return a success response
        return ok("Author created successfully");



        public CompletionStage<Result> getPublisherDetails() {
        JsonNode requestBody = request().body().asJson();
        if (requestBody == null || !requestBody.has("name")) {
            return CompletableFuture.completedFuture(badRequest("Publisher name not found in request body"));
        }

        // Fetch additional details using WsService if needed
        return WsService.getPublisherDetails(requestBody).thenApply(response -> {
            if (response.has("error")) {
                return badRequest(response.get("error").asText());
            }
            return ok(response);
        }).exceptionally(ex -> {
            ex.printStackTrace();
            return internalServerError("An error occurred: " + ex.getMessage());
        });
    }
*/