package com.bxlong.xxdbg.linux.file;

import com.bxlong.xxdbg.android.emulater.IEmulate;
import com.bxlong.xxdbg.memory.Pointer;

public interface LinuxIO {
    int read(Pointer buffer, int count);

    int stat(Pointer stat_buf);

    boolean isCanRead();

    int fcntl(IEmulate emulate, int cmd, long arg);

    int connect(Pointer addr, int addrlen);

    int write(byte[] data);
}
