package com.project.socialconnect.payloads.auth

import javax.validation.constraints.Email
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

class SignUpRequest {
    @NotBlank
    @Size(min = 3, max = 50)
    var firstName: String? = null

    @NotBlank
    @Size(min = 3, max = 50)
    var lastName: String? = null

    @NotBlank
    @Size(min = 3, max = 50)
    var userName: String? = null

    var roles: MutableList<String>? = null

    @NotBlank
    @Size(min = 6, max = 40)
    var password: String? = null
}