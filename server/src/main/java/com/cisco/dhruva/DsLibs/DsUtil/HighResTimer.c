#include "HighResTimer.h"

// Windows OS
#ifdef WIN32
#include <windows.h>
#include <time.h>

LARGE_INTEGER _HR_freq;
BOOL _HR_exists;

LONGLONG _HR_time()
{
	LARGE_INTEGER stamp;
	if (!_HR_exists)
	{
		_HR_exists = QueryPerformanceFrequency(&_HR_freq);	
	}
	if ( _HR_exists)
	{
		QueryPerformanceCounter(&stamp);
/*--
		printf("Tick = %I64d\n", stamp.QuadPart);
		printf("Tick(double) = %e\n", (double)stamp.QuadPart);
		printf("Tick(LONGLONG) = %I64d\n", (LONGLONG)stamp.QuadPart);
		printf("Tick(doble_LONGLONG) = %I64d\n", (LONGLONG)((double)stamp.QuadPart));
		printf("Tick(LONGLONG_double) = %e\n", (double)((LONGLONG)stamp.QuadPart));

		printf("Frequency = %I64d\n", _HR_freq.QuadPart);

		printf("Ticks/sec = %I64d\n", (stamp.QuadPart/_HR_freq.QuadPart));
		printf("Ticks/sec(double) = %e\n", (double)((double)stamp.QuadPart/(double)_HR_freq.QuadPart));
		printf("Ticks/sec(LONGLONG) = %I64d\n", (LONGLONG)((double)stamp.QuadPart/(double)_HR_freq.QuadPart));


		printf("Ticks/nsec = %I64d\n", ((stamp.QuadPart/_HR_freq.QuadPart)*1000000000));
		printf("Ticks/nsec(double) = %e\n", (double)(((double)stamp.QuadPart/(double)_HR_freq.QuadPart)*1000000000));
		printf("Ticks/nsec(LONGLONG) = %I64d\n", (LONGLONG)(((double)stamp.QuadPart/(double)_HR_freq.QuadPart)*1000000000));
--*/
		
		return (LONGLONG)(( (double)stamp.QuadPart/(double)_HR_freq.QuadPart) * 1000000000);
	}
	else return -1;
}

int _HR_usleep(unsigned long ms)
{
	Sleep(ms);
	return 0;
}

BOOL APIENTRY DllMain( HANDLE hModule, 
                       DWORD  ul_reason_for_call, 
                       LPVOID lpReserved
					 )
{
    return TRUE;
}

 
#endif	// WIN32

// Solaris OS
#if defined(__SVR4)
#include <sys/time.h>
#include <unistd.h>

hrtime_t _HR_time()
{
	return gethrtime();
}

int _HR_usleep(useconds_t ms)
{
	return usleep(ms);
}
#endif	// __SVR4


/*
 * Class:     HighResTimer
 * Method:    gethrtime
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_HighResTimer_gethrtime
  (JNIEnv *env, jobject obj)
{
    return _HR_time();
}

/*
 * Class:     HighResTimer
 * Method:    usleep
 * Signature: ()J
 */
JNIEXPORT jint JNICALL Java_com_dynamicsoft_DsLibs_DsUtil_HighResTimer_usleep
  (JNIEnv *env, jobject obj, jlong ms)
{
    return _HR_usleep((unsigned long)ms);
}
