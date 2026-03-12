CREATE TABLE product (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    price NUMERIC(10,2),
    category VARCHAR(50)
);

CREATE TABLE sales_person (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100),
    city VARCHAR(100),
    state VARCHAR(100),
    country VARCHAR(100)
);

CREATE TABLE sales (
    id SERIAL PRIMARY KEY,
    date TIMESTAMP,
    product_id INT REFERENCES product(id),
    sales_person_id INT REFERENCES sales_person(id)
);

CREATE TABLE top_sales_per_city (
   city VARCHAR(100),
   product_id INT REFERENCES product(id),
   total_sales NUMERIC
);

CREATE TABLE top_sales_person_country (
   sales_person_id INT REFERENCES sales_person(id)
   total_sales NUMERIC
);