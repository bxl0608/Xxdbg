package com.bxlong.xxdbg.android.module;


import com.bxlong.elf.ElfFile;

public class ElfModule {
    private String name;
    private long base;
    private long size;
    private long load_bias_;
    private int ref = 0;
    private boolean isLinked = false;
    private boolean isInit = false;
    private ElfFile elfFile;
    private long nbucket;
    private long nchain;
    private long bucket;
    private long chain;
    private long strtab;
    private long symtab;
    private long plt_rel;
    private long rel;
    private long plt_rel_count;
    private long rel_count;

    public long getPlt_rel_count() {
        return plt_rel_count;
    }

    public void setPlt_rel_count(long plt_rel_count) {
        this.plt_rel_count = plt_rel_count;
    }

    public long getRel_count() {
        return rel_count;
    }

    public void setRel_count(long rel_count) {
        this.rel_count = rel_count;
    }

    public long getPlt_rel() {
        return plt_rel;
    }

    public void setPlt_rel(long plt_rel) {
        this.plt_rel = plt_rel;
    }

    public long getRel() {
        return rel;
    }

    public void setRel(long rel) {
        this.rel = rel;
    }

    public long getStrtab() {
        return strtab;
    }

    public long getSymtab() {
        return symtab;
    }

    public void setSymtab(long symtab) {
        this.symtab = symtab;
    }

    public void setStrtab(long strtab) {
        this.strtab = strtab;
    }

    public long getNbucket() {
        return nbucket;
    }

    public void setNbucket(long nbucket) {
        this.nbucket = nbucket;
    }

    public long getNchain() {
        return nchain;
    }

    public void setNchain(long nchain) {
        this.nchain = nchain;
    }

    public long getBucket() {
        return bucket;
    }

    public void setBucket(long bucket) {
        this.bucket = bucket;
    }

    public long getChain() {
        return chain;
    }

    public void setChain(long chain) {
        this.chain = chain;
    }

    public ElfFile getElfFile() {
        return elfFile;
    }

    public void setElfFile(ElfFile elfFile) {
        this.elfFile = elfFile;
    }

    public boolean isInit() {
        return isInit;
    }

    public void setInit(boolean init) {
        isInit = init;
    }

    public boolean isLinked() {
        return isLinked;
    }

    public void setLinked(boolean linked) {
        isLinked = linked;
    }

    public int getRef() {
        return ref;
    }

    public void setRef(int ref) {
        this.ref = ref;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getBase() {
        return base;
    }

    public void setBase(long base) {
        this.base = base;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getLoad_bias_() {
        return load_bias_;
    }

    public void setLoad_bias_(long load_bias_) {
        this.load_bias_ = load_bias_;
    }
}
