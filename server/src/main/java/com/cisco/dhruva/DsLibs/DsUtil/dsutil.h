#include "jni.h"

#define MAX_BUFFER_LEN 4096
/**
 *It contains the Utility functions that are required frequently in the code
 */
void JNU_ThrowByName(JNIEnv *env, char *name, char *message)
{
    jclass cls = (*env)->FindClass(env,name);
    if (cls != NULL)
    {
        (*env)->ThrowNew(env,cls,message);
    }
    (*env)->DeleteLocalRef(env,cls);

}
