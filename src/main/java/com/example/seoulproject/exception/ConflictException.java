package com.example.seoulproject.exception;

import com.example.seoulproject.exception.errorcode.ErrorCode;

public class ConflictException extends CustomException {
    public ConflictException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ConflictException(ErrorCode errorCode, String detail) {
        super(errorCode, detail);
    }
}
