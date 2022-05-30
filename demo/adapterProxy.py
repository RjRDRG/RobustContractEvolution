# -*- coding: utf-8 -*-

import json

from typing import Optional

from ..http.proxy import HttpProxyBasePlugin
from ..http.parser import HttpParser, httpParserTypes

class AdapterPlugin(HttpProxyBasePlugin):
    def __init__(self, *args: Any, **kwargs: Any) -> None:
        super().__init__(*args, **kwargs)
        self.response = HttpParser(httpParserTypes.RESPONSE_PARSER)

    def handle_client_request(
            self, request: HttpParser,
    ) -> Optional[HttpParser]:
        if request.path:
            endpoint = request.path.decode().split("?")[0].split("//")
            endpoint = list(filter(None, endpoint))
            if request.method:
                endpoint.append(request.method.decode().lower())

            match endpoint:
                case ["user"], ["service"], ["post"]:
                    return self.handle_userservicepost_request(request)
                case ["user"], ["service"], ["put"]:
                    return self.handle_userserviceput_request(request)
                case _:
                    return request

        return request

        def handle_userservicepost_request(
                self, request: HttpParser,
        ) -> Optional[HttpParser]:
            query = self.build_query_dictionary(request)
            jsonBody = self.build_json_body_dictionary(request)

            path_id = jsonBody["id"]

            request.path = "/" + "user" + "/" + path_id

            return request

        def handle_userserviceput_request(
                self, request: HttpParser,
        ) -> Optional[HttpParser]:
            query = self.build_query_dictionary(request)
            jsonBody = self.build_json_body_dictionary(request)

            json_name = "joão"
            json_email = self.get_header("email", request)
            json_address = query["mainAddress"]

            request.path = "/" + "user"
            request.update_body(
                ('{"address":"' + json_address + '", "name":"' + json_name + '", "email":"' + json_email + '"}').encode(),
                content_type=b'application/json',
            )

            return request



    def build_query_dictionary(
            self, request: HttpParser,
    ) -> dict[str, str]:
        if request.path:
            aux = request.path.decode().split("?")[1].split("&")
            aux = list(filter(None, aux))
            query = dict()
            for q in aux:
                query[q.split("=")[0]] = q.split("=")[1]
            return query
        else:
            raise Exception()

    def build_json_body_dictionary(
            self, request: HttpParser,
    ):
        body = dict()
        if request.body:
            body = json.loads(request,body)
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

