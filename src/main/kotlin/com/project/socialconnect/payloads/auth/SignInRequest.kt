package com.project.socialconnect.payloads.auth

import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class SignInRequest {
    @NotBlank
    @Size(min = 3, max = 60)
    var userName: String? = null

    @NotBlank
    @Size(min = 6, max = 40)
    var password: String? = null
}