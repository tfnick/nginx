package com.demo.docker;

import cn.hutool.core.map.MapUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;

@RestController
public class HttpProxyController {

    private static final Logger logger = LoggerFactory.getLogger(HttpProxyController.class);

    @Value("${http_sftp_proxy}")
    String proxyServer;
    @Value("${http_sftp_proxy_http_port}")
    Integer proxyHttpPort;
    @Value("${http_sftp_proxy_sftp_port}")
    Integer proxySftpPort;



    @GetMapping("/http.html")
    public String doHttp(String url){

        String target = url;
        if (StringUtils.isEmpty(url)) {
            target = "https://www.baidu.com";
        }

        logger.info("准备通过代理 {}:{} 请求 {}", proxyServer,proxyHttpPort, target);
        SimpleClientHttpRequestFactory reqfac = new SimpleClientHttpRequestFactory();
        reqfac.setProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyServer, proxyHttpPort)));
        RestTemplate template = new RestTemplate();
        template.getMessageConverters().set(1,new StringHttpMessageConverter(StandardCharsets.UTF_8));
        template.setRequestFactory(reqfac);

        return template.getForObject(target, String.class);
    }

}
