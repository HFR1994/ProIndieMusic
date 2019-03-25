#!/usr/bin/env bash

git push origin master
mvn install
docker tag proindiemusic/backend frhec/proindiemusic-backend
docker push frhec/proindiemusic-backend
ibmcloud cf push proindiemusic-backend --docker-image frhec/proindiemusic-backend -i 1 -m 512MB
