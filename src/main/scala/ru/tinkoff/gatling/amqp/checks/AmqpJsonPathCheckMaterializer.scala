package ru.tinkoff.gatling.amqp.checks

import java.io.ByteArrayInputStream
import java.nio.charset.Charset

import io.gatling.core.check.extractor.jsonpath.JsonPathCheckType
import io.gatling.core.check.{CheckMaterializer, Preparer, Specializer}
import io.gatling.core.json.JsonParsers
import ru.tinkoff.gatling.amqp.AmqpCheck
import ru.tinkoff.gatling.amqp.request.AmqpProtocolMessage

import scala.util.Try

class AmqpJsonPathCheckMaterializer(jsonParsers: JsonParsers)
    extends CheckMaterializer[JsonPathCheckType, AmqpCheck, AmqpProtocolMessage, Any] {
  override protected def preparer: Preparer[AmqpProtocolMessage, Any] =
    AmqpJsonPathCheckMaterializer.jsonPathPreparer(jsonParsers)

  override protected def specializer: Specializer[AmqpCheck, AmqpProtocolMessage] = identity
}

object AmqpJsonPathCheckMaterializer {
  private val CharsParsingThreshold = 200 * 1000

  private def jsonPathPreparer(jsonParsers: JsonParsers): Preparer[AmqpProtocolMessage, Any] =
    replyMessage => {
      val bodyCharset = Try(Charset.forName(replyMessage.amqpProperties.getContentEncoding))
        .getOrElse(Charset.defaultCharset())

      if (replyMessage.payload.length > CharsParsingThreshold || jsonParsers.preferJackson)
        jsonParsers.safeParseJackson(new ByteArrayInputStream(replyMessage.payload), bodyCharset)
      else
        jsonParsers.safeParse(new String(replyMessage.payload, bodyCharset))
    }
}
