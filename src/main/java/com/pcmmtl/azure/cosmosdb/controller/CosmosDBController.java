package com.pcmmtl.azure.cosmosdb.controller;


import com.azure.cosmos.*;
import com.azure.cosmos.models.*;
import com.azure.cosmos.util.CosmosPagedIterable;
import com.pcmmtl.azure.cosmosdb.domain.Payment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.UUID;

@RestController
public class CosmosDBController implements InitializingBean {

    @Value("${cosmosdb.host}")
    private String host;

    @Value("${cosmosdb.masterkey}")
    private String masterKey;

    @Value("${cosmosdb.dbname}")
    private String dbName;

    @Value("${cosmosdb.containername}")
    private String containerName;

    private CosmosDatabase database;

    private CosmosContainer container;

    private CosmosClient client;

    @Override
    public void afterPropertiesSet() throws Exception {
        //  Create sync client
        client = new CosmosClientBuilder()
                .endpoint(host)
                .key(masterKey)
                .userAgentSuffix("pcmmtl-java-app")
                .consistencyLevel(ConsistencyLevel.EVENTUAL)
                .buildClient();

        database = client.getDatabase(dbName);
        container = database.getContainer(containerName);
    }

    @GetMapping("/")
    public String info() {
        return "This is a CosmosDB app sample";
    }

    @GetMapping("/createdb")
    private String createDatabaseIfNotExists() {
        CosmosDatabaseResponse databaseResponse = client.createDatabaseIfNotExists(dbName);
        database = client.getDatabase(databaseResponse.getProperties().getId());
        return "Done";
    }

    @GetMapping("/createcontainer")
    private String createContainerIfNotExists() {
        CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, "/partitionKey");
        CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties);
        container = database.getContainer(containerResponse.getProperties().getId());
        return "Done";
    }

    @GetMapping("/createpayments")
    private String createPayments() {

        String id = UUID.randomUUID().toString();
        container.createItem(Payment.builder().id(id).amount(1000d).currency("USD").bic("CMNLUS41").build(), new CosmosItemRequestOptions());

        id = UUID.randomUUID().toString();
        container.createItem(Payment.builder().id(id).amount(2000d).currency("CAD").bic("CPPCUS31").build(), new CosmosItemRequestOptions());

        id = UUID.randomUUID().toString();
        container.createItem(Payment.builder().id(id).amount(3000d).currency("EUR").bic("PMFAUS66").build(), new CosmosItemRequestOptions());

        return "Done";
    }

    @GetMapping("/all")
    public String getAll() {

        // Set some common query options
        int preferredPageSize = 10;
        CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();
        //  Set populate query metrics to get metrics around query executions
        queryOptions.setQueryMetricsEnabled(true);

        CosmosPagedIterable<Payment> familiesPagedIterable = container.queryItems(
                "SELECT * FROM Payment ", queryOptions, Payment.class);

        StringBuilder results = new StringBuilder();
        familiesPagedIterable.iterableByPage(preferredPageSize).forEach(e -> {
            results.append(new ArrayList<>(e.getResults()));
         });

        return results.toString();
    }

}
