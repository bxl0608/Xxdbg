package com.bxlong.xxdbg;

import keystone.Keystone;
import keystone.KeystoneArchitecture;
import keystone.KeystoneMode;
import unicorn.*;

public class UnicornTest {

    private static long BASE = 0x1000;

    public static void main(String[] args) {
        Keystone keystone = new Keystone(KeystoneArchitecture.Arm, KeystoneMode.ArmThumb);
        byte[] code = keystone.assemble("" +
                "movs r0, #2\n" +
                "movs r1, #2\n" +
                "add r0, r1\n" +
                "movs r2, #0x1100\n" +
                "str r0, [r2, #0]\n" +
                "ldr r3, [r2, #0]").getMachineCode();


        //byte[] code = {0x4f, (byte) 0xf0,0x01,0x00,0x4f, (byte) 0xf0,0x02,0x01,0x08,0x44};
        //byte[] code = {0x00, 0x01, (byte) 0xf0, 0x4f,};

        Unicorn unicorn = new Unicorn(Unicorn.UC_ARCH_ARM, Unicorn.UC_MODE_THUMB);
        unicorn.mem_map(BASE, 2 * 1024 * 1024, Unicorn.UC_PROT_ALL);
        //unicorn.mem_map(BASE+3 * 1024 * 1024, 2 * 1024 * 1024, Unicorn.UC_PROT_ALL);
        //EventMem Hook Test
        unicorn.hook_add(new EventMemHook() {
            public boolean hook(Unicorn u, long address, int size, long value, Object user) {
                System.out.print(String.format(">>> Error read unmapped mem at 0x%x, instruction size = 0x%x\n", address, size));
                return true;
            }
        }, UnicornConst.UC_ERR_WRITE_UNMAPPED, null);
        unicorn.mem_write(BASE, code);

        //Code Hook Test
        unicorn.hook_add(new CodeHook() {
            public void hook(Unicorn u, long address, int size, Object user) {
                System.out.print(String.format(">>> Tracing instruction at 0x%x, instruction size = 0x%x\n", address, size));
            }
        }, BASE + 2, BASE + 2, null);

        //Block Hook Test
        unicorn.hook_add(new BlockHook() {
            public void hook(Unicorn u, long address, int size, Object user) {
                System.out.print(String.format(">>> Tracing basic block at 0x%x, block size = 0x%x\n", address, size));
            }
        }, BASE, BASE + code.length, null);

        //Read Hook Test
        unicorn.hook_add(new ReadHook() {
            public void hook(Unicorn u, long address, int size, Object user) {
                System.out.print(String.format(">>> Tracing read mem at 0x%x, block size = 0x%x\n", address, size));
            }
        }, BASE + 0x100, BASE + 0x102, null);

        //Write Hook Test
        unicorn.hook_add(new WriteHook() {
            public void hook(Unicorn u, long address, int size, long value, Object user) {
                System.out.print(String.format(">>> Tracing write mem at 0x%x, value=0x%x, size = 0x%x\n", address, value, size));
            }
        }, BASE + 0x100, BASE + 0x102, null);


        unicorn.emu_start(BASE + 1, BASE + code.length, 0, 0);
        Long o = (Long) unicorn.reg_read(Unicorn.UC_ARM_REG_R0);
        System.out.println(o.intValue());

    }
}
