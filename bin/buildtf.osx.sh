cd $GITDIR/tfengine
mvn package install
if (( $? != 0 )); then echo 'mvn package tfengine failed' && popd &&  exit 127; fi

cd $GITDIR/tcpclient
mvn package install
if (( $? != 0 )); then echo 'mvn package tcpclient failed' && popd &&  exit 127; fi

cd $GITDIR/tfdma
./build.osx.sh
# if [ "$BUILDOK" != "TRUE" ]; then echo 'tfdma build.osx.sh failed'; exit 1; fi
mvn package install

cd $GITDIR/tf
mvn package install
if (( $? != 0 )); then echo 'mvn package tf failed' && popd &&  exit 127; fi
echo "** DONE **"
