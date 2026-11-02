package com.example.retailflow

import com.example.retailflow.util.Validators
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidatorsTest {

    @Test fun email_validFormats() {
        assertTrue(Validators.isValidEmail("ana.perez@rinconsur.com"))
        assertTrue(Validators.isValidEmail("  ana@gmail.com  "))
    }

    @Test fun email_invalidFormats() {
        assertFalse(Validators.isValidEmail("sinarroba"))
        assertFalse(Validators.isValidEmail("a@b"))
        assertFalse(Validators.isValidEmail("@rinconsur.com"))
        assertFalse(Validators.isValidEmail("a b@rinconsur.com"))
    }

    @Test fun email_corporateDomain() {
        assertTrue(Validators.isCorporateEmail("Ana@RinconSur.com"))
        assertFalse(Validators.isCorporateEmail("ana@gmail.com"))
        assertNull(Validators.emailError("", requireCorporate = true))
        assertNotNull(Validators.emailError("ana@gmail.com", requireCorporate = true))
        assertNull(Validators.emailError("ana@gmail.com", requireCorporate = false))
    }

    @Test fun name_rules() {
        assertNull(Validators.nameError(""))
        assertNull(Validators.nameError("Carlos Mendoza"))
        assertNull(Validators.nameError("José Núñez-Peña"))
        assertNotNull(Validators.nameError("Al"))
        assertNotNull(Validators.nameError("Carlos123"))
        assertNotNull(Validators.nameError("Carlos  Mendoza"))
    }

    @Test fun password_rules() {
        assertNull(Validators.passwordError(""))
        assertEquals("Mínimo 8 caracteres", Validators.passwordError("Ab1"))
        assertNotNull(Validators.passwordError("abcdefg1"))   // sin mayúscula
        assertNotNull(Validators.passwordError("Abcdefgh"))   // sin número
        assertNull(Validators.passwordError("Abcdefg1"))
    }

    @Test fun password_confirm() {
        assertNull(Validators.confirmError("Abc12345", ""))
        assertNull(Validators.confirmError("Abc12345", "Abc12345"))
        assertNotNull(Validators.confirmError("Abc12345", "Abc12346"))
    }

    @Test fun password_strength() {
        assertEquals(0, Validators.passwordStrength(""))
        assertEquals(3, Validators.passwordStrength("Abcdefg1"))
        assertEquals(4, Validators.passwordStrength("Abcdefg1!"))
    }

    @Test fun counts() {
        assertEquals(0, Validators.parseCount("0"))
        assertEquals(99, Validators.parseCount(" 99 "))
        assertNull(Validators.parseCount(""))
        assertNull(Validators.parseCount("100"))
        assertNull(Validators.parseCount("-1"))
        assertNull(Validators.parseCount("abc"))
        assertNotNull(Validators.countError("x"))
        assertNull(Validators.countError("5"))
    }
}
