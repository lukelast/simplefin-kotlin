package com.lukelast.simplefin

import kotlinx.coroutines.runBlocking

object FetchAccessTokenUrl {
  @JvmStatic
  fun main(args: Array<String>) {
    runBlocking {
      val setupToken = "your_setup_token_here"
      val token: AccessTokenUrl = SimplefinAuthClient().use { it.fetchAccessUrl(setupToken) }
      println(token)
    }
  }
}
