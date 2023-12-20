package dev.potat.semantica.common.keywords;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import com.mongodb.client.result.InsertOneResult;
import com.mongodb.client.result.UpdateResult;
import dev.potat.semantica.common.dataclasses.SearchResult;
import org.bson.Document;
import lombok.Synchronized;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.mongodb.client.model.Aggregates.*;
import static com.mongodb.client.model.Filters.*;

public class MongoWrapper {

    private static MongoWrapper instance;
    private final MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> linksCollection;

    private MongoWrapper(String uri) {
        mongoClient = MongoClients.create(uri);
        database = mongoClient.getDatabase("semantica_db");
        linksCollection = database.getCollection("links");
    }

    @Synchronized
    public static MongoWrapper getInstance(String uri) {
        if (instance == null) {
            instance = new MongoWrapper(uri);
        }
        return instance;
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    // Methods to work with linksCollection

    public ArrayList<String> filterExistingLinks(ArrayList<String> links) {
        ArrayList<String> existingLinks = new ArrayList<>();
        linksCollection.aggregate(
                Arrays.asList(
                        match(Filters.in("url", links)),
                        Aggregates.project(Projections.include("url"))
                )
        ).forEach(document -> existingLinks.add(document.getString("url")));
        links.removeAll(existingLinks);
        return links;
    }

    public void saveLinkToDB(String url) {
        try {
            // Inserts a sample document describing a movie into the collection
            InsertOneResult result = linksCollection.insertOne(new Document()
                    .append("_id", new ObjectId())
                    .append("url", url)
                    .append("ready", false)
            );

            // Prints the ID of the inserted document
            System.out.println("Success! Inserted document id: " + result.getInsertedId());

            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }

    public boolean checkLinkExists(String url) {
        try {
            // Inserts a sample document describing a movie into the collection
            Document doc = linksCollection.find(eq("url", url))
//                    .projection(projectionFields)
//                    .sort(Sorts.descending("imdb.rating"))
                    .first();
            return (doc != null);
            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Error: " + me);
        }
        return false;
    }

    public void saveKeywordsForURL(String url, Keywords keywords) {
        Document query = new Document().append("url", url);
        Bson updates = Updates.combine(
                Updates.set("keywords", keywords.getStrKeywords()),
                Updates.set("weights", keywords.getKeywordsWeights())
        );
        UpdateOptions options = new UpdateOptions().upsert(false);
        try {
            // Updates the first document that has a "title" value of "Cool Runnings 2"
            UpdateResult result = linksCollection.updateOne(query, updates, options);
            // Prints the number of updated documents and the upserted document ID, if an upsert was performed
            System.out.println("Modified document count: " + result.getModifiedCount());
//            System.out.println("Upserted id: " + result.getUpsertedId());

            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

    public void setReady(String url) {
        Document query = new Document().append("url", url);
        Bson updates = Updates.set("ready", Boolean.TRUE);
        UpdateOptions options = new UpdateOptions().upsert(false);
        try {
            UpdateResult result = linksCollection.updateOne(query, updates, options);
            System.out.println("Modified document count: " + result.getModifiedCount());
            System.out.println("Upserted id: " + result.getUpsertedId());

            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Unable to update due to an error: " + me);
        }
    }

    public List<SearchResult> searchByKeywords(List<String> keywords, List<Float> weights) {
        List<SearchResult> results = new ArrayList<>();

        linksCollection.aggregate(
                Arrays.asList(
                        new Document("$addFields",
                                new Document("matchedTags",
                                        new Document("$setIntersection", Arrays.asList("$keywords", keywords)))),
                        new Document("$match",
                                new Document("matchedTags",
                                        new Document("$ne", Arrays.asList()))),
                        new Document("$addFields",
                                new Document("weightedSum",
                                        new Document("$reduce",
                                                new Document("input", "$matchedTags")
                                                        .append("initialValue", 0L)
                                                        .append("in",
                                                                new Document("$add", Arrays.asList("$$value",
                                                                        new Document("$pow", Arrays.asList(new Document("$arrayElemAt", Arrays.asList("$weights",
//                                                                        new Document("$multiply", Arrays.asList(new Document("$arrayElemAt", Arrays.asList("$weights",
                                                                                        new Document("$indexOfArray", Arrays.asList("$keywords", "$$this")))),
                                                                                new Document("$arrayElemAt", Arrays.asList(weights,
                                                                                        new Document("$indexOfArray", Arrays.asList(keywords, "$$this")))))))))))),
                        new Document("$sort",
                                new Document("weightedSum", -1L)),
                        new Document("$limit", 20L))

        // process results
        ).forEach(doc -> results.add(
                SearchResult.builder()
                        .url(doc.getString("url"))
                        .score(doc.getDouble("weightedSum").floatValue())
                        .build()
        ));
        return results;
    }
}
