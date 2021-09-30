package com.bxlong.xxdbg.android.emulater;

import com.bxlong.xxdbg.backend.BackendType;
import com.bxlong.xxdbg.backend.IBackend;
import com.bxlong.xxdbg.backend.unicorn.UnicornBackend;
import com.bxlong.xxdbg.memory.Memory;
import com.bxlong.xxdbg.utils.FileHelper;
import com.sun.istack.internal.logging.Logger;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;


import java.io.File;
import java.util.Properties;

public final class AndroidEmulate implements IEmulate {

    private AndroidEmulate() {
    }

    /**
     * 32位/64位标识
     */
    private boolean is32Bit = true;

    public boolean is32Bit() {
        return is32Bit;
    }

    public boolean is64Bit() {
        return !is32Bit;
    }

    /**
     * 后端实例
     */
    IBackend backend = null;

    public IBackend getBackend() {
        if (backend == null) {
            throw new AndroidEmulateException("the backend is null");
        }
        return backend;
    }

    /**
     * 内存实例
     */
    Memory memory = null;

    public Memory getMemory() {
        if (memory == null){
            throw new AndroidEmulateException("the memory is null");
        }
        return memory;
    }

    public File getSystemLibrary(String name) {
        //name = name.replaceAll("\\+","p");
        File libFile = FileHelper.getResourceFile(AndroidEmulate.class, "android/ld/" + name);
        return libFile;
    }

    // 默认使用Unicorn
    BackendType backendType;

    /**
     * 建造类
     */
    public static class Builder {
        private boolean is32Bit = true;
        private BackendType backendType = BackendType.Unicorn;

        public Builder(){
            //Logger.getLogger(AndroidEmulate.class).info("123");
            //PropertyConfigurator.configure(AndroidEmulate.class.getClassLoader().getResource("log4j.properties"));
            //BasicConfigurator.configure();
        }

        public Builder for32Bit() {
            is32Bit = true;
            return this;
        }

        public Builder for64Bit() {
            is32Bit = false;
            return this;
        }

        public Builder setBackendType(BackendType backendType) {
            this.backendType = backendType;
            return this;
        }

        public AndroidEmulate build() {
            AndroidEmulate emulate = new AndroidEmulate();
            emulate.is32Bit = this.is32Bit;
            emulate.backendType = this.backendType;

            switch (emulate.backendType) {
                case Unicorn:
                    emulate.backend = new UnicornBackend(is32Bit);
                    break;

                default:
                    throw new AndroidEmulateException("Unknown the backend type!");
            }
            emulate.memory = new Memory(emulate);

            return emulate;
        }
    }
}
