package com.good.job.interview.chapter1.main;

import com.good.job.interview.MemoryMonitor;
import com.good.job.interview.chapter1.QQNumberDeduplicator;

import java.io.IOException;

public class MainRunner {
    public static void main(String[] args) {

        // 手动输入文件路径
        String inputFile = "/Users/chenhao/code/personal/mybook/1000w_sequential_qq.txt";
        String outputFile = "/Users/chenhao/code/personal/mybook/target"+System.currentTimeMillis()+".txt";
        QQNumberDeduplicator deduplicator = new QQNumberDeduplicator();
        try {
            MemoryMonitor.start();
            MemoryMonitor.printMemorySnapshot("QQ去重程序启动");
            deduplicator.deduplicate(inputFile, outputFile);
            MemoryMonitor.printMemorySnapshot("QQ去重操作结束");
        } catch (IOException e) {
            System.err.println("文件操作异常: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
