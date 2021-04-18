package com.demo.docker;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.map.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class SftpProxyController {

    private static final Logger logger = LoggerFactory.getLogger(SftpProxyController.class);

    @Value("${http_sftp_proxy}")
    String proxyServer;
    @Value("${http_sftp_proxy_port}")
    Integer proxyPort;


    @Value("${sftp_server.username}")
    String sftpUsername;
    @Value("${sftp_server.password}")
    String sftpPassword;

    SftpUtil sftpUtil = new SftpUtil();

    @GetMapping("/sftp.html")
    public Map<String,String> doSftp(String remotePath,String fileRegex){

        if (StringUtils.isEmpty(remotePath)) {
            remotePath = "/";
        }
        if (StringUtils.isEmpty(fileRegex)) {
            fileRegex = ".*\\.txt";
        }

        Map<String,String> result = MapUtil.builder("hello","sftp").build();

        logger.info("send sftp request with {} {} {} {}",sftpUsername,sftpPassword,proxyServer,proxyPort);

        try {
            boolean success = sftpUtil.connect(proxyServer, sftpUsername, sftpPassword, proxyPort, 5000);
            if (success) {
                List<String> files = sftpUtil.listFiles(remotePath, fileRegex);
                result.put("files", CollectionUtil.join(files, ","));
            }else{
                result.put("error", "connect fail");
            }


        } catch (Exception e) {
            result.put("error", e.getMessage());
        }

        return result;
    }
}
