FROM abhinavsingh/proxy.py:latest
WORKDIR /proxy
ENV PYTHONPATH="/proxy"
COPY adapterProxy.py .
ENTRYPOINT ["proxy", "--threaded", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8082", "--plugins", "adapterProxy.AdapterPlugin"]
#ENTRYPOINT ["proxy", "--threaded", "--enable-reverse-proxy", "--hostname", "0.0.0.0","--log-level", "d", "--port", "8082", "--plugins", "myProxy.ChangeBodyPlugin", "--plugins", "myProxy.MyReverseProxyPlugin"]
#ENTRYPOINT ["proxy", "--threadless", "--local-executor", "0", "--num-workers", "8", "--enable-reverse-proxy", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8082", "--plugins", "myProxy.MyReverseProxyPlugin"]
#ENTRYPOINT ["proxy", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8082", "--plugins", "proxy.plugin.ModifyChunkResponsePlugin"]
#ENTRYPOINT ["proxy", "--hostname", "0.0.0.0", "--log-level", "d", "--port", "8081", "--plugins", "proxy.plugin.ModifyPostDataPlugin", "--plugins", "proxy.plugin.ProxyPoolPlugin", "--proxy-pool", "demo:8082"]