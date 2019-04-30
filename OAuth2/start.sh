#!/usr/bin/env bash

git push origin master
mvn install
docker tag proindiemusic/oauth2 frhec/proindiemusic-oauth2
docker push frhec/proindiemusic-oauth2
ibmcloud cf push proindiemusic-oauth --docker-image frhec/proindiemusic-oauth2 -i 1 -m 512MB -u port 9000
