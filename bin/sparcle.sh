#!/bin/bash

#DISTDIR=${HOME}/src
DISTDIR=${HOME}/work/pointr/src
DISTNAME=OCspark
LOGDIR=${HOME}/sparcle-logs
START_SLAVE_COMMAND="/shared/runtfserver.sh localhost 61230"
START_MASTER_COMMAND="java -Xmx2048m -Dlogger.level=2 -classpath ${GITDIR}/tf/target/classes:${GITDIR}/tf/libs/* -Djava.net.preferIPv4Stack=true org.openchai.tensorflow.DirectSubmitter /shared/conf/submitter.yml"
BASE_SLAVE_PORT=61230
DATA_DIR=${HOME}/tmp/ocspark
APP_NAME=test-local
GPU_REGISTRY_PORT=60001
DEBUG=1


## ----------------------------------------------------------------------------
## Implement no-op versions of dangerous commands.
## ----------------------------------------------------------------------------

if [ "${DEBUG}" != "0" ]; then
    START_SLAVE_COMMAND="echo ${START_SLAVE_COMMAND}"
    START_MASTER_COMMAND="echo ${START_MASTER_COMMAND}"
fi

## We assume we don't have to build here because it's the same arch as
## the build server.  However we do install (most of) the build tools
## since we might need them later.  Note: this follows bin/setupXgene.sh.

## Idea: one script.  called as:
##
##   deploy setup slave
##   deploy setup master
##   deploy setup dev
##   deploy start master
##   deploy start slave
##   deploy start all

## Note: apt-get is idempotent so we don't have to check if a package
## is already installed before installing it.

## ----------------------------------------------------------------------------
## Infer the architecture and make sure it's recognized.
## ----------------------------------------------------------------------------

ARCH=$(uname -m)

if [ "${ARCH}" != "x86_64" ] && [ "${ARCH}" != "ARM" ]; then
    abort "Unexpected architecture inferred (expecting 'arch_64')"
fi

OCSPARK=${DISTDIR}/${DISTNAME}

## ----------------------------------------------------------------------------
## Abort with an error message.
## ----------------------------------------------------------------------------

function abort { echo "$1.  Aborting." 1>&2; exit 1; }

## ----------------------------------------------------------------------------
## Apply a function on another host (specified by IP).  Note: we
## hardcode USER in the ssh call because we might be using it in a
## declared function (eg. adopt-host) and don't want the function to
## use the remote value of USER (eg. nvidia).
## ----------------------------------------------------------------------------

function remote-op {
    callback=$1
    remote_host=$2
    user=$3

    [ "${remote_host}" == "" ] && abort "No remote host (ip addresses) supplied"

    if [ "${user}" != "" ]; then
        ssh ${user}@${remote_host}\
            -t "$(declare -f abort ${callback}); USER=${USER}; ${callback}" ||
            abort "remote command failed"
    else
        ssh ${remote_host}\
            -t "$(declare -f abort ${callback}); USER=${USER}; ${callback}" ||
            abort "remote command failed"
    fi
}

## ----------------------------------------------------------------------------
## Apply a function to all hosts in an IP file (can contain multiple
## slaves and/or one (at most) master).
## ----------------------------------------------------------------------------

function remote-cluster-op {
    master_callback=$1
    slave_callback=$2
    ip_file=$3

    [ "${ip_file}" == "" ] && abort "No IP file supplied"
    [ -e ${ip_file} ] || abort "Supplied IP file '${ip_file}' not found"

    got_master="no"

    while read -r line; do
        ip=$(echo $line | cut -f1 -d " ")
        class=$(echo $line | cut -f2 -d " ")

        if [ "${class}" == "slave" ]; then
            [ "${slave_callback}" != "" ] && ${slave_callback} ${ip}
        elif [ "${class}" == "master" ]; then
            [ "got_master" == "yes" ] &&\
                abort "Found more than one master in supplied IP file '${ip_file}'"

            got_master="yes"

            [ "${master_callback}" != "" ] && ${master_callback} ${ip}
        else
            abort "Found unexpected host class '${class}' in supplied IP file '${ip_file}' (expecting 'slave' or 'master')"
        fi
    done < ${ip_file}
}

## ----------------------------------------------------------------------------
## Add a personal user on the specified host.  Uses the 'nvidia'
## (sudo) account to access the host in order to add the new account.
## ----------------------------------------------------------------------------

