package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.config.MinioProperties;
import com.atguigu.lease.web.admin.service.FileService;
import io.minio.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {


    @Autowired
    private MinioClient client;

    @Autowired
    private MinioProperties properties;

    //上传到minio服务器里面
    @Override
    public String upload(MultipartFile file) {
        try {
            //判断bucket是否存在
            boolean bucketExists =
                    client.bucketExists(BucketExistsArgs.builder()
                            .bucket(properties.getBucketname())
                            .build());
            //bucket不存在，创建
            if (!bucketExists) {
                //创建bucket
                client.makeBucket(MakeBucketArgs.builder()
                        .bucket(properties.getBucketname()).build());
                //设置bucket策略：私有、公共、自定义
                client.setBucketPolicy(SetBucketPolicyArgs.builder()
                        .bucket(properties.getBucketname())
                        .config(createBucketPolicyConfig(properties.getBucketname())).build());
            }

            //String filename = new SimpleDateFormat("yyyyMMdd").format(new Date()) + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            String uuid = UUID.randomUUID().toString();
            String filename = uuid+"-"+file.getOriginalFilename();
            // UUID-01.jpg
            //上传文件到minio
            client.putObject(PutObjectArgs.builder().
                    bucket(properties.getBucketname()). //bucket名称
                            object(filename). //在bucket文件名称
                            stream(file.getInputStream(), file.getSize(), -1).
                    contentType(file.getContentType()).build());

            //返回文件在minio里面地址
            // http://127.0.0.1:9000/atguigu/01.jpg
//            String url = properties.getEndpoint()
//                    +"/"+properties.getBucketname()
//                    +"/"+filename;

            return String.join("/", properties.getEndpoint(),
                    properties.getBucketname(), filename);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private String createBucketPolicyConfig(String bucketName) {

        return """
            {
              "Statement" : [ {
                "Action" : "s3:GetObject",
                "Effect" : "Allow",
                "Principal" : "*",
                "Resource" : "arn:aws:s3:::%s/*"
              } ],
              "Version" : "2012-10-17"
            }
            """.formatted(bucketName);
    }

    public static void main(String[] args) {
        //    a/b/c
        String result = String.join("/", "a", "b", "c");
        System.out.println(result);

    }
}
