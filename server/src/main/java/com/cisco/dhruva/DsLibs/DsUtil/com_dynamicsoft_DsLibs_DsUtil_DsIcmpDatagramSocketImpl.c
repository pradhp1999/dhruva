#include <sys/types.h>
#include <sys/socket.h>
#include <stdlib.h>
#include <netinet/in.h>
#include <inttypes.h>
#include <errno.h>
#include <string.h>

#include "com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl.h"


jfieldID dp_bufID;
jfieldID dp_offsetID;
jfieldID dp_lengthID;
jfieldID dp_addressID;
jfieldID dp_portID;
jfieldID ia_addressID;
jfieldID ia_familyID;
  
jfieldID ids_fdID;
jfieldID ids_lclportID;
jfieldID ids_lcladdressID;

int ERROR = -1;


/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_init
  (JNIEnv * env, jclass thisClass)
{
    ids_fdID = (*env)->GetFieldID(env,thisClass,"fd","I");
    ids_lclportID = (*env)->GetFieldID(env,thisClass,"localPort","I");
    ids_lcladdressID = (*env)->GetFieldID(env,thisClass,"localAddress","Ljava/net/InetAddress;");

    thisClass = (*env)->FindClass(env,"java/net/DatagramPacket");
    dp_bufID = (*env)->GetFieldID(env, thisClass, "buf", "[B");
    dp_offsetID = (*env)->GetFieldID(env, thisClass, "offset", "I");
    dp_lengthID = (*env)->GetFieldID(env, thisClass, "length", "I");
    dp_addressID = (*env)->GetFieldID(env, thisClass, "address", "Ljava/net/InetAddress;");
    dp_portID = (*env)->GetFieldID(env, thisClass, "port", "I");
    (*env)->DeleteLocalRef(env,thisClass);

    thisClass = (*env)->FindClass(env,"java/net/InetAddress");
    ia_addressID = (*env)->GetFieldID(env, thisClass, "address", "I");
    ia_familyID = (*env)->GetFieldID(env, thisClass, "family", "I");
    (*env)->DeleteLocalRef(env,thisClass);
}//Ends init

JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_create
  (JNIEnv * env, jobject thisObj)
{
    int fd;
    struct sockaddr_in lcladdr;
    jint lcladdrlen = sizeof(lcladdr);
    jint ret;
    jobject dsaddr;
    jclass ia_clazz;
    jmethodID ia_ctor;

    fd =  socket(AF_INET, SOCK_DGRAM, 0);
    ERROR = errno;
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", strerror(ERROR));
	    return;
    }
    (*env)->SetIntField(env, thisObj, ids_fdID, fd);
    memset((char *)&lcladdr,0,sizeof(lcladdr));
    ret = getsockname(fd, (struct sockaddr*)&lcladdr, &lcladdrlen);
    ERROR = errno;
    if(ret == -1)
    {
	    close(fd);
	    JNU_ThrowByName(env,"java/net/SocketException", strerror(ERROR));
	    return;
    }
    (*env)->SetIntField(env, thisObj, ids_lclportID, ntohs(lcladdr.sin_port));
    dsaddr = (*env)->GetObjectField(env, thisObj, ids_lcladdressID);
    if ( dsaddr == NULL)
    {
        ia_clazz = (*env)->FindClass(env, "java/net/InetAddress");
        ia_ctor = (*env)->GetMethodID(env, ia_clazz, "<init>", "()V");
        dsaddr = (*env)->NewObject(env, ia_clazz, ia_ctor);
        (*env)->SetObjectField(env, thisObj, ids_lcladdressID, dsaddr);
    }
    (*env)->SetIntField(env, dsaddr, ia_addressID, ntohl(lcladdr.sin_addr.s_addr));
    // There is discrepancy in the Family ID for different versions of Java InetAddress
    //--(*env)->SetIntField(env, dsaddr, ia_familyID, lcladdr.sin_family);

}//ends create


