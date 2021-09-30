package com.bxlong.xxdbg.backend.unicorn;

import com.bxlong.xxdbg.android.linker.Linker;
import com.bxlong.xxdbg.backend.BackendException;
import com.bxlong.xxdbg.backend.IBackend;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import unicorn.*;

public class UnicornBackend implements IBackend {
    private static final Logger logger = LoggerFactory.getLogger(UnicornBackend.class);
    private Unicorn unicorn;


    public UnicornBackend(boolean is632Bit) {
        unicorn = new Unicorn(is632Bit ? Unicorn.UC_ARCH_ARM : Unicorn.UC_ARCH_ARM64, Unicorn.UC_MODE_THUMB);
    }

    public Number reg_read(int regId) throws BackendException {
        return (Long) unicorn.reg_read(regId);
    }

    public byte[] reg_read_vector(int regId) throws BackendException {
        return new byte[0];
    }

    public void reg_write_vector(int regId, byte[] vector) throws BackendException {

    }

    public void reg_write(int regId, Number value) throws BackendException {
        unicorn.reg_write(regId, value);
    }

    public byte[] mem_read(long address, long size) throws BackendException {
        return unicorn.mem_read(address, size);
    }

    public void mem_write(long address, byte[] bytes) throws BackendException {
        try {
            unicorn.mem_write(address, bytes);
        }catch (Exception e){
            logger.debug(String.format("unicorn backend mem_write exception address: 0x%x",address));
        }

    }

    public void mem_map(long address, long size, int perms) throws BackendException {
        logger.debug(String.format("unicorn backend mem_map address: 0x%x, size: 0x%x, perms: %d",address,size,perms));
        unicorn.mem_map(address, size, perms);
    }

    public void mem_protect(long address, long size, int perms) throws BackendException {
        logger.debug(String.format("unicorn backend mem_protect address: 0x%x, size: 0x%x, perms: %d",address,size,perms));
        unicorn.mem_protect(address, size, perms);
    }

    public void mem_unmap(long address, long size) throws BackendException {
        unicorn.mem_unmap(address, size);
    }

    public boolean removeBreakPoint(long address) {
        return false;
    }

    public void setSingleStep(int singleStep) {
        throw new BackendException("Unsupported");
    }

    public void setFastDebug(boolean fastDebug) {
        throw new BackendException("Unsupported");
    }

    public void hook_add_new(CodeHook callback, long begin, long end, Object user_data) throws BackendException {
        unicorn.hook_add_new(callback, begin, end, user_data);
    }

    public void debugger_add(DebugHook callback, long begin, long end, Object user_data) throws BackendException {
        unicorn.debugger_add(callback, begin, end, user_data);
    }

    public void hook_add_new(ReadHook callback, long begin, long end, Object user_data) throws BackendException {
        unicorn.hook_add_new(callback, begin, end, user_data);
    }

    public void hook_add_new(WriteHook callback, long begin, long end, Object user_data) throws BackendException {
        unicorn.hook_add_new(callback, begin, end, user_data);
    }

    public void hook_add_new(EventMemHook callback, int type, Object user_data) throws BackendException {
        unicorn.hook_add_new(callback, type, user_data);
    }

    public void hook_add_new(InterruptHook callback, Object user_data) throws BackendException {
        unicorn.hook_add_new(callback, user_data);
    }

    public void hook_add_new(BlockHook callback, long begin, long end, Object user_data) throws BackendException {
        unicorn.hook_add_new(callback, begin, end, user_data);
    }

    public void emu_start(long begin, long until, long timeout, long count) throws BackendException {
        unicorn.emu_start(begin, until, timeout, count);
    }

    public void emu_stop() throws BackendException {
        unicorn.emu_stop();
    }

    public void destroy() throws BackendException {
        unicorn.closeAll();
    }

    public void context_restore(long context) {

    }

    public void context_save(long context) {

    }

    public long context_alloc() {
        return 0;
    }

    public int getPageSize() {
        return 0;
    }
}
