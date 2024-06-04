package services;

import play.Configuration;
import com.fasterxml.jackson.databind.JsonNode;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.badRequest;
import static play.mvc.Results.notFound;
import static play.mvc.Results.ok;


public class WsService {

    private final WSClient wsClient;
    private final MongoService mongoService;
    private final Configuration conf;


    @Inject
    public WsService(WSClient wsClient,MongoService mongoService,Configuration conf) {
        this.wsClient = wsClient;
        this.mongoService=mongoService;
        this.conf=conf;
    }


    //to return the details of specific publisher
    public CompletionStage<Result> getPublisherDetails(JsonNode requestBody) {
        return CompletableFuture.supplyAsync(() -> {
            if (requestBody == null || !requestBody.has("name")) {
                return badRequest("Publisher name not found in request body");
            }

            String publisherName = requestBody.get("name").asText();
            JsonNode res=mongoService.getPublisher(publisherName);
            if (res== null) {
                return notFound("Publisher not found");
            }
            return ok(res);
        });
    }


    //to return the list of books written by specific author
    public CompletionStage<Result> getAuthorsBooks(String author) {
        String booksServiceUrl = conf.getString("booksServiceUrl")+author;

        return wsClient.url(booksServiceUrl).get().thenApply(response -> {
            if (response.getStatus() == 200) {
                JsonNode responseBody = response.asJson(); // Parse response body as JSON
                List<String> books = new ArrayList<>();
                for (JsonNode bookNode : responseBody) {
                    String title = bookNode.get("name").asText();
                    books.add(title);
                }
                return ok(Json.toJson(books));
            } else {
                return badRequest("Failed to fetch books from books service");
            }
        });
    }
}
