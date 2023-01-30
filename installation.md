## reqs
2 gb ram

## api
- install java 17
(for ubuntu)
```shell
sudo apt update
sudo apt install openjdk-17-jdk
```
- prepare database
  - install docker (for ubuntu) - https://docs.docker.com/engine/install/ubuntu/ and https://docs.docker.com/engine/install/linux-postinstall/
  - run `docker run --name mariadbtest -e MYSQL_ROOT_PASSWORD=mypass -p 3306:3306 -d docker.io/library/mariadb:latest`
  - create database. run `docker exec -it mariadbtest mariadb --user root -pmypass` then `create database deffun;`
- export next env vars: GOOGLE_OAUTH_CLIENT_ID, GOOGLE_OAUTH_CLIENT_SECRET, DATASOURCES_DEFAULT_URL, DATASOURCES_DEFAULT_USERNAME, DATASOURCES_DEFAULT_PASSWORD, DOKKU_KEY_FILE, DOKKU_KEY_PASSPHRASE, DOKKU_HOST and APP_DIST_DIR to deploy with frontend
- run `java -jar my.jar`

## frontend
- install nodejs 16 (!!!) using nvm for example
```shell
curl -fsSL https://deb.nodesource.com/setup_16.x | sudo -E bash -
sudo apt install -y nodejs
# then yarn
npm install --global yarn
# and quasar
yarn global add @quasar/cli
```
- run `yarn quasar build`

## proxy
install caddy - https://caddyserver.com/docs/install#debian-ubuntu-raspbian
and create Caddyfile
```
app.deffun.io {
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
                reverse_proxy localhost:9000
        }
}
```
