#!/usr/bin/env bash

export PW=$1
export GPG_TTY=$(tty)

echo $1

mvn clean package -Dmaven.test.skip=true gpg:sign -Dgpg.passphrase=$PW
cd target
rm *-jar-with*
rm *stale-data.txt
rm -r */
jar -cf bundle.jar *
cd ..
