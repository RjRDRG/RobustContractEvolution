# -*- coding: utf-8 -*-
from typing import Optional

from ..http.proxy import HttpProxyBasePlugin
from ..http.parser import HttpParser

MODIFIED_BODY = b'{"key": "modified"}'

class MyReverseProxyPlugin(ReverseProxyBasePlugin):

    def before_upstream_connection(
            self, request: HttpParser,
    ) -> Optional[HttpParser]:
        return request

    def handle_client_request(
            self, request: HttpParser,
    ) -> Optional[HttpParser]:
        if request.body:
            request.update_body(
                MODIFIED_BODY,
                content_type=b'application/json',
            )
        return request


