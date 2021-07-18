package com.miaosha.config;


import org.apache.catalina.connector.Connector;
import org.apache.coyote.http11.Http11NioProtocol;
import org.springframework.boot.web.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;


/**
 * 此类为了服务端的安全，
 * 1.避免客户端长连接不用的拖累，又能合理利用服务端的资源。
 * 2.避免了ddos攻击，防止被发送无脑流量包
 * 定义两个参数值
 * keepAliveTimeOut：多少毫秒后不响应则断开keepAlive
 * maxKeepAliveRequests：多少次请求后keepAlive断开失效
 */
@Component
public class WebServerConfiguration implements WebServerFactoryCustomizer<ConfigurableWebServerFactory> {
    @Override
    public void customize(ConfigurableWebServerFactory factory) {
        ((TomcatServletWebServerFactory)factory).addConnectorCustomizers(new TomcatConnectorCustomizer() {
            @Override
            public void customize(Connector connector) {
                Http11NioProtocol protocolHandler = (Http11NioProtocol) connector.getProtocolHandler();

                //30秒内没有请求则服务端自动断开keepAlive连接
                protocolHandler.setKeepAliveTimeout(30000);

                //客户端发送超过10000个请求自动断开keepAlive连接
                protocolHandler.setMaxKeepAliveRequests(10000);
            }
        });
    }
}
