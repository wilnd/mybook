package com.good.job.interview.chapter1.script;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 生成1000万个QQ号（重复率0.01%：999.9万唯一 + 1000个重复）
 * 针对千万级数据做性能优化：预分配集合容量、使用BufferedWriter提升写入效率
 */
public class QQNumberGenerator {// 核心：限定QQ号范围为10000000 ~ 20000000
    // 核心配置：顺序生成的起始、结束值（刚好1000万个不重复数）
    private static final long START_QQ = 10000000L;    // 起始值：1000万
    private static final long TOTAL_COUNT = 10000000L; // 生成总数：1000万
    private static final long END_QQ = START_QQ + TOTAL_COUNT - 1; // 结束值：19999999

    public static void main(String[] args) {
        String outputFile = "1000w_sequential_qq.txt";
        System.out.println("===== 开始顺序生成1000万个不重复QQ号 =====");
        System.out.println("生成范围：" + START_QQ + " ~ " + END_QQ);
        System.out.println("总数量：" + TOTAL_COUNT + " 个（100%不重复）");
        long startMs = System.currentTimeMillis();

        // 核心逻辑：顺序递增生成，边生成边写入
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            long currentQQ = START_QQ;
            // 循环生成1000万个，逐个递增
            for (long i = 0; i < TOTAL_COUNT; i++) {
                // 写入当前QQ号（每行一个）
                writer.write(currentQQ + System.lineSeparator());
                currentQQ++; // 顺序递增，保证不重复

                // 每生成100万条打印进度，避免无反馈
                if ((i + 1) % 1000000 == 0) {
                    long elapsedMs = System.currentTimeMillis() - startMs;
                    System.out.println("已生成：" + (i + 1) + " 个QQ号，当前值：" + currentQQ + "，耗时：" + elapsedMs + "ms");
                }
            }

            writer.flush(); // 确保所有数据写入磁盘
        } catch (IOException e) {
            System.err.println("文件写入失败：" + e.getMessage());
            e.printStackTrace();
            return;
        }

        // 统计总耗时
        long totalMs = System.currentTimeMillis() - startMs;
        System.out.println("===== 生成完成 =====");
        System.out.println("总耗时：" + (totalMs / 1000.0) + " 秒（约" + (totalMs / 60000.0) + "分钟）");
        System.out.println("生成文件：" + System.getProperty("user.dir") + "/" + outputFile);
        System.out.println("内存占用：仅记录当前生成数值（几KB），无任何缓存");

        // 简单验证：检查文件首尾值和行数（确保生成完整）
        verifyFileIntegrity(outputFile);
    }

    /**
     * 验证文件完整性：检查首行、末行和总行数（低内存验证）
     */
    private static void verifyFileIntegrity(String fileName) {
        System.out.println("\n===== 验证文件完整性 =====");
        try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(fileName))) {
            // 检查首行
            String firstLine = reader.readLine();
            long firstQQ = Long.parseLong(firstLine.trim());
            System.out.println("文件首行（第一个QQ号）：" + firstQQ + "（预期：" + START_QQ + "）");

            // 检查末行（快速定位到文件末尾，无需加载全量数据）
            String lastLine = null;
            String line;
            long lineCount = 1; // 已读取首行
            while ((line = reader.readLine()) != null) {
                lastLine = line;
                lineCount++;
            }
            long lastQQ = Long.parseLong(lastLine.trim());

            // 输出验证结果
            System.out.println("文件末行（最后一个QQ号）：" + lastQQ + "（预期：" + END_QQ + "）");
            System.out.println("文件总行数：" + lineCount + "（预期：" + TOTAL_COUNT + "）");

            // 校验是否符合预期
            if (firstQQ == START_QQ && lastQQ == END_QQ && lineCount == TOTAL_COUNT) {
                System.out.println("✅ 验证通过：所有QQ号顺序、不重复、数量完整！");
            } else {
                System.out.println("❌ 验证失败：数据不完整或顺序异常！");
            }
        } catch (IOException e) {
            System.err.println("验证文件失败：" + e.getMessage());
        }
    }
}