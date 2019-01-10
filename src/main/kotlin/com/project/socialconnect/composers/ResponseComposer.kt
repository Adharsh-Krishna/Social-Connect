package com.project.socialconnect.composers

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

enum class ResponseType {
    SUCCESS,
    FAILURE
}

class SuccessResponse(val responseType: ResponseType, val data: Any)

class ErrorResponse(val responseType: ResponseType, val errors: List<String>)

interface ResponseComposer {

    companion object {
        fun composeSuccessResponseWith(payload: Any): ResponseEntity<Any> {
            return ResponseEntity(
                    SuccessResponse(ResponseType.SUCCESS, payload),
                    HttpStatus.OK)
        }

        fun composeErrorResponseWith(vararg errors: String): ResponseEntity<Any> {
            val errorsList: List<String> = errors.map { it }
            return ResponseEntity(
                    ErrorResponse(ResponseType.FAILURE, errorsList),
                    HttpStatus.INTERNAL_SERVER_ERROR)
        }
    }

}