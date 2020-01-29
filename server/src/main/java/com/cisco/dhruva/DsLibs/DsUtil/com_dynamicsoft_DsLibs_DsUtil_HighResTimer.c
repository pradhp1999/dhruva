#include "com_dynamicsoft_DsLibs_DsUtil_HighResTimer.h"

#include <sys/time.h>

/*
 * Class:     HighResTimer
 * Method:    gethrtime
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_HighResTimer_gethrtime
  (JNIEnv *env, jobject obj)
{
    return gethrtime();
}