/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    bind 
 * Signature: (Ljava/net/InetAddress;I)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_bind
  (JNIEnv * env, jobject thisObj, jint port, jobject address)
{
    int fd;

    struct sockaddr_in lcladdr;
    jint lcladdrlen = sizeof(lcladdr);
    jint adr;
    jint family;
    jint ret;
    jobject dsaddr;
    jclass ia_clazz;
    jmethodID ia_ctor;

    // this is wrong: NULL should mean INADDR_ANY -dg
    
    //if (address == NULL)
    //{
        //JNU_ThrowByName(env, "java/lang/NullPointerException", "The address to be bound is null");
        //return;
    //}

    fd = (*env)->GetIntField(env,thisObj,ids_fdID);
    if (fd == -1)
    {
	JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }


    if (address != NULL)
    {
        adr = (*env)->GetIntField(env, address, ia_addressID);
        // There is discrepancy in the Family ID for different versions of Java InetAddress
        //--family = (*env)->GetIntField(env, address, ia_familyID);
    }
    else
    {
        adr = INADDR_ANY;
        // There is discrepancy in the Family ID for different versions of Java InetAddress
        //--family = PF_INET;
    }


    memset((char *)&lcladdr,0,sizeof(lcladdr));
    lcladdr.sin_port = htons((short)port);
    lcladdr.sin_addr.s_addr = (unsigned long)htonl(adr);
    // There is discrepancy in the Family ID for different versions of Java InetAddress
    //--lcladdr.sin_family = family;
    lcladdr.sin_family = AF_INET;

    ret = bind(fd,(struct sockaddr*)&lcladdr,sizeof(lcladdr));
    ERROR = errno;
    if(ret != 0)
    {
	    close(fd);
	    JNU_ThrowByName(env,"java/net/SocketException", strerror(ERROR));
	    return;
    }

    ret = getsockname(fd, (struct sockaddr*)&lcladdr, &lcladdrlen);
    ERROR = errno;
    if(ret == -1)
    {
	    close(fd);

	    JNU_ThrowByName(env,"java/net/SocketException", strerror(ERROR));
	    return;
    }
    (*env)->SetIntField(env, thisObj, ids_lclportID, ntohs(lcladdr.sin_port));
    dsaddr = (*env)->GetObjectField(env, thisObj, ids_lcladdressID);
    if ( dsaddr == NULL)
    {
        ia_clazz = (*env)->FindClass(env, "java/net/InetAddress");
        ia_ctor = (*env)->GetMethodID(env, ia_clazz, "<init>", "()V");
        dsaddr = (*env)->NewObject(env, ia_clazz, ia_ctor);
        (*env)->SetObjectField(env, thisObj, ids_lcladdressID, dsaddr);
    }
    (*env)->SetIntField(env, dsaddr, ia_addressID, ntohl(lcladdr.sin_addr.s_addr));
    // There is discrepancy in the Family ID for different versions of Java InetAddress
    //--(*env)->SetIntField(env, dsaddr, ia_familyID, lcladdr.sin_family);

}//ends bind


/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    connect
 * Signature: (Ljava/net/InetAddress;I)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_connect
  (JNIEnv * env, jobject thisObj, jobject address, jint port)
{
    jint fd;
    struct sockaddr_in rmtaddr;
    jint adr;
    jint family;
    jint ret;


    fd = (*env)->GetIntField(env,thisObj,ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }
    adr = (*env)->GetIntField(env, address, ia_addressID);
    // There is discrepancy in the Family ID for different versions of Java InetAddress
    //--family = (*env)->GetIntField(env, address, ia_familyID);

    memset((char *)&rmtaddr,0,sizeof(rmtaddr));
    rmtaddr.sin_port = htons((short)port);
    rmtaddr.sin_addr.s_addr = (unsigned long)htonl(adr);
    // There is discrepancy in the Family ID for different versions of Java InetAddress
    //--rmtaddr.sin_family = family;
    rmtaddr.sin_family = AF_INET;

    ret = connect(fd,(struct sockaddr*)&rmtaddr,sizeof(rmtaddr));
    ERROR = errno;
    if ( ret == -1 )
    {
        JNU_ThrowByName(env,"java/net/SocketException",strerror(ERROR));
        return;
    }
}// Ends connect

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    close
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_close
  (JNIEnv * env, jobject thisObj)
{
    int fd = -1;
    int res = -1;

    fd = (*env)->GetIntField(env, thisObj, ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }
    res = close(fd);
    ERROR = errno;
    (*env)->SetIntField(env, thisObj, ids_fdID, -1);
    if (res == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", strerror(ERROR));
	    return;
    }
}//Ends close


