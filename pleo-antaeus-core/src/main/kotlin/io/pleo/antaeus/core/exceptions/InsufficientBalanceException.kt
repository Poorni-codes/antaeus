package io.pleo.antaeus.core.exceptions

import java.math.BigDecimal

class InsufficientBalanceException(invoiceId: Int, value: BigDecimal) :
    Exception("Insufficient balance for invoiceId='$invoiceId' amount ='$value'")
