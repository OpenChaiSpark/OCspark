#!/usr/bin/env bash
#export slaves=$(cat /shared/gpu-slaves.txt | awk '{print $2}' | awk -F':' '{print $1}')
export slaves="tx1 tx2 tx3 tx4 tx5"
publish() {
  for s in $slaves; do echo $s; scp $GITDIR/tf/target/tf-1.0.0.jar $s:/shared ; done
  for s in $slaves; do echo $s; scp $GITDIR/bin/runtfserver.sh $s:/shared ; done
  for s in $slaves; do echo $s; ssh $s chmod +x /shared/runtfserver.sh; done
}

sshall() { for s in $slaves; do echo $s; ssh $s "$@"; done ; }

scpall() { for s in $slaves; do scp $1 $s:$2; done ; }

stoptf() { sshall "kill -9 \$(ps -ef |  grep tf-1.0.0.jar | grep -v grep | awk '{print \$2}')" ; }
showtf() { sshall "ps -ef | grep tf-1.0.0.jar | grep -v grep | awk '{print $2}'" ; }
starttf() { ssh $1 "nohup /shared/runtfserver.sh localhost $2 > ~/tf.$2.out 2>&1 &"; }
starttfs() {
  stoptf
  showtf
  ssh tx1 'nohup /shared/runtfserver.sh localhost 61230 > ~/tf.out 2>&1 &'
  ssh tx2 'nohup /shared/runtfserver.sh localhost 61240 > ~/tf.out 2>&1 &'
  ssh tx3 'nohup /shared/runtfserver.sh localhost 61250 > ~/tf.out 2>&1 &'
  ssh tx4 'nohup /shared/runtfserver.sh localhost 61260 > ~/tf.out 2>&1 &'
  sleep 3
  showtf

 }
killtf() { ssh $1 "kill -9 \$(ps -ef | grep -v grep | grep $2 | awk '{print \$2}')" ; }

mkTmpDirs() { sshall "echo \$(cat ~/pwd.txt | head -n 1) | sudo -S mkdir -p /data/tmp/tf2 " ; }


runtf() { cd $GITDIR; bin/runtfsubmitter.sh ; }
cd $GITDIR
cleanimg() { rm /data/input/app1/processing/* && rm -rf /data/input/app1/completed/* && rm /data/input/app2/processing/* && rm /data/input/app2/completed/* ; }
nukeimg() { rm -rf /data/input/* &&  rm -rf /data/output/* && mkdir -p /data/input/app1 && mkdir -p /data/input/app2 ; }
pubimg() { cp -Rp $1/app1/* /data/input/app1/ ; cp -Rp $1/app2/* /data/input/app2/ ; }
pubimg1() { pubimg /data/input.sav/scenery ; }
pubimg2() { pubimg /data/input.sav/malls ; }
pubimg3() { pubimg /data/input.sav/cities ; }
summary() { find /data/input -type f | /bin/grep -v " -> data/input/scenery/" | xargs -r ls -lrta ; find /data/output -type f | xargs -r ls -lrta ; }
alias cp2='cp -p'
localtf() {
     nohup /shared/runtfserver.sh localhost 61260 > ~/tf.out 2>&1 &
 }