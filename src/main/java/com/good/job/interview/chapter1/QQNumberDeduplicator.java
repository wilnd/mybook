package com.good.job.interview.chapter1;

import java.io.*;

/**
 * 40亿QQ号去重解决方案
 *
 * 假设QQ号范围为0-2^32-1（约42亿），使用BitMap实现
 * 内存占用：2^32 bit = 512MB < 1GB，满足约束条件
 */
public class QQNumberDeduplicator {

    /**
     * QQ号的最大值（32位整数最大值）
     */
    private static final long MAX_QQ_NUM = 2147483647L; // 2^31 - 1

    /**
     * 使用BitMap进行去重
     *
     * @param inputFile 输入的QQ号文件（每行一个QQ号）
     * @param outputFile 输出去重后的QQ号文件
     * @throws IOException IO异常
     */
    public void deduplicate(String inputFile, String outputFile) throws IOException {
        // 步骤1：初始化BitMap
        System.out.println("初始化BitMap，最大支持数字：" + MAX_QQ_NUM);
        BitMap bitMap = new BitMap(MAX_QQ_NUM);
        System.out.println("BitMap内存占用：" + (bitMap.getMemorySize() / 1024 / 1024) + "MB");

        // 步骤2：第一遍遍历，标记所有出现的QQ号
        System.out.println("开始第一遍遍历，标记QQ号...");
        try (BufferedReader reader = new BufferedReader(
                new FileReader(inputFile))) {
            String line;
            long count = 0;
            while ((line = reader.readLine()) != null) {
                long qqNum = Long.parseLong(line.trim());
                bitMap.set(qqNum);
                count++;
                if (count % 100000000 == 0) {
                    System.out.println("已处理：" + count + " 个QQ号");
                }
            }
            System.out.println("第一遍遍历完成，共处理：" + count + " 个QQ号");
        }

        // 步骤3：第二遍遍历，输出去重后的QQ号
        System.out.println("开始第二遍遍历，输出去重后的QQ号...");
        try (BufferedReader reader = new BufferedReader(
                new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(
                     new FileWriter(outputFile))) {

            String line;
            long outputCount = 0;
            while ((line = reader.readLine()) != null) {
                long qqNum = Long.parseLong(line.trim());
                // 只输出第一次出现的QQ号（通过判断bitMap中是否存在，输出后清除标记）
                if (bitMap.get(qqNum)) {
                    writer.write(String.valueOf(qqNum));
                    writer.newLine();
                    bitMap.clear(qqNum); // 清除标记，避免重复输出
                    outputCount++;
                }
            }
            System.out.println("去重完成，输出了：" + outputCount + " 个唯一QQ号");
        }
    }

    /**
     * 优化版本：使用第二个BitMap记录已输出的QQ号
     *
     * 说明：虽然会额外占用512MB内存（总计约1GB），但逻辑更清晰
     * 如果去重后的QQ号数量远小于原始数量（如只有1亿个唯一QQ号），
     * 可以考虑使用HashSet记录已输出的QQ号，内存占用会更小
     */
    public void deduplicateOptimized(String inputFile, String outputFile) throws IOException {
        BitMap bitMap = new BitMap(MAX_QQ_NUM);

        // 第一遍：标记所有出现的QQ号
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                bitMap.set(Long.parseLong(line.trim()));
            }
        }

        // 第二遍：输出去重结果，使用第二个BitMap记录已输出的QQ号
        BitMap outputBitMap = new BitMap(MAX_QQ_NUM); // 额外512MB，总计约1GB

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            String line;
            long outputCount = 0;
            while ((line = reader.readLine()) != null) {
                long qqNum = Long.parseLong(line.trim());
                // 判断是否已存在且未输出过
                if (bitMap.get(qqNum) && !outputBitMap.get(qqNum)) {
                    writer.write(String.valueOf(qqNum));
                    writer.newLine();
                    outputBitMap.set(qqNum); // 标记为已输出
                    outputCount++;
                }
            }
            System.out.println("去重完成，输出了：" + outputCount + " 个唯一QQ号");
        }
    }
}