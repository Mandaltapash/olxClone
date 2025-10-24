package com.tods.project_olx

import com.tods.project_olx.utils.ValidationResult
import com.tods.project_olx.utils.Validator
import org.junit.Test

class ValidatorTest {

    @Test
    fun `validateEmail with valid email returns Success`() {
        val result = Validator.validateEmail("test@example.com")
        assert(result is ValidationResult.Success)
    }

    @Test
    fun `validateEmail with invalid email returns Error`() {
        val result = Validator.validateEmail("invalid-email")
        assert(result is ValidationResult.Error)
    }

    @Test
    fun `validatePassword with short password returns Error`() {
        val result = Validator.validatePassword("123")
        assert(result is ValidationResult.Error)
    }

    @Test
    fun `validatePassword with valid password returns Success`() {
        val result = Validator.validatePassword("password123")
        assert(result is ValidationResult.Success)
    }

    @Test
    fun `validatePhone with valid phone returns Success`() {
        val result = Validator.validatePhone("+55 (11) 98765-4321")
        assert(result is ValidationResult.Success)
    }
}

