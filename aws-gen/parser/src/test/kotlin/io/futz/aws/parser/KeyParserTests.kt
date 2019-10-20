package io.futz.aws.parser

import io.futz.aws.parser.KeyParser
import org.junit.Assert.assertEquals
import org.junit.Test

class KeyParserTests {

    @Test
    fun doubleColonOnly() {
        val r = KeyParser().parse("AWS::OpsWorksCM::Server")
        assertEquals("aws.opsworkscm", r[0])
        assertEquals("Server", r[1])
    }

    @Test
    fun mix() {
        val r = KeyParser().parse("AWS::ApiGatewayV2::Stage.AccessLogSettings")
        assertEquals("aws.apigatewayv2.stage", r[0])
        assertEquals("AccessLogSettings", r[1])
    }

    @Test
    fun tag() {
        val r = KeyParser().parse("Tag")
        assertEquals("aws", r[0])
        assertEquals("Tag", r[1])
    }
}