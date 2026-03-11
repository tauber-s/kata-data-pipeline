package com.example.spark.job;

import org.apache.spark.sql.*;
import java.util.Properties;

import static org.apache.spark.sql.functions.*;

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

        String csvPath = System.getenv().getOrDefault("CSV_PATH", "/data");

        Properties props = new Properties();
        props.put("user", user);
        props.put("password", pass);

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
            System.out.println("Unable to connect to PostgreSQL.");
            spark.stop();
            System.exit(1);
        }

        Dataset<Row> salesDB = spark.read().jdbc(url, "sales", props);
        Dataset<Row> productsDB = spark.read().jdbc(url, "product", props);
        Dataset<Row> salesPersonsDB = spark.read().jdbc(url, "sales_person", props);

        Dataset<Row> salesCSV = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(csvPath + "/sales.csv");

        Dataset<Row> productsCSV = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(csvPath + "/products.csv");

        Dataset<Row> salesPersonsCSV = spark.read()
                .option("header", "true")
                .option("inferSchema", "true")
                .csv(csvPath + "/sales_person.csv");

        Dataset<Row> sales = salesDB.unionByName(salesCSV);
        Dataset<Row> products = productsDB.unionByName(productsCSV);
        Dataset<Row> salesPersons = salesPersonsDB.unionByName(salesPersonsCSV);

        Dataset<Row> topSalesPerCity = sales
                .join(products, sales.col("product_id").equalTo(products.col("id")))
                .groupBy("city", "product_id")
                .agg(sum("price").alias("total_sales"));

        topSalesPerCity.write()
                .mode(SaveMode.Overwrite)
                .jdbc(url, "top_sales_per_city", props);

        Dataset<Row> topSalesmanCountry = sales
                .join(salesPersons, sales.col("sales_person_id").equalTo(salesPersons.col("id")))
                .groupBy("sales_person_id")
                .agg(sum("price").alias("total_sales"));

        topSalesmanCountry.write()
                .mode(SaveMode.Overwrite)
                .jdbc(url, "top_salesman_country", props);

        spark.stop();
    }
}