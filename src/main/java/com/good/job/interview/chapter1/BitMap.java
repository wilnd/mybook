package com.good.job.interview.chapter1;

public class BitMap {
    /**
     * 使用byte数组存储bit位
     * 每个byte有8个bit，可存储8个数字的标记
     */
    private byte[] bits;

    /**
     * 位图能表示的最大数字
     */
    private long maxNum;

    /**
     * 构造函数
     *
     * @param maxNum 需要去重的最大数字值
     */
    public BitMap(long maxNum) {
        this.maxNum = maxNum;
        // 计算需要的byte数组大小：向上取整
        // 例如：maxNum=40亿，需要 (40亿 / 8) + 1 个byte
        long byteSize = (maxNum >> 3) + 1;
        if (byteSize > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("BitMap size too large: " + byteSize);
        }
        this.bits = new byte[(int) byteSize];
    }

    /**
     * 将指定数字标记为存在
     *
     * @param num 待标记的数字
     */
    public void set(long num) {
        if (num < 0 || num > maxNum) {
            return; // 超出范围，忽略
        }
        // 计算该数字在byte数组中的位置
        int byteIndex = (int) (num >> 3); // 等价于 num / 8
        // 计算该数字在byte中的bit位置（0-7）
        int bitIndex = (int) (num & 0x07); // 等价于 num % 8

        // 将对应bit位置为1：使用位运算 OR
        // 例如：bitIndex=3，则 1 << 3 = 00001000
        bits[byteIndex] |= (1 << bitIndex);
    }

    /**
     * 判断指定数字是否存在
     *
     * @param num 待查询的数字
     * @return true表示存在，false表示不存在
     */
    public boolean get(long num) {
        if (num < 0 || num > maxNum) {
            return false;
        }
        int byteIndex = (int) (num >> 3);
        int bitIndex = (int) (num & 0x07);

        // 检查对应bit位是否为1：使用位运算 AND
        // 例如：bits[byteIndex] = 00001000, bitIndex=3
        // 则 (bits[byteIndex] & (1 << 3)) != 0，返回true
        return (bits[byteIndex] & (1 << bitIndex)) != 0;
    }

    /**
     * 清除指定数字的标记（将对应bit位置为0）
     *
     * @param num 待清除的数字
     */
    public void clear(long num) {
        if (num < 0 || num > maxNum) {
            return;
        }
        int byteIndex = (int) (num >> 3);
        int bitIndex = (int) (num & 0x07);

        // 将对应bit位置为0：使用位运算 AND NOT
        // 例如：bits[byteIndex] = 00001111, bitIndex=3
        // 则 bits[byteIndex] & ~(1 << 3) = 00001111 & 11110111 = 00000111
        bits[byteIndex] &= ~(1 << bitIndex);
    }

    /**
     * 获取当前位图占用的内存大小（字节）
     */
    public long getMemorySize() {
        return bits.length;
    }
}
