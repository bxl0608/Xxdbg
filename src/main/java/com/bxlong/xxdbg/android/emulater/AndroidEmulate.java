package com.bxlong.xxdbg.android.emulater;

import com.bxlong.xxdbg.backend.IBackend;
import com.bxlong.xxdbg.backend.unicorn.UnicornBackend;

public class AndroidEmulate implements IEmulate {

    private AndroidEmulate() {
    }

    /**
     * 后端类型, 待扩展
     */
    public enum BackendType {
        Unicorn
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

    // 默认使用Unicorn
    BackendType backendType;

    /**
     * 建造类
     */
    public static class Builder {
        private boolean is32Bit = true;
        private BackendType backendType = BackendType.Unicorn;

        public Builder for32Bit() {
            is32Bit = true;
            return this;
        }

        public Builder for64Bit() {
            is32Bit = false;
            return this;
        }

        public Builder setBackendType(BackendType backendType){
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
            return emulate;
        }
    }
}
