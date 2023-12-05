package com.zaga.handler;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.zaga.entity.queryentity.kepler.Response.ContainerPowerMetrics;
import com.zaga.entity.queryentity.kepler.Response.KeplerResponseData;
import com.zaga.repo.KeplerMetricRepo;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bson.Document;

@ApplicationScoped
public class KeplerMetricHandler {

    @Inject
    KeplerMetricRepo keplerMetricRepo;

    @Inject
    MongoClient mongoClient;


    public List<KeplerResponseData> getAllKeplerByDateAndTime(
            LocalDate from,
            LocalDate to,
            int minutesAgo,
            String type,
            List<String> keplerTypeList) {
        LocalDateTime startTime = LocalDateTime.now();
        System.out.println("------------DB call startTimestamp------ " + startTime);

        MongoDatabase database = mongoClient.getDatabase("KeplerMetric");
        MongoCollection<Document> collection = database.getCollection("KeplerMetricDTO");


        
        List<KeplerResponseData> result;

        if (from != null && to != null) {
            result = executeAggregationPipeline(collection, from, to, type, keplerTypeList);
        } else if (from != null && minutesAgo > 0) {
            result = executeAnotherLogic(collection, from, minutesAgo, type, keplerTypeList);
        } else {
            System.out.println("Invalid parameters. Provide either 'from' or 'minutesAgo'.");
            result = Collections.emptyList();
        }

        LocalDateTime endTime = LocalDateTime.now();
        System.out.println("------------DB call endTimestamp------ " + endTime);
        System.out.println("-----------DB call ended Timestamp------ " + Duration.between(startTime, endTime));

        return result;
    }

    private List<KeplerResponseData> executeAggregationPipeline(
            MongoCollection<Document> collection,
            LocalDate from,
            LocalDate to,
            String type,
            List<String> keplerTypeList) {
 
        List<Document> pipeline = Arrays.asList(
                new Document("$addFields",
                        new Document("justDate",
                                new Document("$dateToString",
                                        new Document("format", "%m-%d-%Y")
                                                .append("date", "$date")))),
                new Document("$match",
                        new Document("$and", Arrays.asList(
                                new Document("justDate",
                                        new Document("$gte", from.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))
                                                .append("$lte", to.format(DateTimeFormatter.ofPattern("MM-dd-yyyy")))),
                                new Document("$or",
                                        Arrays.asList(new Document("type", type))),
                                new Document("keplerType",
                                        new Document("$in", keplerTypeList)),
                                 new Document("powerConsumption",
                                                new Document("$gt", 0L))))),
                new Document("$group",
                        new Document("_id", "$serviceName")
                                .append("matchedDocuments",
                                        new Document("$push",
                                                new Document("powerConsumption", "$powerConsumption")
                                                        .append("date", "$date")))
                                .append("count",
                                        new Document("$sum", 1L))),
                new Document("$project",
                        new Document("_id", 0L)
                                .append("serviceName", "$_id")
                                .append("matchedDocuments", 1L)
                                .append("count", 1L)));

        AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline);

        List<KeplerResponseData> result = new ArrayList<>();
        for (Document document : aggregationResult) {
            // System.out.println("result-------" + document.toJson());
            result.add(fromDocument(document));
        }

        return result;
    }

    private List<KeplerResponseData> executeAnotherLogic(
            MongoCollection<Document> collection,
            LocalDate from,
            Integer minutesAgo,
            String type,
            List<String> keplerTypeList) {

        List<Document> pipeline = Arrays.asList(new Document("$match",
                new Document("$and", Arrays.asList(new Document("$or", Arrays.asList(new Document("type", type))),
                        new Document("keplerType",
                                new Document("$in", keplerTypeList)),
                        new Document("$expr",
                                new Document("$and", Arrays.asList(new Document("$gte", Arrays.asList("$date",
                                        new Document("$subtract",
                                                Arrays.asList(new java.util.Date(), minutesAgo * 60L * 1000L)))),
                                        new Document("$lte", Arrays.asList("$date",
                                                new java.util.Date()))))),
                                         new Document("powerConsumption",
                                                new Document("$gt", 0L))))),
                new Document("$group",
                        new Document("_id", "$serviceName")
                                .append("matchedDocuments",
                                        new Document("$push",
                                                new Document("powerConsumption", "$powerConsumption")
                                                        .append("date", "$date")))
                                .append("count",
                                        new Document("$sum", 1L))),
                new Document("$project",
                        new Document("_id", 0L)
                                .append("serviceName", "$_id")
                                .append("matchedDocuments", 1L)
                                .append("count", 1L)));

        AggregateIterable<Document> aggregationResult = collection.aggregate(pipeline);

        List<KeplerResponseData> result = new ArrayList<>();
        for (Document document : aggregationResult) {
            // System.out.println("result-------" + document.toJson());
            result.add(fromDocument(document));
        }

        return result;
    }

    public static KeplerResponseData fromDocument(Document document) {
        KeplerResponseData keplerResponse = new KeplerResponseData();
        keplerResponse.setDisplayName(document.getString("serviceName")); // Assuming "serviceName" corresponds to
                                                                          // "displayName"

        List<Document> matchedDocuments = document.getList("matchedDocuments", Document.class);
        List<ContainerPowerMetrics> containerPowerMetricsList = new ArrayList<>();

        for (Document matchedDocument : matchedDocuments) {
            ContainerPowerMetrics containerPowerMetrics = new ContainerPowerMetrics();
            containerPowerMetrics.setConsumptionValue(matchedDocument.getDouble("powerConsumption")); // Use getDouble
                                                                                                      // for Double
            containerPowerMetrics.setCreatedTime(matchedDocument.getDate("date"));
            containerPowerMetricsList.add(containerPowerMetrics);
        }

        keplerResponse.setContainerPowerMetrics(containerPowerMetricsList);
        return keplerResponse;
    }

}

