package com.good.job.interview.chapter1.main;

import com.good.job.interview.chapter1.QQNumberDeduplicator;

import java.io.IOException;
import java.util.Scanner;

public class MainRunner {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 手动输入文件路径
        String inputFile = "/Users/chenhao/code/personal/mybook/1000w_sequential_qq.txt";
        String outputFile = "/Users/chenhao/code/personal/mybook/target"+System.currentTimeMillis()+".txt";
        System.out.print("请选择去重方法 (1-默认方法, 2-优化方法): ");
        QQNumberDeduplicator deduplicator = new QQNumberDeduplicator();

        try {
            System.out.println("去重开始~");
            deduplicator.deduplicate(inputFile, outputFile);
            System.out.println("去重完成！");
        } catch (IOException e) {
            System.err.println("文件操作异常: " + e.getMessage());
            e.printStackTrace();
        } finally {
            scanner.close();
        }
    }
}
