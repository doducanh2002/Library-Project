package org.aibles.authenservice.controller;

import org.aibles.authenservice.dto.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/test")
@CrossOrigin(origins = "*")
public class TestController {

    @GetMapping("/test")
    @ResponseStatus(HttpStatus.OK)
    public BaseResponse<String> test() {
        return new BaseResponse<>("SUCCESS", System.currentTimeMillis(),"Đã test thành công");
    }
}