## Note: possible approaches to eliminating the need for the user
## interaction and multiple password entries:
##
##  See: https://serverfault.com/questions/841831/ssh-ask-password-once-reuse-password-until-timeout-finishes
##
##    sshpass
##    expect
##    ssh -o ControlPath...

## Need to make ${USER} evaluate to mike (ie. im the local shell
## *before* the function is declared and invoced by the remote shell).

function adopt-host {
    echo sudo adduser ${USER} || abort "adduser failed"
    echo sudo usermod -aG sudo ${USER} || abort "usermod failed"
}

function adopt-remote-host { remote-op adopt-host $1 nvidia; }

function authorize-ssh {
    mkdir -p ${HOME}/.ssh || abort "make ssh dir failed"
    cat >> ${HOME}/.ssh/authorized_keys || abort "append ssh keys failed"
}

function authorize-remote-ssh {
    [ -e "${HOME}/.ssh/id_dsa.pub" ] || abort "No DSA public key found"
    [ -e "${HOME}/.ssh/id_rsa.pub" ] || abort "No RSA public key found"

    cat ${HOME}/.ssh/id_dsa.pub ${HOME}/.ssh/id_rsa.pub | remote-op authorize-ssh $1
}

## ----------------------------------------------------------------------------
## Install java.
## ----------------------------------------------------------------------------

function set-up-java {
    sudo add-apt-repository ppa:webupd8team/java || abort "set java repo failed"
    sudo apt-get update || abort "update failed"
    sudo apt-get install -y oracle-java8-installer || abort "install java failed"
    sudo apt-get install -y oracle-java8-set-default || abort "set default failed"
}

function set-up-remote-java { remote-op set-up-java $1; }

## ----------------------------------------------------------------------------
## Install scala.
## ----------------------------------------------------------------------------

function set-up-scala {
    sudo wget www.scala-lang.org/files/archive/scala-2.11.8.deb ||
        abort "fetch scala failed"
    sudo dpkg -i scala-2.11.8.deb || abort "install scala failed"
}

function remote-set-up-scala { remote-op set-up-scala $1; }

## ----------------------------------------------------------------------------
## Install misc. other packages.
## ----------------------------------------------------------------------------

function set-up-misc {
    sudo apt-get install -y screen || abort "install screen failed"
    sudo apt get install -y software-properties-common ||
        abort "install common properties failed"
}

function remote-set-up-misc { remote-op set-up-misc $1; }

## ----------------------------------------------------------------------------
## Install maven
## ----------------------------------------------------------------------------

function set-up-maven {
    sudo apt-add-repository universe || abort "add universe repo failed"
    sudo apt-get update || abort "update failed"
    sudo apt-get install -y maven || abort "install maven failed"
}

function remote-set-up-maven { remote-op set-up-maven $1; }

## ----------------------------------------------------------------------------
## Install yq (a dependency of mkshared.sh).
## ----------------------------------------------------------------------------

function set-up-yq {
    sudo add-apt-repository ppa:rmescandon/yq || abort "add yq repo failed"
    sudo apt update || abort "update failed"
    sudo apt install -y yq || abort "install yq failed"
}

function remote-set-up-yq { remote-op set-up-yq $1; }

## ----------------------------------------------------------------------------
## Create a clone of the sparcle repo.
## ----------------------------------------------------------------------------

