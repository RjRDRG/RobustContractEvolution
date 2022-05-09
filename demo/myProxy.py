# -*- coding: utf-8 -*-
from typing import List, Tuple

from proxy.http.server import ReverseProxyBasePlugin

REVERSE_PROXY_LOCATION: str = r'/user$'

REVERSE_PROXY_PASS = [
    b'http://demo/user'
]

class MyReverseProxyPlugin(ReverseProxyBasePlugin):
    def routes(self) -> List[Tuple[str, List[bytes]]]:
        return [(REVERSE_PROXY_LOCATION, REVERSE_PROXY_PASS)]
