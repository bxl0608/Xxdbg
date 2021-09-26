package com.bxlong.xxdbg;

import keystone.Keystone;
import keystone.KeystoneArchitecture;
import keystone.KeystoneMode;
import unicorn.ArmConst;
import unicorn.Unicorn;

public class UnicornTest {
    private static long BASE = 0x1000;

    public static void main(String[] args) {
        Keystone keystone = new Keystone(KeystoneArchitecture.Arm, KeystoneMode.ArmThumb);
        byte[] code = keystone.assemble("" +
                "mov r0, #2\n" +
                "mov r1, #2\n" +
                "add r0,r1").getMachineCode();


        //byte[] code = {0x4f, (byte) 0xf0,0x01,0x00,0x4f, (byte) 0xf0,0x02,0x01,0x08,0x44};
        //byte[] code = {0x00, 0x01, (byte) 0xf0, 0x4f,};

        Unicorn unicorn = new Unicorn(Unicorn.UC_ARCH_ARM, Unicorn.UC_MODE_THUMB);
        unicorn.mem_map(BASE, 2 * 1024 * 1024, Unicorn.UC_PROT_ALL);
        unicorn.mem_write(BASE, code);
        unicorn.emu_start(BASE + 1, BASE + code.length, 0, 0);
        Long o = (Long) unicorn.reg_read(Unicorn.UC_ARM_REG_R0);
        System.out.println(o.intValue());

    }
}
