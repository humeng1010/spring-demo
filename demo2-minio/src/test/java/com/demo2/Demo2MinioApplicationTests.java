package com.demo2;

import cn.hutool.core.util.StrUtil;
import com.demo2.config.MinioTemplate;
import io.minio.*;
import lombok.Cleanup;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.io.FileInputStream;
import java.util.Date;

@SpringBootTest
class Demo2MinioApplicationTests {

	@Resource
	private MinioClient minioClient;

	@Resource
	private MinioTemplate minioTemplate;

	@Test
	@SneakyThrows
	public void testCreateBucket(){
		boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("demo01").build());
		if (found) {
			System.out.println("demo01-bucket exists");
		} else {
			System.out.println("demo01-bucket does not exist");
			minioClient.makeBucket(MakeBucketArgs.builder().bucket("demo01").build());
			System.out.println("demo01-bucket created");
		}
	}

	public static final String PATH = "D:\\sources\\";
	public static final String fileName = "棒球棍igs格式模型_77592.rar";

	@Test
	@SneakyThrows
	public void testPutObject(){
		@Cleanup FileInputStream fileInputStream = new FileInputStream(PATH + fileName);
		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		minioClient.putObject(PutObjectArgs.builder()
				.bucket("demo01").object(new Date().getDate()+ StrUtil.SLASH+ fileName).stream(fileInputStream, fileInputStream.available(), -1).build());
		stopWatch.stop();
		System.out.println(stopWatch.getTotalTimeMillis());
	}

}
