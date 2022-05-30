# -*- coding: utf-8 -*-
from typing import List, Tuple, Optional

from proxy.http.proxy import HttpProxyBasePlugin
from proxy.http.parser import HttpParser

MODIFIED_BODY = b'{"key": "modified"}'

class AdapterPlugin(HttpProxyBasePlugin):

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
