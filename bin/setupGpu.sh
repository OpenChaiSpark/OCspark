#!/usr/bin/env bash
alias aptget='sudo apt-get install -y'
sudo wget www.scala-lang.org/files/archive/scala-2.11.8.deb
cat .bashrc
sudo mkdir /git
sudo chown steve:steve /git
sudo mkdir /shared
sudo chmod 777 /shared

sudo add-apt-repository ppa:webupd8team/java
sudo apt-get update
sudo apt-get install oracle-java8-installer
sudo apt-get install oracle-java8-set-default

# ln -s $(pwd) /shared/orajava
#git config --global user.email javadba@gmail.com
#git config --global user.name javadba
#git config --global push.default current
#git config --global commit.default current
#aptget software-properties-common
#cd /git
#gitc https://OpenChaiSpark:simit-****-peynir@github.com/OpenChaiSpark/OCspark
#ln -s /git/OCspark/tf/src/main/resources/apps-config.yml /shared/conf/
#cd tcpclient
#mvn package install
#cd ../tfdma
#mvn package
#./build.arm.sh
#cd /git/OCspark
#mvn package install
#aptget maven
#sudo apt-add-repository universe
#sudo apt-get update
#aptget maven
#cd /git/OCspark/tcpclient
#mvn package install
#cd ..
#gitf
#git pull origin master
#bin/buildtf.sh
#vi bin/buildtf.sh
#export OCDIR=/git/OCspark
#cd $OCDIR/tfdma
#vi build.sh
#./build.sh
#cd ..
#cat bin/buildtf.sh
#cd tfdma
#mvn package install
#cd ..
#cat bin/buildtf.sh
#cd $OCDIR/tcpclient
#mvn package install
#cd $OCDIR/tf
#./build.arm.sh
#mvn package install
#find .. -name build.arm\*
#cd ../tfdma
#./build.arm.sh
#mvn package install
#cd $OCDIR/tcpclient
#mvn package install
#cd $OCDIR/tf
#mvn package install
#cd ..
#cat bin/runtf.sh
#bin/runtf.sh 65254
#ln -s $(pwd) /shared/orajava
#ln -s /git/OCspark/tf/src/main/resources/apps-config.yml /shared/conf/
#mkdir /shared/conf
#ln -s /git/OCspark/tf/src/main/resources/apps-config.yml /shared/conf/
#bin/runtf.sh 65254
scp -Rp tx1:~ubuntu/tensorflow ~
sudo cp -Rp ~/tensorflow ~ubuntu
sudo ln -s ~ubuntu/tensorflow /shared/tensorflow

# TODO: scp the label_image.sh from master as part of publish
scp tx1:/git/OCspark/bin/label_image.sh ~
sudo ln -s ~/label_image.sh    /home/ubuntu/tensorflow/bazel-bin/tensorflow/examples/label_image/label_image.sh

#runtf
sudo ln -s /home/ubuntu/tensorflow/bazel-bin/tensorflow/examples/label_image/label_image /home/ubuntu/tensorflow/bazel-bin/tensorflow/examples/label_image/label_image_bin
aptget screen
sudo mkdir /data
sudo chmod 777 /data

scp tx1:~/inputimages.zip ~
cd /data
unzip ~/inputimages.zip
