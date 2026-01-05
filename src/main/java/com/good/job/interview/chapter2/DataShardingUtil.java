package com.good.job.interview.chapter2;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 海量用户ID数据分片工具类
 * 核心逻辑：基于用户ID哈希取模实现分片，确保相同ID进入同一分片
 */
public class DataShardingUtil {
    // 分片数量，可根据内存大小调整（8G内存建议100-200个分片）
    private static final int SHARD_COUNT = 10;
    // 线程池，利用多核CPU并行分片，提升效率
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());


    public static void main(String[] args) throws IOException {
        // 示例：对多个原始文件进行分片
        DataShardingUtil.shard("D:/raw_data/user_id_1.txt,D:/raw_data/user_id_2.txt", "D:/shard_data");
    }
    /**
     * 对原始用户ID文件进行分片
     * @param rawFilePath 原始文件路径（多个文件用逗号分隔）
     * @param shardDir 分片文件存储目录
     */
    public static void shard(String rawFilePath, String shardDir) throws IOException {
        // 校验目录
        File shardDirFile = new File(shardDir);
        if (!shardDirFile.exists()) {
            if (!shardDirFile.mkdirs()) {
                throw new IOException("创建分片目录失败：" + shardDir);
            }
        }

        // 处理每个原始文件
        String[] rawFiles = rawFilePath.split(",");
        for (String rawFile : rawFiles) {
            File file = new File(rawFile);
            if (!file.exists()) {
                throw new FileNotFoundException("原始文件不存在：" + rawFile);
            }
            // 提交分片任务到线程池
            executor.submit(() -> doShard(file, shardDir));
        }

        // 关闭线程池，等待所有任务完成
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("分片任务中断", e);
            }
        }
    }

    /**
     * 单个文件的分片逻辑
     */
    private static void doShard(File rawFile, String shardDir) {
        try (BufferedReader reader = new BufferedReader(new FileReader(rawFile, StandardCharsets.UTF_8))) {
            String line;
            // 初始化100个分片输出流
            BufferedWriter[] shardWriters = new BufferedWriter[SHARD_COUNT];
            for (int i = 0; i < SHARD_COUNT; i++) {
                File shardFile = new File(shardDir + "/shard_" + i + ".txt");
                shardWriters[i] = new BufferedWriter(new FileWriter(shardFile, StandardCharsets.UTF_8, true));
            }

            // 读取每行用户ID，进行分片写入
            while ((line = reader.readLine()) != null) {
                String userIdStr = line.trim();
                if (userIdStr.isEmpty()) {
                    continue;
                }

                // 关键修改1：不再转long，直接基于String计算分片索引
                // 步骤1：计算String的哈希值（相同字符串哈希值绝对相同）
                int hash = userIdStr.hashCode();
                // 步骤2：处理hash为Integer.MIN_VALUE的边界情况（Math.abs(Integer.MIN_VALUE)仍为负数）
                int shardIndex = (hash & Integer.MAX_VALUE) % SHARD_COUNT;
                // 等价写法（更易理解）：
                // int shardIndex = hash % SHARD_COUNT;
                // if (shardIndex < 0) {
                //     shardIndex += SHARD_COUNT;
                // }

                // 关键修改2：直接写入字符串ID，而非long
                shardWriters[shardIndex].write(userIdStr + "\n");
            }

            // 关闭所有输出流
            for (BufferedWriter writer : shardWriters) {
                writer.close();
            }
        } catch (IOException e) {
            throw new RuntimeException("分片处理失败：" + rawFile.getName(), e);
        }
    }

}
