package com.lukelast.simplefin

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AccessTokenUrlTest {

  @Test
  fun `constructor with valid URL creates AccessTokenUrl correctly`() {
    val url = "https://demo:password@beta-bridge.simplefin.org/simplefin"
    val accessTokenUrl = AccessTokenUrl(url)

    assertEquals("demo", accessTokenUrl.user)
    assertEquals("password", accessTokenUrl.pass)
    assertEquals("https://beta-bridge.simplefin.org/simplefin", accessTokenUrl.fullUrlString)
  }

  @Test
  fun `constructor with user and pass creates AccessTokenUrl correctly`() {
    val accessTokenUrl = AccessTokenUrl("testuser", "testpass")

    assertEquals("testuser", accessTokenUrl.user)
    assertEquals("testpass", accessTokenUrl.pass)
    assertEquals("https://beta-bridge.simplefin.org/simplefin", accessTokenUrl.fullUrlString)
  }

  @Test
  fun `accountsUrl returns correct URLBuilder`() {
    val accessTokenUrl = AccessTokenUrl("user", "pass")
    val accountsUrl = accessTokenUrl.accountsUrl().build()

    assertEquals("https", accountsUrl.protocol.name)
    assertEquals("beta-bridge.simplefin.org", accountsUrl.host)
    assertEquals(listOf("simplefin", "accounts"), accountsUrl.segments)
  }

  @Test
  fun `demoAccessUrl is configured correctly`() {
    assertEquals("demo", demoAccessUrl.user)
    assertEquals("demo", demoAccessUrl.pass)
    assertTrue(demoAccessUrl.fullUrlString.contains("beta-bridge.simplefin.org/simplefin"))
  }

  @Test
  fun `constructor throws on non-HTTPS URL`() {
    val url = "http://demo:password@beta-bridge.simplefin.org/simplefin"

    assertThrows<IllegalArgumentException> { AccessTokenUrl(url) }
  }

  @Test
  fun `constructor throws on URL without credentials`() {
    val url = "https://beta-bridge.simplefin.org/simplefin"

    assertThrows<IllegalStateException> { AccessTokenUrl(url) }
  }

  @Test
  fun `constructor works with empty password`() {
    val url = "https://demo:@beta-bridge.simplefin.org/simplefin"
    val accessTokenUrl = AccessTokenUrl(url)

    assertEquals("demo", accessTokenUrl.user)
    assertEquals("", accessTokenUrl.pass)
  }

  @Test
  fun `constructor throws on invalid host`() {
    val url = "https://demo:password@invalid-host.com/simplefin"

    assertThrows<IllegalArgumentException> { AccessTokenUrl(url) }
  }

  @Test
  fun `constructor throws on invalid path`() {
    val url = "https://demo:password@beta-bridge.simplefin.org/invalid-path"

    assertThrows<IllegalArgumentException> { AccessTokenUrl(url) }
  }
}
