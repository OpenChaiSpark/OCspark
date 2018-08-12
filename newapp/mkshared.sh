GITDIR=${HOME}/work/pointr/src/OCspark
TARGET=/shared
CMDNAME=test-local
#CMD=du
CMD=${GITDIR}/newapp/newapp
INPUTDIR=${HOME}/tmp/ocspark/input
OUTPUTDIR=${HOME}/tmp/ocspark/output
RUNDIR=${HOME}/tmp/ocspark/run
TMPDIR=${HOME}/tmp/ocspark/tmp
##IP=10.0.0.142
IP=127.0.0.1

## Create the necessary dirs.

sudo mkdir -p ${TARGET}/conf
mkdir -p ${INPUTDIR}
mkdir -p ${OUTPUTDIR}
mkdir -p ${RUNDIR}
mkdir -p ${TMPDIR}

## Create the gpu-slaves.txt file (based on format of
## ${GITDIR}/tf/src/main/resources/gpu-slaves.txt).

echo "1 localhost:61230  ${CMDNAME} ${INPUTDIR}  ${OUTPUTDIR}" |\
    sudo tee ${TARGET}/gpu-slaves.txt > /dev/null

## Heavily modified copy of ${GITDIR}/tf/target/classes/apps-config.yml .
## Note: must use explicit IP address rather than localhost here.

cat ${GITDIR}/tf/src/main/resources/apps-config.yml |\
    yq w - connections.gpuRegistryHost "${IP}" |\
    yq w - defaults.apps.${CMDNAME}.cmdline "${CMD} \${1}" |\
    yq w - defaults.apps.${CMDNAME}.rundir "${RUNDIR}" |\
    yq w - defaults.apps.${CMDNAME}.tmpdir "${TMPDIR}" |\
    yq w - environments.osx.env.POINTR_HOME "${TMPDIR}" |\
    yq w - environments.osx.env.TENSORFLOW_HOME "${TMPDIR}" |\
    yq w - environments.osx.env.DARKNET_HOME "${TMPDIR}" |\
    sudo tee ${TARGET}/conf/apps-config.yml > /dev/null

## Copy more stuff.

sudo cp ${GITDIR}/tf/target/classes/submitter.yml ${TARGET}/conf/
sudo cp ${GITDIR}/tf/target/tf-1.0.0.jar ${TARGET}/
sudo cp ${GITDIR}/bin/runtfserver.sh ${TARGET}/

## Make run script executable.

sudo chmod +x ${TARGET}/runtfserver.sh

## Added by me to avoid (caught) exception on server start.  Actual
## value (ie. "localhost") seems to be arbitrary.

echo "localhost" | sudo tee ${TARGET}/conf/hostname > /dev/null
