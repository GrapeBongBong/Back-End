package com.example.capstone.controller.auth;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Api(tags = {"API 정보를 제공하는 Controller"})
@Controller
@RequestMapping("/auth")
public class AuthController {

    @ApiOperation(value = "hello 매소드", notes = "hello 메시지를 반환합니다.") // hello() 메소드 문서화
    @PostMapping("/login")
    public String login() {
        return "hello";
    }

    @PostMapping("/join")
    public String join() {
        return "join";
    }
}
