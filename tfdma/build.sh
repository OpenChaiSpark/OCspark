#!/bin/bash

echo "[1] Building C header files for java native methods (PcieDMA[Client|Server]) .."
mvn dependency:build-classpath -Dmdep.outputFile=cp.txt
javah  -v -stubs -d /git/OCSpark/tfdma/src/main/cpp/includes/ -cp $(cat cp.txt):/git/OCSpark/tfdma/src/main/java org.openchai.tensorflow.api.PcieDMAClient
#cp=$(mvn dependency:build-classpath)
#javah  -v -stubs -d /git/OCSpark/tfdma/src/main/cpp/includes/ -cp $(cp):/git/OCSpark/tfdma/src/main/java org.openchai.tensorflow.api.PcieDMAClient

echo "[2] Compiling the C files PcieDMA[Client|Server].c .."
pushd /git/OCSpark/tfdma/src/main/cpp/ && gcc -dynamiclib -odmaclient.dylib -shared -v -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin" -fpic /git/OCSpark/tfdma/src/main/cpp/org_openchai_tensorflow_api_PcieDMAClient.c; popd
pushd /git/OCSpark/tfdma/src/main/cpp/ && gcc -dynamiclib -odmaserver.dylib -shared -v -I"$JAVA_HOME/include" -I"$JAVA_HOME/include/darwin" -fpic /git/OCSpark/tfdma/src/main/cpp/org_openchai_tensorflow_api_PcieDMAServer.c; popd
echo "**Entry points for dylibs** "
find . -name \*.dylib | xargs nm -gU | awk '{print $3}'

echo "[3] Compiling java sources and building release jar.."
mvn package 

