package com.good.job.interview.chapter2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 分片UV统计工具类
 * 核心逻辑：MMap读取分片文件 + 布隆过滤器去重统计
 */
public class ShardUVStatisticsUtil {
    // 每个分片的预计最大用户ID数量（1亿）
    private static final long EXPECTED_INSERTIONS_PER_SHARD = 100_000_000L;
    // 可接受的误判率（1%）
    private static final double FALSE_POSITIVE_PROBABILITY = 0.01;

    /**
     * 统计单个分片的UV数量
     * @param shardFilePath 分片文件路径
     * @return 分片UV数量（含可能的误判）
     */
    public static long statisticsShardUV(String shardFilePath) throws IOException {
        File shardFile = new File(shardFilePath);
        if (!shardFile.exists()) {
            throw new FileNotFoundException("分片文件不存在：" + shardFilePath);
        }

        // 初始化布隆过滤器（适配1亿数据量，1%误判率）
        BloomFilter bloomFilter = new BloomFilter(EXPECTED_INSERTIONS_PER_SHARD, FALSE_POSITIVE_PROBABILITY);
        // 原子类计数（确保线程安全，支持后续并发统计）
        AtomicLong uvCount = new AtomicLong(0);

        // 使用MMap映射文件，提升IO效率
        try (RandomAccessFile raf = new RandomAccessFile(shardFile, "r");
             FileChannel channel = raf.getChannel()) {
            MappedByteBuffer mbb = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());

            // 读取MMap中的数据（按行解析用户ID）
            StringBuilder sb = new StringBuilder();
            while (mbb.hasRemaining()) {
                byte b = mbb.get();
                if (b == '\n' || b == '\r') {
                    // 解析一行用户ID
                    String userIdStr = sb.toString().trim();
                    if (!userIdStr.isEmpty()) {
                        long userId = Long.parseLong(userIdStr);
                        // 布隆过滤器判断：不存在则计数+1并添加到过滤器
                        if (!bloomFilter.contains(userId)) {
                            uvCount.incrementAndGet();
                            bloomFilter.add(userId);
                        }
                    }
                    sb.setLength(0);
                } else {
                    sb.append((char) b);
                }
            }

            // 处理最后一行（无换行符的情况）
            if (sb.length() > 0) {
                String userIdStr = sb.toString().trim();
                if (!userIdStr.isEmpty()) {
                    long userId = Long.parseLong(userIdStr);
                    if (!bloomFilter.contains(userId)) {
                        uvCount.incrementAndGet();
                        bloomFilter.add(userId);
                    }
                }
            }
        }

        System.out.println("分片文件：" + shardFilePath + "，UV数量：" + uvCount.get() + "，实际误判率：" + bloomFilter.getActualFalsePositiveProbability());
        return uvCount.get();
    }
}
