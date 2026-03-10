docker compose up -d

docker ps

docker exec -it sales_postgres psql -U postgres -d sales_db

python3 -m venv .venv  
source .venv/bin/activate 
python3 ingestion/tmp_populate_db.py