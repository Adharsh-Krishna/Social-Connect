package com.project.socialconnect.payloads.account

import java.math.BigInteger

data class AccountResponsePayload(val id: BigInteger,
                                  val name: String,
                                  val accountTypeId: BigInteger)