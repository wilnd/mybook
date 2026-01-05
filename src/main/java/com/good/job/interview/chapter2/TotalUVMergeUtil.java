package com.good.job.interview.chapter2;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 总UV结果合并工具类
 */
public class TotalUVMergeUtil {
    private static final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    /**
     * 合并所有分片的UV结果，得到总UV
     * @param shardDir 分片文件存储目录
     * @return 总UV数量
     */
    public static long mergeTotalUV(String shardDir) throws InterruptedException {
        File shardDirFile = new File(shardDir);
        if (!shardDirFile.exists() || !shardDirFile.isDirectory()) {
            throw new IllegalArgumentException("分片目录不存在或不是目录：" + shardDir);
        }

        File[] shardFiles = shardDirFile.listFiles((dir, name) -> name.startsWith("shard_") && name.endsWith(".txt"));
        if (shardFiles == null || shardFiles.length == 0) {
            throw new IllegalArgumentException("分片目录中无分片文件：" + shardDir);
        }

        // 原子类存储总UV（支持并发累加）
        AtomicLong totalUV = new AtomicLong(0);

        // 并发处理所有分片的UV统计并累加
        for (File shardFile : shardFiles) {
            executor.submit(() -> {
                try {
                    long shardUV = ShardUVStatisticsUtil.statisticsShardUV(shardFile.getAbsolutePath());
                    totalUV.addAndGet(shardUV);
                } catch (Exception e) {
                    throw new RuntimeException("分片UV统计失败：" + shardFile.getName(), e);
                }
            });
        }

        // 关闭线程池，等待所有任务完成
        executor.shutdown();
        if (!executor.awaitTermination(1, TimeUnit.HOURS)) {
            executor.shutdownNow();
            throw new RuntimeException("UV合并任务超时");
        }

        return totalUV.get();
    }

}
