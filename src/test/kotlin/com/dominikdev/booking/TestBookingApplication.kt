package com.dominikdev.booking

import org.springframework.boot.fromApplication
import org.springframework.boot.with


fun main(args: Array<String>) {
	fromApplication<BookingApplication>().with(TestcontainersConfiguration::class).run(*args)
}