/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    send
 * Signature: (Ljava/net/DatagramPacket;)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_send
  (JNIEnv * env, jobject thisObj, jobject packet)
{
    char BUF[MAX_BUFFER_LEN];
    char *fullPacket;
    jint fd;
    jint res;
    jint packetBufferOffset, packetBufferLen;
    jbyteArray packetBuffer;

    if (packet == NULL)
    {
	    JNU_ThrowByName(env,"java/lang/NullPointerException", "null packet");
	    return;
    }

    fd = (*env)->GetIntField(env, thisObj, ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }
    packetBufferOffset = (*env)->GetIntField(env, packet, dp_offsetID);
    packetBuffer = (jbyteArray)(*env)->GetObjectField(env, packet, dp_bufID);

    if ( packetBuffer == NULL )
    {
	    JNU_ThrowByName(env, "java/lang/NullPointerException", "null buffer");
	    return;
    }

    packetBufferLen = (*env)->GetIntField(env, packet, dp_lengthID);

    if (packetBufferLen > MAX_BUFFER_LEN)
    {
	    fullPacket = (char *)malloc(packetBufferLen);
	    if (!fullPacket)
        {
	        JNU_ThrowByName(env,"java/lang/OutOfMemoryError", "heap allocation failed");
	        return;
	    }
    }
    else
    {
	    fullPacket = &(BUF[0]);
    }

    (*env)->GetByteArrayRegion(env, packetBuffer, packetBufferOffset, packetBufferLen, (jbyte *)fullPacket);
    res = send(fd, fullPacket,packetBufferLen,0);
    ERROR = errno;
    if (packetBufferLen > MAX_BUFFER_LEN)
    {
	free(fullPacket);
    }
    if ( res == -1 )
    {
        JNU_ThrowByName(env,"java/net/SocketException", strerror(ERROR));
        return;
    }

}//Ends send

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    receive
 * Signature: (Ljava/net/DatagramPacket;)V
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_receive
  (JNIEnv * env, jobject thisObj, jobject packet)
{
    char BUF[MAX_BUFFER_LEN];
    char *fullPacket;
    jint fd;
    jint res;
    jint packetBufferOffset, packetBufferLen, len;
    jbyteArray packetBuffer;

    struct sockaddr_in raddr;
    jint raddr_len = sizeof(raddr);
    jclass ia_clazz;
    jmethodID ia_ctor;
    jobject packetAddress;

    if (packet == NULL)
    {
	    JNU_ThrowByName(env,"java/lang/NullPointerException", "null packet");
	    return;
    }

    fd = (*env)->GetIntField(env, thisObj, ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }
    packetBufferOffset = (*env)->GetIntField(env, packet, dp_offsetID);
    packetBuffer = (jbyteArray)(*env)->GetObjectField(env, packet, dp_bufID);

    if ( packetBuffer == NULL )
    {
	    JNU_ThrowByName(env, "java/lang/NullPointerException", "null buffer");
	    return;
    }

    packetBufferLen = (*env)->GetIntField(env, packet, dp_lengthID);

    if (packetBufferLen > MAX_BUFFER_LEN)
    {
	fullPacket = (char *)malloc(packetBufferLen);
	if (!fullPacket)
        {
	    JNU_ThrowByName(env,"java/lang/OutOfMemoryError", "heap allocation failed");
	    return;
	}
    }
    else
    {
	    fullPacket = &(BUF[0]);
    }
    res = recvfrom(fd, fullPacket, packetBufferLen, 0, (struct sockaddr *)&raddr, &raddr_len);
    ERROR = errno;
    if (res < 0) 
    {
	JNU_ThrowByName(env,"java/net/SocketException", "Error while receiving datagram packet");
    } 
    else 
    {
	if ( (len = res) > packetBufferLen)
	{
	    len = packetBufferLen;
	}
        (*env)->SetByteArrayRegion(env, packetBuffer, packetBufferOffset, len, (jbyte *)fullPacket);
        (*env)->SetIntField(env, packet, dp_lengthID, len);
        ia_clazz = (*env)->FindClass(env, "java/net/InetAddress");
        ia_ctor = (*env)->GetMethodID(env, ia_clazz, "<init>", "()V");
        packetAddress = (*env)->NewObject(env, ia_clazz, ia_ctor);
        (*env)->SetIntField(env, packetAddress, ia_addressID, ntohl(raddr.sin_addr.s_addr));
        // There is discrepancy in the Family ID for different versions of Java InetAddress
        //--(*env)->SetIntField(env, packetAddress, ia_familyID, raddr.sin_family);
        /* stuff the new address in the packet */
        (*env)->SetObjectField(env, packet, dp_addressID, packetAddress);
        (*env)->SetIntField(env, packet, dp_portID, ntohs(raddr.sin_port));
    }
    if (packetBufferLen > MAX_BUFFER_LEN) 
    {
	free(fullPacket);
    }
}// Ends receive()

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    setReceiveBufferSize
 * Signature: (I)
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_setReceiveBufferSize
  (JNIEnv * env, jobject thisObj, jint size)
{
    jint fd, ret, len;

    fd = (*env)->GetIntField(env, thisObj, ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }

    len = sizeof(size);
    ret = setsockopt(fd, SOL_SOCKET, SO_RCVBUF, &size, len);     
    ERROR = errno;
    if (ret < 0)
    {
	JNU_ThrowByName(env,"java/net/SocketException", "Error while setting the datagram socket's receive buffer size");
    }
	
}// Ends setReceiveBufferSize()

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    getReceiveBufferSize
 * Signature: ()
 */
