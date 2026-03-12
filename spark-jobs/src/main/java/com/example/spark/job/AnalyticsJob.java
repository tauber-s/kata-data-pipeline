package com.example.spark.job;

import com.example.spark.soap.SoapClient;
import org.apache.spark.sql.*;
import java.util.Properties;

public class AnalyticsJob {
    private static final int MAX_RETRIES = 10;
    private static final int RETRY_DELAY_MS = 5000;

    public static void run() {
        SparkSession spark = SparkSession.builder()
                .appName("Sales Analytics Job")
                .master("local[*]")
                .getOrCreate();

        String user = System.getenv("POSTGRES_USER");
        String pass = System.getenv("POSTGRES_PASSWORD");
        String url = System.getenv("POSTGRES_URL");

        Properties props = new Properties();
        props.put("user", user);
        props.put("password", pass);

        waitForPostgres(spark, url, props);
        processFromDatabase(spark, url, props);
        processFromCsv(spark, url, props);
        processFromSoap(spark, url, props);

        spark.stop();
    }

    private static void waitForPostgres(SparkSession spark, String url, Properties props) {
        boolean connected = false;
        int attempt = 0;

        while (!connected && attempt < MAX_RETRIES) {
            try {
                spark.read().jdbc(url, "sales", props).limit(1);
                connected = true;
                System.out.println("Connection to PostgreSQL established!");
            } catch (Exception e) {
                attempt++;
                System.out.println("Postgres unavailable, retrying... (attempt " + attempt + ")");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!connected) {
            System.out.println("Unable to connect to PostgreSQL  after " + MAX_RETRIES + " attempts");
            spark.stop();
            System.exit(1);
        }
    }

    private static void processFromDatabase(SparkSession spark, String url, Properties props) {
        System.out.println("Processing data from DATABASE");

        Dataset<Row> sales = spark.read().jdbc(url, "sales", props);
        Dataset<Row> products = spark.read().jdbc(url, "product", props);
        Dataset<Row> salesPersons = spark.read().jdbc(url, "sales_person", props);

        runAnalytics(sales, products, salesPersons, url, props, "DB");
    }

    private static void processFromCsv(SparkSession spark, String url, Properties props) {
        System.out.println("Processing data from CSV");

        Dataset<Row> sales = spark.read()
                .option("header", true)
                .option("inferSchema", true)
                .csv("/data/sales.csv");

        Dataset<Row> products = spark.read()
                .option("header", true)
                .option("inferSchema", true)
                .csv("/data/products.csv");

        Dataset<Row> salesPersons = spark.read()
                .option("header", true)
                .option("inferSchema", true)
                .csv("/data/sales_person.csv");

        runAnalytics(sales, products, salesPersons, url, props, "csv");
    }

    private static void processFromSoap(SparkSession spark, String url, Properties props) {
        System.out.println("Processing data from SOAP");

        String soapBase = "http://soap-mock:8080";
        String productsXml = SoapClient.call(soapBase + "/products");
        String salesXml = SoapClient.call(soapBase + "/sales");
        String salesPersonsXml = SoapClient.call(soapBase + "/sales-person");

        Dataset<Row> products = spark.read()
                .option("rowTag", "product")
                .xml(spark.createDataset(
                        java.util.List.of(productsXml),
                        Encoders.STRING()
                ));

        Dataset<Row> sales = spark.read()
                .option("rowTag", "sale")
                .xml(spark.createDataset(
                        java.util.List.of(salesXml),
                        Encoders.STRING()
                ));

        Dataset<Row> salesPersons = spark.read()
                .option("rowTag", "salesPerson")
                .xml(spark.createDataset(
                        java.util.List.of(salesPersonsXml),
                        Encoders.STRING()
                ));

        runAnalytics(sales, products, salesPersons, url, props, "soap");
    }

    private static void runAnalytics(
            Dataset<Row> sales,
            Dataset<Row> products,
            Dataset<Row> salesPersons,
            String url,
            Properties props,
            String source
    ) {
        Dataset<Row> topSalesPerCity = sales
                .join(functions.broadcast(products),
                        sales.col("product_id").equalTo(products.col("id")))
                .join(functions.broadcast(salesPersons),
                        sales.col("sales_person_id").equalTo(salesPersons.col("id")))
                .groupBy(
                        salesPersons.col("city"),
                        products.col("id").alias("product_id")
                )
                .agg(functions.sum(products.col("price")).alias("total_sales"));

        topSalesPerCity.write()
                .mode(SaveMode.Overwrite)
                .jdbc(url, "top_sales_per_city", props);

        Dataset<Row> topSalesmanCountry = sales
                .join(functions.broadcast(products),
                        sales.col("product_id").equalTo(products.col("id")))
                .join(functions.broadcast(salesPersons),
                        sales.col("sales_person_id").equalTo(salesPersons.col("id")))
                .groupBy(
                        salesPersons.col("country"),
                        salesPersons.col("id").alias("sales_person_id")
                )
                .agg(functions.sum(products.col("price")).alias("total_sales"));

        topSalesmanCountry.write()
                .mode(SaveMode.Overwrite)
                .jdbc(url, "top_salesman_country", props);

        System.out.println("Analytics completed for source " + source);
    }
}