package com.demo;

import com.alibaba.excel.EasyExcel;

import java.util.Collection;
import java.util.List;

public class Main {
    public static final String path = "D:\\";
    public static void main(String[] args) {
        long l = System.currentTimeMillis();
        String fileName = path + "write" + System.currentTimeMillis() + ".xlsx";
        // 写
        EasyExcel.write(fileName, DemoData.class).sheet("sheet1").doWrite(data());
        // 读
        EasyExcel.read(fileName, DemoData.class,new DemoDataListener()).sheet().doRead();
        long l1 = System.currentTimeMillis();
        System.out.println(l1-l);
    }

    private static Collection<?> data() {
        return List.of(new DemoData("张三",19),new DemoData("李四",32));
    }
}