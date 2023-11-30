package com.demo6.security;

import lombok.SneakyThrows;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootDemoRbacSecurityApplicationTests {

    @Test
    @SneakyThrows
    public void contextLoads() {
        FileInputStream fileInputStream = new FileInputStream("\"D:\\company_resource_group\\中交第二公路勘察设计研究院有限公司\\10号槽钢.rfa\"");
        // 创建一个临时文件
        String version = null;
        outerLoop:
        for (int i = 0; i < 20; i++) {
            ((InputStream) fileInputStream).skip(i);
            byte[] buffer = new byte[2048];
            while (((InputStream) fileInputStream).read(buffer) != -1) {
                String head = new String(buffer, StandardCharsets.UTF_16);
//                Pattern pattern = Pattern.compile(REVIT_SOURCE_VERSION_PATTERN);
//                Matcher matcher = pattern.matcher(head);
//                if (matcher.find()) {
//                    version = matcher.group();
//                    break outerLoop;
//                }
                System.out.println(head);
            }
            ((InputStream) fileInputStream).close();
        }
        System.out.println(version);

    }

}
