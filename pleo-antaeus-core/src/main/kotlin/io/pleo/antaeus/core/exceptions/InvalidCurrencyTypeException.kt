package io.pleo.antaeus.core.exceptions

import io.pleo.antaeus.models.Currency

class InvalidCurrencyTypeException(customerId: Int, currency : Currency) :
    Exception("Currency = $currency of Customer '$customerId' does not match the list of acceptable currency type")
