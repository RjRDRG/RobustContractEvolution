FROM abhinavsingh/proxy.py:latest
WORKDIR /proxy
ENV PYTHONPATH="/proxy"
COPY myProxy.py .
ENTRYPOINT ["proxy", "--enable-reverse-proxy", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8082", "--plugins", "myProxy.MyReverseProxyPlugin"]
#ENTRYPOINT ["proxy", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8082", "--plugins", "proxy.plugin.ModifyChunkResponsePlugin"]
#ENTRYPOINT ["proxy", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8081", "--plugins", "proxy.plugin.ProxyPoolPlugin", "--proxy-pool", "demo:8082"]

#ENTRYPOINT ["proxy", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8081", "--plugins", "proxy.plugin.ModifyPostDataPlugin", "--plugins", "proxy.plugin.ProxyPoolPlugin", "--proxy-pool", "demo:8082"]