## api
- install java 17
- prepare database
  - install docker
  - run `docker run --name mariadbtest -e MYSQL_ROOT_PASSWORD=mypass -p 3306:3306 -d docker.io/library/mariadb:latest`
  - create database. run `docker exec -it mariadbtest mariadb --user root -pmypass` then `create database deffun;`
- export next env vars: GOOGLE_OAUTH_CLIENT_ID, GOOGLE_OAUTH_CLIENT_SECRET, DATASOURCES_DEFAULT_URL, DATASOURCES_DEFAULT_USERNAME, DATASOURCES_DEFAULT_PASSWORD, DOKKU_KEY_FILE, DOKKU_KEY_FILE_PASSPHRASE, DOKKU_HOST
- run `java -jar my.jar`

## frontend
- install nodejs 16 (!!!) using nvm for example
- run `yarn build` and `yarn start`

## everything
```
app.deffun.io  {
        handle /api* {
                reverse_proxy localhost:8080
        }
        handle /oauth* {
                reverse_proxy localhost:8080
        }
        handle /graphql {
                reverse_proxy localhost:8080
        }
        handle /graphiql {
                reverse_proxy localhost:8080
        }
        handle {
                reverse_proxy localhost:3000
        }
}
```
