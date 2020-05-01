This setup documentation was tested for Ubuntu 18.04.

# Update Updater
 - `apt-get update`

# Install UFW
For more details see https://hostadvice.com/how-to/how-to-configure-firewall-with-ufw-on-ubuntu-18/
 - `apt-get install ufw`
 - `ufw allow 22/tcp`
 - `ufw allow 3000/tcp`
 - `ufw enable`
 - `ufw status verbose`

# Install Java 11
 - `apt install openjdk-11-jdk`

# Intall and setup PostgresSQL
 - `apt-get install postgresql-10` 
 - `sudo su postgres`
 - `plsq`, then run following commands
	- `CREATE DATABASE metabase;`
	- `CREATE USER metabase WITH password 'mb_01234';`
	- `GRANT CONNECT ON DATABASE metabase TO metabase;`
	- `GRANT USAGE ON SCHEMA public TO metabase;`
	- `GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO metabase;`
	- `GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO metabase;`
	- `\q` to exit the command prompt
 - Documentation psql: https://www.postgresql.org/docs/10/app-psql.html
 - grant privileges: https://stackoverflow.com/questions/22483555/give-all-the-permissions-to-a-user-on-a-db

# Install docker
For more details see https://docs.docker.com/engine/install/ubuntu/
 - Install docker registry
    - `apt-get update`
    - `apt-get install apt-transport-https ca-certificates curl gnupg-agent software-properties-common`
    - `curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo apt-key add -`
    - `apt-key fingerprint 0EBFCD88`
    - `add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"`
 - Install docker
    - `apt-get update`
    - `apt-get install docker-ce docker-ce-cli containerd.io`
 - Test
    - `docker run hello-world`
    - `docker ps -a`
    - `docker rm <pid>` 

# Running Metabase on Docker
For more details https://www.metabase.com/docs/latest/operations-guide/running-metabase-on-docker.html.
Metabase is executed inside the following docker container loaded with the following command
```
    docker run -d  \
        --network host \
        -e "MB_DB_TYPE=postgres" \
        -e "MB_DB_DBNAME=metabase" \
        -e "MB_DB_PORT=5432" \
        -e "MB_DB_USER=metabase" \
        -e "MB_DB_PASS=mb_01234" \
        -e "MB_DB_HOST=127.0.01" \
        -e "JAVA_TIMEZONE=CET" \
        --name metabase metabase/metabase
```

# Migrating an existing H2 DB into this new system
For more details see https://www.metabase.com/docs/latest/operations-guide/migrating-from-h2.html.
  - Copy the H2 file to the server, e.g. `scp ...`
  -
    ```
    docker run --name db-migration 
       -v /tmp/:/tmp
       --network host 
       -e "MB_DB_TYPE=postgres" 
       -e "MB_DB_PORT=5432" 
       -e "MB_DB_USER=metabase" 
       -e "MB_DB_PASS=mb_01234" 
       -e "MB_DB_DBNAME=metabase" 
       -e "MB_DB_HOST=127.0.0.1" 
       -e "MB_DB_FILE=/tmp/" 
       metabase/metabase load-from-h2
    ```

# Install nginx
 - `apt-get update`
 - `apt-get install nginx`
 - Configure Metabase in nginx
    - `vi /etc/nginx/sites-available/metabase.conf`
	- ``` 
      server {
         listen [::]:80;
         listen 80;
         server_name corona-statistics.mmnze.de;
         location / {
            proxy_set_header X-Forwarded-Host $host;
            proxy_set_header X-Forwarded-Server $host;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_pass http://127.0.0.1:3000;
            client_max_body_size 100M;
         }
      }
      
      server {
         listen 443 ssl http2;
         listen [::]:443 ssl http2;
         server_name corona-statistics.mmnze.de;
         server_tokens off;

         ssl on;
         ssl_certificate /etc/letsencrypt/live/corona-statistics.mmnze.de/fullchain.pem;
         ssl_certificate_key /etc/letsencrypt/live/corona-statistics.mmnze.de/privkey.pem;

         ssl_buffer_size 8k;
         ssl_protocols TLSv1.2 TLSv1.1 TLSv1;
         ssl_prefer_server_ciphers on;

         ssl_ciphers ECDH+AESGCM:ECDH+AES256:ECDH+AES128:DH+3DES:!ADH:!AECDH:!MD5;
         ssl_ecdh_curve secp384r1;
         ssl_session_tickets off;

         # OCSP stapling
         ssl_stapling on;
         ssl_stapling_verify on;
         resolver 8.8.8.8 8.8.4.4;

         location / {
            proxy_pass http://127.0.0.1:3000;
            proxy_connect_timeout 600;
            proxy_send_timeout 600;
            proxy_read_timeout 600;
            send_timeout 600;
         }      
	  ```
    - `systemctl reload nginx`

# Installation of SSL certificates (using certbot and Let's Encrypt)
For more details https://www.digitalocean.com/community/tutorials/how-to-secure-nginx-with-let-s-encrypt-on-ubuntu-18-04
 - `add-apt-repository ppa:certbot/certbot`
 - `apt-get install python-certbot-nginx`
 - `ufw allow 'Nginx Full'`
 - `certbot --nginx -d corona-statistics.mmnze.de`
    - Provide mail adress
    - Agree (must)
    - Disagree (send data)
    - 2 (redirect to HTTPS)
 - Test certbot
    - `certbot renew --dry-run`
 - Certbot writes it's data into `/etc/nginx/sites-available/default`. Last lines of the config file should be deleted.
