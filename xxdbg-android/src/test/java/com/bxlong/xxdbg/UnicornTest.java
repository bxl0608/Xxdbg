package com.bxlong.xxdbg;

import keystone.Keystone;
import keystone.KeystoneArchitecture;
import keystone.KeystoneMode;
import unicorn.*;

public class UnicornTest {

    private static long BASE = 0x40000000L;
    private static long LR = 0xffff0000L;

    public static void main(String[] args) {
        Keystone keystone = new Keystone(KeystoneArchitecture.Arm, KeystoneMode.ArmThumb);
        byte[] code = keystone.assemble("" +
                "movs r0, #2\n" +
                "movs r1, #3\n" +
                "add r0, r1\n" +
                "mov r7, #3\n" +
                "svc #0\n" +
                "add r0, r1").getMachineCode();
//        Keystone keystone = new Keystone(KeystoneArchitecture.Arm, KeystoneMode.Arm);
//        byte[] code = keystone.assemble("" +
//                "add ip, pc, #0, #12\n" +
//                "add ip, ip, #0x3000\n" +
//                "ldr pc, [ip, #0x954]!").getMachineCode();

        //byte[] code = {0x4f, (byte) 0xf0,0x01,0x00,0x4f, (byte) 0xf0,0x02,0x01,0x08,0x44};
        //byte[] code = {0x00, 0x01, (byte) 0xf0, 0x4f,};

        Unicorn unicorn = new Unicorn(Unicorn.UC_ARCH_ARM, Unicorn.UC_MODE_THUMB);
        //unicorn.mem_map(0x4011a000, 16384, 0);
        //unicorn.mem_map(0x40000000,528384,0);
        //unicorn.mem_protect(0x40000000,528383,5);
//        unicorn.mem_map(0x40081000,40960,0);
//        unicorn.mem_map(0x4008b000,585728,0);
//        unicorn.mem_map(0x4011a000,0x4000,0);
        unicorn.mem_map(BASE, 40 * 1024 * 1024, Unicorn.UC_PROT_ALL);
        unicorn.mem_protect(BASE,1024 * 1024,UnicornConst.UC_PROT_WRITE);
        unicorn.mem_write(BASE, code);
        /*

        //EventMem Hook Test
        unicorn.hook_add(new EventMemHook() {
            public boolean hook(Unicorn u, long address, int size, long value, Object user) {
                System.out.print(String.format(">>> Error read unmapped mem at 0x%x, instruction size = 0x%x\n", address, size));
                return true;
            }
        }, UnicornConst.UC_ERR_WRITE_UNMAPPED, null);

*/
        //Code Hook Test
        unicorn.hook_add_new(new InterruptHook() {
            @Override
            public void hook(Unicorn u, int intno, Object user) {
                u.reg_write(ArmConst.UC_ARM_REG_R1, 4);
                System.out.print(String.format(">>> Interrupt occur, intno: %d\n", intno));
            }
        }, null);
        unicorn.hook_add(new CodeHook() {
            public void hook(Unicorn u, long address, int size, Object user) {
                System.out.print(String.format(">>> Tracing instruction at 0x%x, instruction size = 0x%x\n", address, size));
            }
        }, 0, -1, null);
/*
        unicorn.debugger_add(new DebugHook() {
            @Override
            public void onBreak(Unicorn u, long address, int size, Object user) {
                System.out.print(String.format(">>> Debugger onBreak at 0x%x, instruction size = 0x%x\n", address, size));
            }

            @Override
            public void hook(Unicorn u, long address, int size, Object user) {
                System.out.print(String.format(">>> Debugger hook instruction at 0x%x, instruction size = 0x%x\n", address, size));
            }
        },0,-1,null);
        unicorn.addBreakPoint(BASE);
        //unicorn.

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
*/
        //unicorn.reg_write(ArmConst.UC_ARM_REG_LR,LR);
        unicorn.emu_start(BASE + 1, BASE + code.length, 0, 0);
        Long o = (Long) unicorn.reg_read(Unicorn.UC_ARM_REG_R0);
        System.out.println(Long.toHexString(o.intValue()));

    }
}
