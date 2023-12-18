package dev.potat.semantica.common;

import com.mongodb.MongoException;
import com.mongodb.client.*;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.result.InsertOneResult;
import org.bson.Document;
import lombok.Synchronized;
import org.bson.types.ObjectId;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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
                        Aggregates.match(Filters.in("url", links)),
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
                    .append("url", url));
            // Prints the ID of the inserted document
            System.out.println("Success! Inserted document id: " + result.getInsertedId());

            // Prints a message if any exceptions occur during the operation
        } catch (MongoException me) {
            System.err.println("Unable to insert due to an error: " + me);
        }
    }
}
