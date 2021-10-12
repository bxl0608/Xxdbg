package com.bxlong.xxdbg;

import com.bxlong.xxdbg.android.emulater.AndroidEmulate;
import com.bxlong.xxdbg.android.module.ElfModule;
import com.bxlong.xxdbg.backend.BackendType;
import com.bxlong.xxdbg.memory.Memory;
import com.bxlong.xxdbg.memory.Pointer;
import com.bxlong.xxdbg.utils.FileHelper;
import unicorn.ArmConst;

public class AndroidEmulateTest {
    public static void main(String[] args) {
        AndroidEmulate emulate = new AndroidEmulate.Builder()
                .for32Bit()
                .setBackendType(BackendType.Unicorn)
                .build();

        Memory memory = emulate.getMemory();
        ElfModule elfModule = memory.loadLibrary(FileHelper.getResourceFile(AndroidEmulateTest.class, "example/libtest-lib.so"),true);
        emulate.traceCode(0,-1);
        emulate.getBackend().reg_write(ArmConst.UC_ARM_REG_R2,4);
        emulate.getBackend().reg_write(ArmConst.UC_ARM_REG_R3,6);
        Number number = emulate.eFunc(elfModule.getBase() + 0x799, 0);
        System.out.println(number.intValue());
//        Pointer pointer = new Pointer(emulate,elfModule.getBase()+0x7fde4);
//        byte[] byteArray = pointer.getByteArray(0, 4);
//        System.out.println(byteArray);



    }

    public static String byteToHex(byte[] bytes){
        String strHex = "";
        StringBuilder sb = new StringBuilder("");
        for (int n = 0; n < bytes.length; n++) {
            strHex = Integer.toHexString(bytes[n] & 0xFF);
            sb.append((strHex.length() == 1) ? "0" + strHex : strHex);
        }
        return sb.toString().trim();
    }
}
