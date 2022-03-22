package com.example.demo.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;

import static com.example.demo.config.BaseResponseStatus.*;

@RestControllerAdvice
public class ExceptionHandlerAdvice {

    @ExceptionHandler(BaseException.class)
    public BaseResponse BaseException(BaseException exception){
        return new BaseResponse<>(exception.getStatus());
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse> methodValidException(MethodArgumentNotValidException e, HttpServletRequest request){
        BaseResponse baseResponse = makeErrorResponse(e.getBindingResult());
        return new ResponseEntity<BaseResponse>(baseResponse, HttpStatus.BAD_REQUEST);
    }

    private BaseResponse makeErrorResponse(BindingResult bindingResult) {

        String detail = "";

        //DTO에 설정한 meaasge값을 가져온다
        detail = bindingResult.getFieldError().getDefaultMessage();

        //DTO에 유효성체크를 걸어놓은 어노테이션명을 가져온다.
        String bindResultCode = bindingResult.getFieldError().getCode();

        switch (bindResultCode){
            case "NotBlank":
                return new BaseResponse(NOT_BLANK, detail);
            case "NotEmpty":
                return new BaseResponse(NOT_EMPTY, detail);
            case "Pattern":
                return new BaseResponse(PATTERN, detail);
            case "Min":
                return new BaseResponse(MIN_VALUE, detail);
            case "Size":
                return new BaseResponse(SIZE, detail);
            case "NotNull":
                return new BaseResponse(NOT_NULL, detail);
        }

        return new BaseResponse(DEFAULT_ERROR);
    }

}