JNIEXPORT int JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_getReceiveBufferSize
  (JNIEnv * env, jobject thisObj)
{
    int fd, ret, len, size;

    fd = (*env)->GetIntField(env, thisObj, ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }

    size = 0;
    len = sizeof(size);
    ret = getsockopt(fd, SOL_SOCKET, SO_RCVBUF, &size, &len);     
    ERROR = errno;
    if (ret < 0)
    {
	JNU_ThrowByName(env,"java/net/SocketException", "Error while retrieving the datagram socket's receive buffer size");
    }
    return size;
}//Ends getReceiveBufferSize()

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    setSendBufferSize
 * Signature: (I)
 */
JNIEXPORT void JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_setSendBufferSize
  (JNIEnv * env, jobject thisObj, jint size)
{
    jint fd, ret, len;

    fd = (*env)->GetIntField(env, thisObj, ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }

    len = sizeof(size);
    ret = setsockopt(fd, SOL_SOCKET, SO_SNDBUF, &size, len);     
    ERROR = errno;
    if (ret < 0)
    {
	JNU_ThrowByName(env,"java/net/SocketException", "Error while setting the datagram socket's send buffer size");
    }
	
}// Ends setSendBufferSize()

/*
 * Class:     com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl
 * Method:    getSendBufferSize
 * Signature: ()
 */
JNIEXPORT int JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_DsIcmpDatagramSocketImpl_getSendBufferSize
  (JNIEnv * env, jobject thisObj)
{
    int fd, ret, len, size;

    fd = (*env)->GetIntField(env, thisObj, ids_fdID);
    if (fd == -1)
    {
	    JNU_ThrowByName(env,"java/net/SocketException", "Socket closed");
	    return;
    }

    size = 0;
    len = sizeof(size);
    ret = getsockopt(fd, SOL_SOCKET, SO_SNDBUF, &size, &len);     
    ERROR = errno;
    if (ret < 0)
    {
	JNU_ThrowByName(env,"java/net/SocketException", "Error while retrieving the datagram socket's send buffer size");
    }
    return size;
}//Ends getSendBufferSize()
	

