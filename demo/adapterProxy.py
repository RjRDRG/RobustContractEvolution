# -*- coding: utf-8 -*-

import json
import copy

from typing import List, Tuple, Optional

from proxy.http.proxy import HttpProxyBasePlugin
from proxy.http.parser import HttpParser, httpParserTypes

class AdapterPlugin(HttpProxyBasePlugin):

    def __init__(self, *args: any, **kwargs: any) -> None:
        super().__init__(*args, **kwargs)
        self.response = HttpParser(httpParserTypes.RESPONSE_PARSER)
        self.request = None

    def before_upstream_connection(
            self, request: HttpParser,
    ) -> Optional[HttpParser]:
        self.request = request
        return self.handle_message(copy.deepcopy(request), None)

    def handle_client_request(
            self, request: HttpParser,
    ) -> Optional[HttpParser]:
        if self.request is None:
            self.request = request
            return self.handle_message(copy.deepcopy(request), None)
        else:
            return request

    def handle_upstream_chunk(self, chunk: memoryview) -> Optional[memoryview]:
        self.response.parse(chunk)
        if self.response.is_complete:
            self.response = self.handle_message(self.request, self.response)
            self.client.queue(memoryview(self.response.build_response()))
            self.response = HttpParser(httpParserTypes.RESPONSE_PARSER)
            self.request = None
        return None

    def handle_message(
            self, request: HttpParser, response: Optional[HttpParser]
    ) -> Optional[HttpParser]:
        if request.path:
            endpoint = request.path.decode().split("?")[0].split("/")
            endpoint = list(filter(None, endpoint))
            if request.method:
                endpoint.append(request.method.decode().lower())
            if response:
                endpoint.append(response.code.decode().lower())
            message = None
            if response:
                message = response
            else:
                message = request
            match endpoint:
                case["user", "service", "post"]:
                    return self.handle_userservicepost_request(message)
                case["user", "service", "post", "200"]:
                    return self.handle_userservicepost_200(message)
                case["user", "service", "put"]:
                    return self.handle_userserviceput_request(message)
                case _:
                    return message
        return message

    def handle_userservicepost_request(
            self, message: HttpParser,
    ) -> Optional[HttpParser]:
        query = self.build_query_dictionary(message)
        body = self.build_json_body_dictionary(message)
        path_id = body["id"]
        message.path = ("/" + "user" + "/" + path_id).encode()
        message.method = b"GET"
        return message

    def handle_userservicepost_200(
            self, message: HttpParser,
    ) -> Optional[HttpParser]:
        body = self.build_json_body_dictionary(message)
        json_name = "joão2"
        json_email = body["address"]
        json_mainAddress = body["email"]
        message.code = b'200'
        message.body = ('{"name":"' + json_name + '", "email":"' + json_email + '", "mainAddress":"' + json_mainAddress + '"}').encode()
        return message

    def handle_userserviceput_request(
            self, message: HttpParser,
    ) -> Optional[HttpParser]:
        query = self.build_query_dictionary(message)
        body = self.build_json_body_dictionary(message)
        json_name = "joão"
        json_email = self.get_header("email", message)
        json_address = query["mainAddress"]
        message.path = ("/" + "user").encode()
        message.method = b"POST"
        message.update_body(
            ('{"address":"' + json_address + '", "name":"' + json_name + '", "email":"' + json_email + '"}').encode(),
            content_type=b'application/json',
        )
        return message



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
            self, message: HttpParser,
    ):
        if message.body:
            return json.loads(message.body)
        return dict()

    def get_header(
            self, headerKey: str, message: HttpParser,
    ) -> str:
        if message.headers:
            aux = message.headers.get(headerKey.encode())
            if aux:
                return aux[1].decode()
            else:
                raise Exception()
        else:
            raise Exception()

