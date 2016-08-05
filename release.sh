#!/usr/bin/env bash

export PW=$1

echo $1

mvn clean package -Dmaven.test.skip=true gpg:sign -Dgpg.passphrase=$PW
cd target
rm *-jar-with*
rm -r */
jar -cf bundle.jar *
cd ..

gradle release