function clone {
    (cd ${HOME} && mkdir -p ${DISTDIR}) || abort "mkdir failed"
    (cd ${DISTDIR} && git clone https://github.com/OpenChaiSpark/OCspark.git) ||
        abort "git clone failed"
    (cd ${OCSPARK} && git checkout -b v3 origin/v3) || abort "git branch failed"
}

function remote-clone { remote-op clone $1; }

## ----------------------------------------------------------------------------
## Build.
## ----------------------------------------------------------------------------

## Note: there is apparently a circular dependency: you have to do
## some combination of "./bin/buildtf.arm.sh", "(cd tfdma;
## ./build.arm.sh)" and "mvn package", in some order, in order to get
## "./bin/buildtf.arm.sh" building properly.
##
## Need to add some include paths for gcc:
##
##   For jni.h:     /usr/lib/jvm/java-8-oracle/include
##   For jni_md.h:  /usr/lib/jvm/java-8-oracle/include/linux
##   Other headers: /usr/include
##   Other headers: /usr/include/linux
##   Other headers: /usr/local/include

function build {
    export CPATH=/usr/lib/jvm/java-8-oracle/include:/usr/lib/jvm/java-8-oracle/include/linux:/usr/include:/usr/include/linux:/usr/local/include
    export GITDIR=${OCSPARK}
    (cd ${GITDIR} && mvn install && ./bin/buildtf.arm.sh) ||
        abort "git maven build failed"
    (cd ${GITDIR}/newapp && make) || abort "git make newapp failed"
}

function remote-build { remote-op build $1; }

## ----------------------------------------------------------------------------
## Create a sparcle package (tarball) ready to deploy to remote hosts.
## ----------------------------------------------------------------------------

function package {
    (cd ${DISTDIR} && tar czvf ${HOME}/ocspark-${ARCH}.tgz ${DISTNAME}) ||
        abort "create package failed"
}

function remote-package { remote-op package $1; }

## ----------------------------------------------------------------------------
## Deploy a sparcle package on a remote host.
## ----------------------------------------------------------------------------

function deploy {
    export remote_host=192.168.1.109
    ssh ${remote_host} "ls -d ${OCSPARK}" && abort "Remote sparcle already present"
    scp ${HOME}/ocspark-${ARCH}.tgz ${remote_host}: ||
        abort "sparcle package copy failed"
    ssh ${remote_host} "mkdir -p ${DISTDIR} && cd ${DISTDIR} && tar -xzvf ${HOME}/ocspark-${ARCH}.tgz" ||
        abort "sparcle package unpack failed"
}

## ----------------------------------------------------------------------------
## Start a slave (typically on a GPU).  Note: slaves must be started
## before the master.
## ----------------------------------------------------------------------------

function start-slave {
    TMPFILE=$(mktemp ${LOGDIR}/sparcle-slave.XXXXXX) || abort "mktemp failed"
    /shared/runtfserver.sh localhost 61230 >> ${TMPFILE} 2>&1 &
}

function start-remote-slave { remote-op start-slave $1; }

## ----------------------------------------------------------------------------
## Start a slave on a remote host (typically a GPU), logging to this
## host.  Note: slaves must be started before the master.
## ----------------------------------------------------------------------------

function start-remote-slave-logging-here {
    remote_host=$1

    [ "${remote_host}" == "" ] && abort "No remote host (ip addresses) supplied"

    TMPFILE=$(mktemp ${LOGDIR}/sparcle-slave.XXXXXX) || abort "mktemp failed"
    ssh ${remote_host} "/shared/runtfserver.sh localhost 61230" >> ${TMPFILE} 2>&1 &
}

## ----------------------------------------------------------------------------
## Start slaves on multiple remote hosts, supplied via a file.
## ----------------------------------------------------------------------------

function start-remote-slaves {
    SLAVEFILE=$1

    [ "${SLAVEFILE}" == "" ] && abort "No slaves file (ip addresses) supplied"
    [ -e ${SLAVEFILE} ] || abort "Supplied slave file '${SLAVEFILE}' not found"

    for slave in $(cat ${SLAVEFILE}); do
        start-remote-slave ${slave}
    done
}

## ----------------------------------------------------------------------------
## Stop a slave on a remote host (typically a GPU).
## ----------------------------------------------------------------------------

function stop-slave { pkill -f -n "${START_SLAVE_COMMAND}"; }
function stop-remote-slave { remote-op stop-slave $1; }

## ----------------------------------------------------------------------------
## Stop slaves on multiple remote hosts, supplied via a file.
## ----------------------------------------------------------------------------

function stop-remote-slaves {
    SLAVEFILE=$1

    [ "${SLAVEFILE}" == "" ] && abort "No slaves file (ip addresses) supplied"
    [ -e ${SLAVEFILE} ] || abort "Supplied slave file '${SLAVEFILE}' not found"

    for slave in $(cat ${SLAVEFILE}); do
        stop-remote-slave ${slave}
    done
}
    
## ----------------------------------------------------------------------------
## Start a master (eg. on x-gene or i3).
## ----------------------------------------------------------------------------

function start-master {
    TMPFILE=$(mktemp ${LOGDIR}/sparcle-master.XXXXXX) || exit 1
    export GITDIR=${OCSPARK}
    java -Xmx2048m -Dlogger.level=2 -classpath ${GITDIR}/tf/target/classes:${GITDIR}/tf/libs/* -Djava.net.preferIPv4Stack=true org.openchai.tensorflow.DirectSubmitter /shared/conf/submitter.yml >> ${TMPFILE} 2>&1 &
}

function start-remote-master { remote-op start-master $1; }

## ----------------------------------------------------------------------------
## Start a master on a remote host (eg. on x-gene or i3), logging to
## this host.  Note: slaves must be started before the master.
## ----------------------------------------------------------------------------

function start-remote-master-logging-here {
    remote_host=$1

    [ "${remote_host}" == "" ] && abort "No remote host (ip addresses) supplied"

    TMPFILE=$(mktemp ${LOGDIR}/sparcle-slave.XXXXXX) || exit 1
    ssh ${remote_host} "${START_MASTER_COMMAND}" >> ${TMPFILE} 2>&1 &
}

## ----------------------------------------------------------------------------
## Stop a master on a remote host.
## ----------------------------------------------------------------------------

function stop-master { pkill -f -n "${START_MASTER_COMMAND}"; }
function stop-remote-master { remote-op stop-master $1; }

## ----------------------------------------------------------------------------
## Wrap the remote cluster operation.
## ----------------------------------------------------------------------------

function start-remote-cluster { remote-cluster-op start-remote-master start-remote-slave $@; }
function stop-remote-cluster { remote-cluster-op stop-remote-master start-remote-slave $@; }
function adopt-remote-cluster { remote-cluster-op adopt-remote-host adopt-remote-host $@; }

## ----------------------------------------------------------------------------
## Create a gpu-slaves.txt file.  Note: increments a local
## slave_number variable to assign ports.
## ----------------------------------------------------------------------------

declare -i slave_number

function make-gpu-slaves-entry {
    ip=$1

    port=$((${BASE_SLAVE_PORT} + ${slave_number}))
    slave_number=$((${slave_number} + 1))

    echo "1 ${ip}:${port} ${APP_NAME} ${DATA_DIR}/input ${DATA_DIR}/output"
}

function make-gpu-slaves {
    ip_file=$1
    output_file=$2

    [ -e ${output_file} ] && abort "gpu-slaves file already exists"

    slave_number=0

    remote-cluster-op "" make-gpu-slaves-entry "${ip_file}" > ${output_file}
}

## ----------------------------------------------------------------------------
## Create a submitter.yml file.  Based on
## tf/target/classes/submitter.yml.
## ----------------------------------------------------------------------------

## TODO: could replace the cat with calls to yq.
## TODO: make sure we actually need this (it's passed to java as an argument
## to start the master, but the tests on the lab machines didn't have it and
## worked anyway.

function make-submitter-master {
    ip=$1

    cat << EOF
map:
  main :
    appType: direct
    imageExtensions:
      - jpg
      - jpeg
      - png
      - gif
      - svg
      - tiff
      - mpg
    batchSize: 2
    gpuRegistryHost: ${ip}
    gpuRegistryPort: "${GPU_REGISTRY_PORT}"
EOF
}

function make-submitter {
    ip_file=$1
    output_file=$2

    [ -e ${output_file} ] && abort "submitter file already exists"

    remote-cluster-op make-submitter-master "" "${ip_file}" > ${output_file}
}

## ----------------------------------------------------------------------------
## Create an apps-config.yml file.
## ----------------------------------------------------------------------------

## TODO: could replace the cat with calls to yq.

function make-apps-config-master {
    ip=$1

    cat << EOF
connections:
  gpuRegistryHost: ${ip}
  gpuRegistryPort: "${GPU_REGISTRY_PORT}"
defaults:
  apps:
    test-local:
      cmdline: ${OCSPARK}/newapp/newapp ${ip}
      rundir: ${DATA_DIR}/run
      tmpdir: ${DATA_DIR}/tmp
environments:
  linux:
    env:
      POINTR_HOME: /shared/pointr
      TENSORFLOW_HOME: /home/ubuntu/tensorflow
      DARKNET_HOME: /git/darknet
      TF_CPP_MIN_LOG_LEVEL: "2"
  osx:
    env:
      POINTR_HOME: /home/mike/tmp
      TENSORFLOW_HOME: /home/mike/tmp
      DARKNET_HOME: /home/mike/tmp
      TF_CPP_MIN_LOG_LEVEL: "2"
EOF
}

function make-apps-config {
    ip_file=$1
    output_file=$2

    [ -e ${output_file} ] && abort "apps-config file already exists"

    remote-cluster-op make-apps-config-master "" "${ip_file}" > ${output_file}
}

## ----------------------------------------------------------------------------
## Create a shared directory (tarball) ready for deployment.
## ----------------------------------------------------------------------------

function make-shared {
    ip_file=$1
    shared_dir=$2

    mkdir -p ${shared_dir}/conf || abort "can't make shared directory"

    make-gpu-slaves ${ip_file} ${shared_dir}/gpu-slaves.txt
    make-apps-config ${ip_file} ${shared_dir}/conf/apps-config.yml
    make-submitter ${ip_file} ${shared_dir}/conf/submitter.yml

    cp ${OCSPARK}/tf/target/tf-1.0.0.jar ${shared_dir}/ ||
        abort "copy tf.jar failed"
    cp ${OCSPARK}/bin/runtfserver.sh ${shared_dir}/ ||
        abort "copy runtfserver.sh failed"

    chmod +x ${shared_dir}/runtfserver.sh

    echo "localhost" > ${shared_dir}/conf/hostname
}


## ----------------------------------------------------------------------------
## Entry point.
## ----------------------------------------------------------------------------

TASK=$1

if [ "${TASK}" == "adopt" ]; then
    IP=$2

    adopt-remote-host "${IP}"
    authorize-remote-ssh "${IP}"
elif [ "${TASK}" == "adopt-cluster" ]; then
    IP_FILE=$2

    adopt-remote-cluster "${IP_FILE}"
elif [ "${TASK}" == "gpu-slaves" ]; then
    IP_FILE=$2
    OUTPUT_FILE=$3

    make-gpu-slaves "${IP_FILE}" "${OUTPUT_FILE}"
elif [ "${TASK}" == "submitter" ]; then
    IP_FILE=$2
    OUTPUT_FILE=$3

    make-submitter "${IP_FILE}" "${OUTPUT_FILE}"
elif [ "${TASK}" == "apps-config" ]; then
    IP_FILE=$2
    OUTPUT_FILE=$3

    make-apps-config "${IP_FILE}" "${OUTPUT_FILE}"
elif [ "${TASK}" == "shared" ]; then
    IP_FILE=$2
    SHARED_DIR=$3

    make-shared "${IP_FILE}" "${SHARED_DIR}"
elif [ "${TASK}" == "setup" ]; then
    CLASS=$2

    set-up-java
    set-up-misc
    set-up-yq

    if [ "${CLASS}" == "" ]; then
        :
    elif [ "${CLASS}" == "dev" ]; then
        set-up-scala
        set-up-maven
    else
        abort "Unexpected class specified (expecting nothing or 'dev')"
    fi
elif [ "${TASK}" == "build" ]; then
    build
elif [ "${TASK}" == "clone" ]; then
    clone
elif [ "${TASK}" == "package" ]; then
    package
elif [ "${TASK}" == "deploy" ]; then
    deploy
elif [ "${TASK}" == "start" ]; then
    CLASS=$2

    if [ "${CLASS}" == "slave" ]; then
        start-slave
    elif [ "${CLASS}" == "multi-slave" ]; then
        start-remote-slaves $3
    elif [ "${CLASS}" == "master" ]; then
        start-master
    elif [ "${CLASS}" == "cluster" ]; then
        start-remote-slave $3 && start-master
    elif [ "${CLASS}" == "multi-cluster" ]; then
        start-remote-slaves $3 && start-master
    else
        abort "Unexpected or no class specified (expecting 'master', 'slave', 'multi-slave', 'cluster', 'multi-cluster')."
    fi
elif [ "${TASK}" == "stop" ]; then
    CLASS=$2

    if [ "${CLASS}" == "slave" ]; then
        stop-slave
    elif [ "${CLASS}" == "multi-slave" ]; then
        stop-remote-slaves $3
    elif [ "${CLASS}" == "master" ]; then
        stop-master
    elif [ "${CLASS}" == "cluster" ]; then
        stop-remote-slave $3 && stop-master
    elif [ "${CLASS}" == "multi-cluster" ]; then
        stop-remote-slaves $3 && stop-master
    else
        abort "Unexpected or no class specified (expecting 'master', 'slave', 'multi-slave', 'cluster', 'multi-cluster')"
    fi
else
    abort "Unexpected or no task specified (expecting 'setup', 'build', 'clone', 'package', 'deploy', 'start', 'stop')"
fi
