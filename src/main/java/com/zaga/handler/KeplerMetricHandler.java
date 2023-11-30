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

// public List<KeplerMetricDTO> getAllKeplerByDateAndTime(
// LocalDate from,
// LocalDate to,
// int minutesAgo,
// String type,
// List<String> keplerTypeList
// ) {
// Document timeFilter;
// if (from != null && to != null && to.isBefore(from)) {
// LocalDate temp = from;
// from = to;
// to = temp;
// }

// if (from != null && to != null) {
// timeFilter = createCustomDateFilter(from, to);
// } else if (minutesAgo > 0) {
// LocalDateTime currentDateTime = LocalDateTime.now();
// LocalDateTime startOfToday = LocalDate.now().atStartOfDay();

// LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);
// if (fromDateTime.isBefore(startOfToday)) {
// fromDateTime = startOfToday;
// }
// LocalDateTime toDateTime = currentDateTime;

// LocalDateTime DBCallOneStart = LocalDateTime.now();

// System.out.println(
// "------------DB call One startTimestamp------ " + DBCallOneStart
// );

// Bson bsonFilter = Filters.and(
// Filters.gte(
// "date",
// Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())
// ),
// Filters.lt(
// "date",
// Date.from(toDateTime.atZone(ZoneId.systemDefault()).toInstant())
// )
// );

// LocalDateTime DBCallOneEnd = LocalDateTime.now();

// System.out.println(
// "------------DB call One endTimestamp------ " + DBCallOneEnd
// );

// System.out.println(
// "-----------DB call ended Timestamp------ " +
// Duration.between(DBCallOneStart, DBCallOneEnd)
// );

// timeFilter = Document.parse(bsonFilter.toBsonDocument().toJson());

// } else {
// throw new IllegalArgumentException(
// "Either date range or minutesAgo must be provided"
// );
// }

// if (type != null && !type.isEmpty()) {
// timeFilter.append("type", type);
// }

// if (keplerTypeList != null && !keplerTypeList.isEmpty()) {
// timeFilter.append("keplerType", new Document("$in", keplerTypeList));
// }

// LocalDateTime DBCallTwoStart = LocalDateTime.now();

// System.out.println(
// "------------DB call Two startTimestamp------ " + DBCallTwoStart
// );

// PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(timeFilter);

// LocalDateTime DBCallTwoEnd = LocalDateTime.now();

// System.out.println(
// "------------DB call Two endTimestamp------ " + DBCallTwoEnd
// );

// System.out.println(
// "-----------DB call ended Timestamp------ " +
// Duration.between(DBCallTwoStart, DBCallTwoEnd)
// );

// return query.list();
// }

// public List<KeplerMetricDTO> getAllKeplerByDateAndTime(
// LocalDate from,
// LocalDate to,
// int minutesAgo,
// String type,
// List<String> keplerTypeList
// ) {
// LocalDateTime startTime = LocalDateTime.now();

// Bson bsonFilter;

// if (from != null && to != null && to.isBefore(from)) {
// LocalDate temp = from;
// from = to;
// to = temp;
// }

// if (from != null && to != null) {
// bsonFilter = Filters.and(
// Filters.gte("date",
// Date.from(from.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant())),
// Filters.lt("date",
// Date.from(to.plusDays(1).atStartOfDay().atZone(ZoneId.systemDefault()).toInstant()))
// );
// } else if (minutesAgo > 0) {
// LocalDateTime currentDateTime = LocalDateTime.now();
// LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
// LocalDateTime fromDateTime = currentDateTime.minusMinutes(minutesAgo);

// if (fromDateTime.isBefore(startOfToday)) {
// fromDateTime = startOfToday;
// }

// bsonFilter = Filters.and(
// Filters.gte("date",
// Date.from(fromDateTime.atZone(ZoneId.systemDefault()).toInstant())),
// Filters.lt("date",
// Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant()))
// );
// } else {
// throw new IllegalArgumentException("Either date range or minutesAgo must be
// provided");
// }

// if (type != null && !type.isEmpty()) {
// bsonFilter = Filters.and(bsonFilter, Filters.eq("type", type));
// }

// if (keplerTypeList != null && !keplerTypeList.isEmpty()) {
// bsonFilter = Filters.and(bsonFilter, Filters.in("keplerType",
// keplerTypeList));
// }

// LocalDateTime dbCallStart = LocalDateTime.now();
// System.out.println("------------DB call startTimestamp------ " +
// dbCallStart);

// Document documentFilter =
// Document.parse(bsonFilter.toBsonDocument().toJson());

// PanacheQuery<KeplerMetricDTO> query = keplerMetricRepo.find(documentFilter);

// LocalDateTime dbCallEnd = LocalDateTime.now();
// System.out.println("------------DB call endTimestamp------ " + dbCallEnd);
// System.out.println("-----------DB call ended Timestamp------ " +
// Duration.between(dbCallStart, dbCallEnd));

// return query.list();
// }

// private Document createCustomDateFilter(LocalDate from, LocalDate to) {
// Bson bsonFilter = Filters.and(
// Filters.gte("date", from.atStartOfDay()),
// Filters.lt("date", to.plusDays(1).atStartOfDay())
// );

// return Document.parse(bsonFilter.toBsonDocument().toJson());
// }
