#!/bin/bash

export GITDIR=/git/OCspark/

# if `mvn package install` already done you can comment out next line
pushd $GITDIR && mvn package install; popd

echo "[1] Building C header files for java native methods (PcieDMA[Client|Server]) .."
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
javah  -v -stubs -d $GITDIR/tfdma/src/main/cpp/includes/ -cp $(cat cp.txt):$GITDIR/tfdma/src/main/java org.openchai.tensorflow.api.PcieDMAClient

echo "[2a] Workaround for -I not working on linux: copy the linux/ include files to its parent dir.."
cp -p $JAVA_HOME/include/linux/*.h $JAVA_HOME/include

echo "[2] Compiling the C files PcieDMA[Client|Server].c .."

if [[ $(uname) == 'Linux' ]] ; then export OSOPTS=' -I"'"$JAVA_HOME/include"'" -I"'"$JAVA_HOME/include/linux"'" -odmaserver.so';  else export OSOPTS='-dynamiclib  -odmaserver.dylib -I"'"$JAVA_HOME/include/darwin"'" -I"'"$JAVA_HOME/include"'" '; fi

for f in org_openchai_tensorflow_api_PcieDMAClient.c org_openchai_tensorflow_api_PcieDMAServer.c; do  pushd $GITDIR/tfdma/src/main/cpp/ && gcc -v -shared $OSOPTS  -fpic $GITDIR/tfdma/src/main/cpp/$f; popd; done
echo "**Entry points for dylibs** "
find . -name \*.dylib | xargs nm -gU | awk '{print $3}'

echo "[3] Compiling java sources and building release jar.."
mvn package