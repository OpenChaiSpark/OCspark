java -Dlogger.level=2 -classpath $GITDIR/tf/target/classes:$GITDIR/tf/libs/* -Djava.net.preferIPv4Stack=true com.pointr.tensorflow.TfSubmitter /shared/conf/submitter.yml
