 git checkout origin/master
 mvn clean
 bin/buildtf.arm.sh
 publish
 vi /shared/conf/submitter.yml
 vi /shared/conf/apps-config.yml
 vi /shared/gpu-slaves.txt
 vi bin/startStopServers.sh
 bin/runtfserver.sh
 pubimg2
 bin/gpucontroller.sh
 starttf
 showtf
 pubimg1
 summary
 killtf() { ssh $1 "kill -9 \$(ps -ef | grep -v grep | grep $2 | awk '{print \$2}')" ; }
 killtf txa2 61240
 showtf
 starttf() { ssh $1 "nohup /shared/runtfserver.sh localhost $2 > ~/tf.$2.out 2>&1 &"; }
 starttf txa2 61240
 mkdir -p /shared/data/input.sav
 ssh steve@192.168.1.146
 ssh steve@192.168.1.146
 # scp inputimages.1213.zip steve@192.168.1.146:~
 #ssh steve@192.168.1.146
