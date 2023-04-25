package org.example.misskeySample

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

object Main {
  data class LocalTimelineReq(
    val i: String,
    val limit: Int = 10
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class User(
    val id: String,
    val username: String,
    val name: String?
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class Note(
    val id: String,
    val createdAt: String,
    val text: String?,
    val user: User
  )

  private fun readLocalTimeline(accessToken: String) {
    val input = LocalTimelineReq(
      i = accessToken,
      limit = 20
    )

    val mapper = jacksonObjectMapper()
    val inputJson = mapper.writeValueAsString(input)

    val url = "https://misskey.io/api/notes/local-timeline"

    val req =
      HttpRequest
        .newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(inputJson))
        .build()

    val client = HttpClient.newBuilder().build()
    val res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))

    if (res.statusCode() != 200) {
      throw Exception("api error. status=${res.statusCode()}")
    }

    val resultJson = res.body()
    val output = mapper.readValue(resultJson, Array<Note>::class.java)

    for (p in output) {
      if (p.text != null) {
        val user = p.user.name ?: p.user.username
        println("user=${user} text=${p.text}")
      }
    }
  }

  enum class NoteVisibility {
    public,
    home,
    followers,
    specified
  }

  data class CreateNoteReq(
    val i: String,
    val visibility: String,
    val text: String?
  )

  @JsonIgnoreProperties(ignoreUnknown = true)
  data class CreateNoteRes(
    val createdNote: Note
  )

  private fun createNote(accessToken: String, message: String, visibility: String) {
    val input = CreateNoteReq(
      i = accessToken,
      text = message,
      visibility = visibility
    )

    val mapper = jacksonObjectMapper()
    val inputJson = mapper.writeValueAsString(input)

    val url = "https://misskey.io/api/notes/create"
    val req =
      HttpRequest
        .newBuilder()
        .uri(URI.create(url))
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(inputJson))
        .build()

    val client = HttpClient.newBuilder().build()
    val res = client.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8))

    if (res.statusCode() != 200) {
      throw Exception("api failed. status=${res.statusCode()}")
    }

    val resultJson = res.body()
    val output = mapper.readValue(resultJson, CreateNoteRes::class.java)

    println("create: id=${output.createdNote.id} id=${output.createdNote.createdAt} ")
  }

  @JvmStatic
  fun main(args: Array<String>) {
    val accessToken = ""
    assert(accessToken.isNotEmpty())
    createNote(accessToken, "Hello Misskey World.", NoteVisibility.public.name)
    Thread.sleep(1000)
    readLocalTimeline(accessToken)
  }
}
