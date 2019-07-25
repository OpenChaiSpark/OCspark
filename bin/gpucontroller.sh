java -Xmx2048m -Dlogger.level=2 -classpath $GITDIR/tf/target/classes:$GITDIR/tf/libs/* -Djava.net.preferIPv4Stack=true com.pointr.tensorflow.DirectSubmitter /shared/conf/submitter.yml
