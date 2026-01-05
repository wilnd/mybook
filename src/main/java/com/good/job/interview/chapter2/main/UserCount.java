package com.good.job.interview.chapter2.main;

import com.good.job.interview.MemoryMonitor;
import com.good.job.interview.chapter2.DataShardingUtil;
import com.good.job.interview.chapter2.TotalUVMergeUtil;

import java.io.IOException;


public class UserCount {
    // ===================== 可配置参数（用户只需修改这部分）=====================
    /** 原始用户ID文件路径（多个文件用逗号分隔） */
    private static final String RAW_FILE_PATH = "/Users/chenhao/code/personal/mybook/data/user_ids_20bit_1767621685385.txt";
    /** 分片文件存储目录（会自动创建） */
    private static final String SHARD_DIR = "/Users/chenhao/code/personal/mybook/data/shard";
    /** 内存监控间隔（毫秒） */
    private static final long MEMORY_MONITOR_INTERVAL = 2000;
    // =========================================================================

    public static void main(String[] args) {
        // 1. 初始化：启动内存监控，打印流程开始信息
        MemoryMonitor.start(MEMORY_MONITOR_INTERVAL);
        MemoryMonitor.printMemorySnapshot("UV统计全流程开始");
        long startTime = System.currentTimeMillis();

        try {
            // 2. 第一步：原始文件分片
            System.out.println("\n========== 第一步：开始分片原始用户ID文件 ==========");
            MemoryMonitor.printMemorySnapshot("分片操作开始");
            DataShardingUtil.shard(RAW_FILE_PATH, SHARD_DIR);
            System.out.println("✅ 分片操作完成，分片文件存储目录：" + SHARD_DIR);
            MemoryMonitor.printMemorySnapshot("分片操作完成");

            // 3. 第二步：合并所有分片UV，得到总UV（内部会并发统计每个分片）
            System.out.println("\n========== 第二步：开始统计并合并所有分片UV ==========");
            MemoryMonitor.printMemorySnapshot("UV统计合并开始");
            long totalUV = TotalUVMergeUtil.mergeTotalUV(SHARD_DIR);
            MemoryMonitor.printMemorySnapshot("UV统计合并完成");

            // 4. 输出最终结果
            long totalTime = (System.currentTimeMillis() - startTime) / 1000;
            System.out.println("\n========== UV统计全流程完成 ==========");
            System.out.println("📊 原始文件路径：" + RAW_FILE_PATH);
            System.out.println("📊 分片目录：" + SHARD_DIR);
            System.out.println("📊 总UV数量：" + totalUV);
            System.out.println("⏱️  总耗时：" + totalTime + " 秒");

        } catch (IOException e) {
            System.err.println("❌ IO异常：" + e.getMessage());
            e.printStackTrace();
        } catch (InterruptedException e) {
            System.err.println("❌ 线程中断异常：" + e.getMessage());
            Thread.currentThread().interrupt();
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("❌ UV统计流程异常：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 5. 收尾：停止内存监控
            MemoryMonitor.printMemorySnapshot("UV统计全流程结束");
            MemoryMonitor.stop();
        }
    }
}
