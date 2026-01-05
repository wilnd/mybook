package com.good.job.interview.chapter2.script;

import com.good.job.interview.MemoryMonitor;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;

/**
 * 20位带指定前缀的用户ID生成工具
 * 支持生成1亿级ID，分批写入文件避免内存溢出，集成内存监控
 */
public class UserIdGenerator {
    // 目标生成数量：1亿
    private static final long TOTAL_COUNT = 100_000_000L;
    // 每批写入数量（100万/批，平衡性能和内存）
    private static final long BATCH_SIZE = 1_000_000L;

    // 测试main方法
    public static void main(String[] args) {
        // 自定义前缀（示例："8888"，可修改为你需要的前缀）
        String customPrefix = "8888";
        // 输出文件路径（建议用绝对路径，如/Users/xxx/ids_20bit.txt）
        String outputPath = "/Users/chenhao/code/personal/mybook/user_ids_20bit_" + System.currentTimeMillis() + ".txt";

        // 生成1亿个20位ID
        generate20BitUserId(customPrefix, outputPath);
    }

    /**
     * 生成20位带指定前缀的用户ID
     * @param prefix 指定前缀（如"8888"，长度必须≤20）
     * @param outputFilePath 生成的ID写入的文件路径
     */
    public static void generate20BitUserId(String prefix, String outputFilePath) {
        // 1. 参数校验：前缀长度不能超过20位
        if (prefix == null || prefix.length() > 20) {
            throw new IllegalArgumentException("前缀不能为空，且长度不能超过20位！");
        }

        // 2. 计算后缀需要的长度（20 - 前缀长度）
        int suffixLength = 20 - prefix.length();
        if (suffixLength < 1) {
            throw new IllegalArgumentException("前缀长度不能等于20位（需留至少1位给后缀保证唯一性）！");
        }

        // 3. 创建数字格式化器：补前导零，保证后缀长度固定
        DecimalFormat suffixFormatter = new DecimalFormat();
        suffixFormatter.setGroupingUsed(false); // 禁用千分位分隔符
        suffixFormatter.setMinimumIntegerDigits(suffixLength); // 最少位数=后缀长度，不足补0

        // 4. 初始化文件写入流（缓冲写入提升效率）
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(outputFilePath), StandardCharsets.UTF_8)
        )) {
            // 启动内存监控（每2秒监测一次，适配大数量生成）
            MemoryMonitor.start(2000);
            MemoryMonitor.printMemorySnapshot("ID生成开始");

            long generatedCount = 0L; // 已生成数量
            long batchNum = 0L; // 批次号

            System.out.println("开始生成1亿个20位用户ID，前缀：" + prefix + "，输出文件：" + outputFilePath);

            // 5. 分批生成+写入（避免1亿数据全放内存）
            while (generatedCount < TOTAL_COUNT) {
                batchNum++;
                long currentBatchSize = Math.min(BATCH_SIZE, TOTAL_COUNT - generatedCount);

                // 生成当前批次的ID并写入
                for (long i = 0; i < currentBatchSize; i++) {
                    long suffixNum = generatedCount + i + 1; // 后缀自增数（从1开始）
                    String suffix = suffixFormatter.format(suffixNum); // 补前导零
                    String userId = prefix + suffix; // 拼接20位ID

                    // 写入文件（每行一个ID）
                    writer.write(userId);
                    writer.newLine();
                }

                // 刷新缓冲区，确保数据写入磁盘
                writer.flush();

                // 更新已生成数量
                generatedCount += currentBatchSize;

                // 打印进度+内存快照
                double progress = (double) generatedCount / TOTAL_COUNT * 100;
                System.out.printf("✅ 批次%d完成，累计生成%d条ID，进度：%.2f%%%n",
                        batchNum, generatedCount, progress);
                MemoryMonitor.printMemorySnapshot("批次" + batchNum + "完成");
            }

            MemoryMonitor.printMemorySnapshot("ID生成完成");
            System.out.println("🎉 1亿个20位用户ID生成完成！文件路径：" + outputFilePath);

        } catch (Exception e) {
            System.err.println("❌ ID生成失败：" + e.getMessage());
            e.printStackTrace();
        } finally {
            // 停止内存监控
            MemoryMonitor.stop();
        }
    }


}
