package com.good.job.interview;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 内存监控插件（可复用工具类）
 * 多个main方法可直接调用，一键启用/停止内存监控
 */
public class MemoryMonitor {
    // 定时器实例（用于实时监测）
    private static Timer monitorTimer;
    // 标记是否已启动监控（避免重复启动）
    private static final AtomicBoolean isRunning = new AtomicBoolean(false);
    // 默认监测间隔：1秒（1000毫秒）
    private static final long DEFAULT_INTERVAL = 1000;

    /**
     * 启动内存实时监控（使用默认间隔：1秒）
     */
    public static void start() {
        start(DEFAULT_INTERVAL);
    }

    /**
     * 启动内存实时监控（自定义监测间隔）
     * @param interval 监测间隔（毫秒），比如500=0.5秒，2000=2秒
     */
    public static void start(long interval) {
        // 防止重复启动
        if (isRunning.compareAndSet(false, true)) {
            monitorTimer = new Timer("Memory-Monitor-Timer", true);
            monitorTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    printMemoryInfo();
                }
            }, 0, interval);
            System.out.println("✅ 内存监控插件已启动，监测间隔：" + interval + "毫秒");
        } else {
            System.out.println("⚠️ 内存监控插件已在运行中，无需重复启动");
        }
    }

    /**
     * 停止内存实时监控
     */
    public static void stop() {
        // 防止重复停止
        if (isRunning.compareAndSet(true, false)) {
            if (monitorTimer != null) {
                monitorTimer.cancel();
                monitorTimer = null;
            }
            System.out.println("🛑 内存监控插件已停止");
        } else {
            System.out.println("⚠️ 内存监控插件未运行，无需停止");
        }
    }

    /**
     * 手动打印一次当前程序内存信息（非实时，按需调用）
     */
    public static void printMemoryInfo() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        // 堆内存（程序核心业务内存）
        MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
        long heapUsed = heapUsage.getUsed() / (1024 * 1024);
        long heapMax = heapUsage.getMax() / (1024 * 1024);
        // 非堆内存（JVM自身内存）
        MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        long nonHeapUsed = nonHeapUsage.getUsed() / (1024 * 1024);
        // 总已用内存
        long totalUsed = heapUsed + nonHeapUsed;

        // 格式化输出（带时间戳，便于排查）
        String timestamp = java.time.LocalTime.now().toString();
        System.out.printf("[%s] 📊 内存监控 | 堆内存已用: %d MB (最大: %d MB) | 非堆内存已用: %d MB | 总计: %d MB%n",
                timestamp, heapUsed, heapMax, nonHeapUsed, totalUsed);
    }

    /**
     * 打印程序关键节点的内存快照（比如启动/结束时）
     * @param nodeName 节点名称（如：程序启动、去重开始、去重结束）
     */
    public static void printMemorySnapshot(String nodeName) {
        System.out.println("\n===== " + nodeName + " - 内存快照 =====");
        printMemoryInfo();
        System.out.println("===============================");
    }
}