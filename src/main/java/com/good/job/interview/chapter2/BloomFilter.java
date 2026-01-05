package com.good.job.interview.chapter2;

import java.util.BitSet;

/**
 * 自定义布隆过滤器（适配64位用户ID）
 * 核心原理：通过多个独立哈希函数将数据映射到位数组，实现高效去重判断
 */
public class BloomFilter {
    // 位数组（核心存储结构）
    private final BitSet bitSet;
    // 位数组大小
    private final int bitSetSize;
    // 哈希函数数量
    private final int hashFunctionCount;
    // 预计插入数据量
    private final long expectedInsertions;
    // 可接受的误判率
    private final double falsePositiveProbability;

    /**
     * 构造函数：根据预计插入量和误判率计算位数组大小和哈希函数数量
     * @param expectedInsertions 预计插入数据量
     * @param falsePositiveProbability 可接受的误判率（如0.01表示1%）
     */
    public BloomFilter(long expectedInsertions, double falsePositiveProbability) {
        this.expectedInsertions = expectedInsertions;
        this.falsePositiveProbability = falsePositiveProbability;

        // 计算位数组大小：m = -n * ln(p) / (ln(2))²
        this.bitSetSize = calculateBitSetSize(expectedInsertions, falsePositiveProbability);
        // 计算哈希函数数量：k = m * ln(2) / n
        this.hashFunctionCount = calculateHashFunctionCount(bitSetSize, expectedInsertions);

        this.bitSet = new BitSet(bitSetSize);
    }

    /**
     * 计算位数组大小
     */
    private int calculateBitSetSize(long n, double p) {
        if (p <= 0) {
            throw new IllegalArgumentException("误判率必须大于0");
        }
        return (int) (-n * Math.log(p) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 计算哈希函数数量
     */
    private int calculateHashFunctionCount(int m, long n) {
        return Math.max(1, (int) (m * Math.log(2) / n));
    }

    /**
     * 向布隆过滤器中添加数据（用户ID）
     */
    public void add(long userId) {
        long[] hashes = calculateHashes(userId);
        for (long hash : hashes) {
            // 将哈希值映射到位数组索引（确保非负）
            int index = (int) (hash % bitSetSize);
            if (index < 0) {
                index += bitSetSize;
            }
            bitSet.set(index, true);
        }
    }

    /**
     * 判断用户ID是否存在（可能存在误判）
     */
    public boolean contains(long userId) {
        long[] hashes = calculateHashes(userId);
        for (long hash : hashes) {
            int index = (int) (hash % bitSetSize);
            if (index < 0) {
                index += bitSetSize;
            }
            // 只要有一个位为false，说明一定不存在
            if (!bitSet.get(index)) {
                return false;
            }
        }
        // 所有位都为true，说明可能存在（存在误判）
        return true;
    }

    /**
     * 计算多个哈希值（基于MurmurHash算法，保证哈希值的均匀分布）
     */
    private long[] calculateHashes(long userId) {
        long[] hashes = new long[hashFunctionCount];
        // 第一个哈希值（基于用户ID直接计算）
        long hash1 = murmurHash64A(userId, 0x12345678L);
        // 第二个哈希值（用于生成后续多个哈希值）
        long hash2 = murmurHash64A(userId, 0x87654321L);

        for (int i = 0; i < hashFunctionCount; i++) {
            hashes[i] = hash1 + i * hash2;
        }
        return hashes;
    }

    /**
     * MurmurHash64A算法：高效的非加密哈希算法，适合布隆过滤器
     */
    private long murmurHash64A(long data, long seed) {
        final long m = 0xc6a4a7935bd1e995L;
        final int r = 47;

        long h = seed ^ (8 * m);
        long k = data;

        k *= m;
        k ^= k >>> r;
        k *= m;

        h ^= k;
        h *= m;

        h ^= h >>> r;
        h *= m;
        h ^= h >>> r;

        return h;
    }

    /**
     * 获取当前布隆过滤器的误判率（实际误判率，可能与预期有差异）
     */
    public double getActualFalsePositiveProbability() {
        // 实际误判率公式：(1 - e^(-k*n/m))^k
        return Math.pow(1 - Math.exp(-hashFunctionCount * expectedInsertions / bitSetSize), hashFunctionCount);
    }
}