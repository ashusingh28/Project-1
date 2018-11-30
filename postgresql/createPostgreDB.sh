#! /bin/bash
echo "creating db named ... ashu_jesse_DB"
createdb -h localhost -p $PGPORT "ashu_jesse_DB"
pg_ctl status

echo "Copying csv files ... "
sleep 1
cp ../data/*.csv /tmp/ashu_jesse/myDB/data/.

echo "Initializing tables .. "
sleep 1
psql -h localhost -p $PGPORT "ashu_jesse_DB" < ../sql/create.sql
