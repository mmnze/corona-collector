# Install UFW
https://hostadvice.com/how-to/how-to-configure-firewall-with-ufw-on-ubuntu-18/

# Install Java 11
 - apt install openjdk-11-jdk

# Intall and setup PostgresSQL
 - apt-get install postgresql-10 
 - plsq, then run following commands
	- CREATE DATABASE metabase;
	- CREATE USER metabase WITH password mb_01234;
	- GRANT CONNECT ON DATABASE my_db TO metabase;
	- GRANT USAGE ON SCHEMA public TO metabase;
	- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO metabase;
	- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO metabase;
 - Documentation psql: https://www.postgresql.org/docs/10/app-psql.html
 - grant privileges: https://stackoverflow.com/questions/22483555/give-all-the-permissions-to-a-user-on-a-db
 
# Install nginx
 - sudo apt update
 - sudo apt install nginx
	
# Installation of SSL certificates (using certbot and Let's Encrypt)
https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-ubuntu-18-04

# Running Metabase on Docker
https://www.metabase.com/docs/latest/operations-guide/running-metabase-on-docker.html
 - Running the docker image from postgresql
 - docker run -d -p 3000:3000 \
	  -e "MB_DB_TYPE=postgres" \
	  -e "MB_DB_DBNAME=metabase" \
	  -e "MB_DB_PORT=5432" \
	  -e "MB_DB_USER=metabase" \
	  -e "MB_DB_PASS=mb_01234" \
	  -e "MB_DB_HOST=h2876148.stratoserver.net" \
	  --name metabase metabase/metabase

## Docker documentation
https://docs.docker.com/engine/reference/commandline/ps/


