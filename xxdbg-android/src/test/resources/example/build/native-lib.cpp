#include <jni.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stdint.h>
#include<unistd.h>
#include<android/log.h>

int fib(int i){
    if (i == 2 || i == 1){
        return 1;
    }
    return fib(i-1) + fib(i-2);
}

extern "C"
JNIEXPORT jint JNICALL
Java_com_dta_lesson5_MainActivity_add(JNIEnv *env, jobject thiz, jint a, jint b) {
    __android_log_print(4,"xxdbg","hello world, %d",123);
//    __asm__ __volatile__(" \
//    movs r4, r0\r\n \
//    movs r0, #1\r\n \
//    movs r1, %0\r\n \
//    movs r2, #14\r\n \
//    movs r7, #0x4\r\n \
//    svc #0 \r\n \
//    movs r0, r4" \
//     : "=r"(buf));
    char *m = (char *)malloc(10);
    memset(m,0,10);
    memcpy(m,"123456789",9);
    //char *m = "123456789\0";
    __android_log_print(3,"xxdbg","%s",m);
    if(a < 0){
        a = -a;
    }
    if(b < 0){
        b = -b;
    }

    return fib(a+b);
}