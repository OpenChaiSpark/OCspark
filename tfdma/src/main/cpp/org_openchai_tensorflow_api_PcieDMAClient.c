/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class org_openchai_tensorflow_api_PcieDMAClient */

#ifndef _Included_org_openchai_tensorflow_api_PcieDMAClient
#define _Included_org_openchai_tensorflow_api_PcieDMAClient
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    setupChannelN
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_setupChannelN
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    prepareWriteN
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_prepareWriteN
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    writeN
 * Signature: (Ljava/lang/String;[B[B)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_writeN
  (JNIEnv *, jobject, jstring, jbyteArray, jbyteArray);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    completeWriteN
 * Signature: (Ljava/lang/String;)Lorg/openchai/tensorflow/api/DMAStructures/WriteResultStruct;
 */
JNIEXPORT jobject JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_completeWriteN
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    prepareReadN
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_prepareReadN
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    readN
 * Signature: (Ljava/lang/String;[B[B)Lorg/openchai/tensorflow/api/DMAStructures/ReadResultStruct;
 */
JNIEXPORT jobject JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_readN
  (JNIEnv *, jobject, jstring, jbyteArray, jbyteArray);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    completeReadN
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_completeReadN
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    shutdownChannelN
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_shutdownChannelN
  (JNIEnv *, jobject, jstring);

/*
 * Class:     org_openchai_tensorflow_api_PcieDMAClient
 * Method:    readDataN
 * Signature: ([B)[B
 */
JNIEXPORT jbyteArray JNICALL Java_org_openchai_tensorflow_api_PcieDMAClient_readDataN
  (JNIEnv *, jobject, jbyteArray);

#ifdef __cplusplus
}
#endif
#endif