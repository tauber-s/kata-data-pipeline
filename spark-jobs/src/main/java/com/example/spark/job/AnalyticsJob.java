package com.example.spark.job;

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

        String user = System.getenv().get("POSTGRES_USER");
        String pass = System.getenv().get("POSTGRES_PASSWORD");
        String url = System.getenv().get("POSTGRES_URL");

        Properties props = new Properties();
        props.put("user", user);
        props.put("password", pass);

        boolean connected = false;
        int attempt = 0;

        while (!connected && attempt < MAX_RETRIES) {
            try {
                Dataset<Row> test = spark.read().jdbc(url, "sales", props).limit(1);
                connected = true;
                System.out.println("Connection to PostgreSQL established!");
            } catch (Exception e) {
                attempt++;
                System.out.println("Postgres is unavailable, retrying in 5 seconds... (attempt " + attempt + ")");
                try {
                    Thread.sleep(RETRY_DELAY_MS);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }
        }

        if (!connected) {
            System.out.println("Unable to connect to PostgreSQL after " + MAX_RETRIES + " attempts.");
            spark.stop();
            System.exit(1);
        }

        Dataset<Row> sales = spark.read().jdbc(url, "sales", props);
        Dataset<Row> products = spark.read().jdbc(url, "product", props);
        Dataset<Row> salesPersons = spark.read().jdbc(url, "sales_person", props);

        Dataset<Row> topSalesPerCity = sales
                .join(products, sales.col("product_id").equalTo(products.col("id")))
                .groupBy("city", "product_id")
                .agg(functions.sum("price").alias("total_sales"));

        topSalesPerCity.write().mode(SaveMode.Overwrite).jdbc(url, "top_sales_per_city", props);

        Dataset<Row> topSalesmanCountry = sales
                .join(salesPersons, sales.col("sales_person_id").equalTo(salesPersons.col("id")))
                .groupBy("sales_person_id")
                .agg(functions.sum("price").alias("total_sales"));

        topSalesmanCountry.write().mode(SaveMode.Overwrite).jdbc(url, "top_salesman_country", props);

        spark.stop();
    }
}