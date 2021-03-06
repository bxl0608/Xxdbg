package com.bxlong.xxdbg.memory;

import com.bxlong.xxdbg.android.emulater.IEmulate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class Pointer {

    private IEmulate emulate;
    public long peer;
    private long pointerSize;
    private int size;

    public int getSize() {
        return size;
    }

    public Pointer setSize(int size) {
        this.size = size;
        return this;
    }

    public Pointer(IEmulate emulate, long peer) {
        this.emulate = emulate;
        this.peer = peer;
        pointerSize = emulate.is32Bit() ? 4 : 8;
    }

    public Pointer getPointer(long offset){
        String s = Integer.toHexString(getInt(offset));
        return Pointer.point(emulate, Long.parseLong(s,16));
    }

    public void write(long offset, byte[] buf, int index, int length) {
//        if (size > 0) {
//            if (offset < 0) {
//                throw new IllegalArgumentException();
//            }
//
//            if (size - offset < length) {
//                throw new InvalidMemoryAccessException();
//            }
//        }
        byte[] data;
        if (index == 0 && buf.length == length) {
            data = buf;
        } else {
            data = new byte[length];
            System.arraycopy(buf, index, data, 0, length);
        }
        long addr = peer + offset;
        emulate.getBackend().mem_write(addr, data);
    }

    public byte[] getByteArray(long offset, int arraySize) {
//        if (size > 0 && offset + arraySize > size) {
//            throw new InvalidMemoryAccessException();
//        }
//
//        if (arraySize < 0 || arraySize >= 0x7ffffff) {
//            throw new InvalidMemoryAccessException("Invalid array size: " + arraySize);
//        }
        return emulate.getBackend().mem_read(peer + offset, arraySize);
    }

    public ByteBuffer getByteBuffer(long offset, long length) {
        return ByteBuffer.wrap(getByteArray(offset, (int) length)).order(ByteOrder.LITTLE_ENDIAN);
    }

    private ByteBuffer allocateBuffer(int size) {
        return ByteBuffer.allocate(size).order(ByteOrder.LITTLE_ENDIAN);
    }

    public short getShort(long offset){
        return getByteBuffer(offset, 2).getShort();
    }

    public int getInt(long offset) {
        return getByteBuffer(offset, 4).getInt();
    }

    public long getLong(long offset) {
        return getByteBuffer(offset, 8).getLong();
    }

    public void setLong(long offset, long value) {
        write(offset, allocateBuffer(8).putLong(value).array(), 0, 8);
    }

    public void setInt(long offset, int value) {
        write(offset, allocateBuffer(4).putInt(value).array(), 0, 4);
    }

    public static long numberToAddress(IEmulate emulator, Number number) {
        if (emulator.is64Bit()) {
            return number.longValue();
        } else {
            return number.intValue() & 0xffffffffL;
        }
    }

    public static Pointer point(IEmulate emulate, long peer){
        return new Pointer(emulate, peer);
    }


    public static Pointer point(IEmulate emulator, Number number) {
        return point(emulator, numberToAddress(emulator,number));
    }

    public static Pointer register(IEmulate emulator, int reg) {
        return point(emulator, emulator.getBackend().reg_read(reg));
    }

    public void setPointer(long offset, Pointer aim) {
        long value;
        if (aim == null) {
            value = 0;
        } else {
            value = aim.peer;
        }

        if (pointerSize == 4) {
            setInt(offset, (int) value);
        } else {
            setLong(offset, value);
        }
    }

    public Pointer share(long offset) {
        return share(offset,0);
    }

    public Pointer share(long offset, long sz) {
        if (offset == 0L) {
            return this;
        }

        return new Pointer(emulate, peer + offset);
    }

    public String getString(long offset){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] bytes;
        int i = 0;
        do {
            bytes = emulate.getBackend().mem_read(peer + offset+i, 1);
            try {
                if (bytes[0] != 0){
                    bos.write(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            i++;
        }while (bytes[0] != 0);
        return new String(bos.toByteArray());
    }

    public void setString(long offset, String s){
        byte[] bytes = s.getBytes();
        write(offset, bytes, 0, bytes.length);
    }




    @Override
    public String toString() {
        return "xxdbg@0x"+Long.toHexString(peer);
    }
}
