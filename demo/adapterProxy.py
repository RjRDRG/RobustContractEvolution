# -*- coding: utf-8 -*-

import json

from typing import List, Tuple, Optional

from proxy.http.proxy import HttpProxyBasePlugin
from proxy.http.parser import HttpParser, httpParserTypes

class AdapterPlugin(HttpProxyBasePlugin):

    DEFAULT_CHUNKS = [
        b'modify',
        b'chunk',
        b'response',
        b'plugin',
    ]

    def __init__(self, *args: any, **kwargs: any) -> None:
        super().__init__(*args, **kwargs)
        self.response = HttpParser(httpParserTypes.RESPONSE_PARSER)

    def before_upstream_connection(
            self, request: HttpParser,
    ) -> Optional[HttpParser]:
        return self.handle_service_request(request)

    def handle_client_request(
            self, request: HttpParser,
    ) -> Optional[HttpParser]:
        return self.handle_service_request(request)

    def handle_upstream_chunk(self, chunk: memoryview) -> Optional[memoryview]:
        self.response.parse(chunk)
        if self.response.is_complete:
            print(self.response.path)
            print(self.response)
            if self.response.is_chunked_encoded:
                self.response.body = b'\n'.join(self.DEFAULT_CHUNKS) + b'\n'
            self.client.queue(memoryview(self.response.build_response()))
            self.response = HttpParser(httpParserTypes.RESPONSE_PARSER)
        # Avoid returning chunk straight to client
        return None

    def handle_service_request(
        self, request: HttpParser,
    ) -> Optional[HttpParser]:
        if request.path:
            endpoint = request.path.decode().split("?")[0].split("/")
            endpoint = list(filter(None, endpoint))
            if request.method:
                endpoint.append(request.method.decode().lower())
            match endpoint:
                case["user", "service", "post"]:
                    return self.handle_userservicepost_request(request)
                case["user", "service", "put"]:
                    return self.handle_userserviceput_request(request)
                case _:
                    return request
        return request

    def handle_userservicepost_request(
        self, request: HttpParser,
    ) -> Optional[HttpParser]:
        query = self.build_query_dictionary(request)
        json_body = self.build_json_body_dictionary(request)
        path_id = json_body["id"]
        request.path = ("/" + "user" + "/" + path_id).encode()
        request.method = b"GET"
        return request

    def handle_userserviceput_request(
        self, request: HttpParser,
    ) -> Optional[HttpParser]:
        query = self.build_query_dictionary(request)
        json_body = self.build_json_body_dictionary(request)
        json_name = "joão"
        json_email = self.get_header("email", request)
        json_address = query["mainAddress"]
        request.path = ("/" + "user").encode()
        request.method = b"POST"
        request.update_body(
            ('{"address":"' + json_address + '", "name":"' + json_name + '", "email":"' + json_email + '"}').encode(),
            content_type=b'application/json',
        )
        return request



    def build_query_dictionary(
        self, request: HttpParser,
    ) -> dict[str, str]:
        if request.path:
            aux = request.path.decode().split("?")
            if len(aux) < 2:
                return dict()
            aux = aux[1].split("&")
            aux = list(filter(None, aux))
            query = dict()
            for q in aux:
                query[q.split("=")[0]] = q.split("=")[1]
            return query
        else:
            return dict()

    def build_json_body_dictionary(
        self, request: HttpParser,
    ):
        body = dict()
        if request.body:
            body = json.loads(request.body)
        return body

    def get_header(
        self, headerKey: str, request: HttpParser,
    ) -> str:
        if request.headers:
            aux = request.headers.get(headerKey.encode())
            if aux:
                return aux[1].decode()
            else:
                raise Exception()
        else:
            raise Exception()

