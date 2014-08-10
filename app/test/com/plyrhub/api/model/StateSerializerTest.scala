/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation (version 3 of the
 *     License).
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plyrhub.api.model

import org.junit._
import Assert._
import org.junit.runner.RunWith
import org.specs2.runner.JUnitRunner
import play.api.data.validation.ValidationError
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._

class StateSerializerTest {

  case class TestStateInSimpleClass(s:State)

  // Simple Class
  @Test
  def stateSimpleReadsTest() = {

    // Using a combinator
    implicit val tsReads:Reads[TestStateInSimpleClass] = (JsPath \ "status").read[State].map(TestStateInSimpleClass)

    val jsonTS =
      """
        |{"status": "active"}
      """.stripMargin

    val jsvalueTS = Json.parse(jsonTS)

    val ts:TestStateInSimpleClass = Json.fromJson[TestStateInSimpleClass](jsvalueTS).get

    assert(ts.s.id == "active")

  }

  @Test
  def stateSimpleWritesTest() = {

    // TODO: Review this to see if there is a better method (this is really ugly)
    // Using a converter
    implicit val tsWrites:Writes[TestStateInSimpleClass] = Writes[TestStateInSimpleClass] {
      (ts:TestStateInSimpleClass) => JsObject(Seq("status" -> JsString(ts.s.id)))
    }

    val ts = TestStateInSimpleClass(StateNotActive())

    val json = Json.toJson(ts)

    assert(json.toString() == """{"status":"not_active"}""")

  }

  // Complex Class
  case class TestStateInComplexClass(s:State, i:Int)

  @Test
  def stateComplexReadsTest() = {

    // Using a combinator
    implicit val tsReads:Reads[TestStateInComplexClass] = (
      (JsPath \ "status").read[State] and
        (JsPath \ "value").read[Int]
      )(TestStateInComplexClass.apply _ )

    val jsonTS =
      """
        |{"status": "active","value":25}
      """.stripMargin

    val jsvalueTS = Json.parse(jsonTS)

    val ts:TestStateInComplexClass = Json.fromJson[TestStateInComplexClass](jsvalueTS).get

    assert(ts.s.id == "active")
    assert(ts.i == 25)

  }

  @Test
  def stateComplexWritesTest() = {

    // Using a combinator
    implicit val tsWrites:Writes[TestStateInComplexClass] = (
      (JsPath \ "status").write[State] and
        (JsPath \ "value").write[Int]
      )(unlift(TestStateInComplexClass.unapply))

    val ts = TestStateInComplexClass(StateNotActive(), 94)

    val json = Json.toJson(ts)

    assert(json.toString() == """{"status":"not_active","value":94}""")

  }

  @Test
  def stateComplexFormatTest() = {

    // format
    implicit val tsFormat:Format[TestStateInComplexClass] = (
      (JsPath \ "status").format[State] and
        (JsPath \ "value").format[Int]
      )(TestStateInComplexClass.apply, unlift(TestStateInComplexClass.unapply))

    // Write
    val tsWrite = TestStateInComplexClass(StateNotActive(), 94)
    val jsonWrite = Json.toJson(tsWrite)
    assert(jsonWrite.toString() == """{"status":"not_active","value":94}""")

    // Read
    val jsonTS =
      """
        |{"status": "active","value":39}
      """.stripMargin
    val jsvalueTS = Json.parse(jsonTS)
    val tsRead:TestStateInComplexClass = Json.fromJson[TestStateInComplexClass](jsvalueTS).get
    assert(tsRead.s.id == "active")
    assert(tsRead.i == 39)

  }

  // JSON Macro Inception
  case class TestStateInception(status:State, value:Int)

  @Test
  def stateInceptionFormatTest() = {

    // format
    implicit val testFormat = Json.format[TestStateInception]

    // Write
    val tsWrite = TestStateInception(StateNotActive(), 94)
    val jsonWrite = Json.toJson(tsWrite)
    assert(jsonWrite.toString() == """{"status":"not_active","value":94}""")

    // Read
    val jsonTS =
      """
        |{"status": "active","value":39}
      """.stripMargin
    val jsvalueTS = Json.parse(jsonTS)
    val tsRead:TestStateInception = Json.fromJson[TestStateInception](jsvalueTS).get
    assert(tsRead.status.id == "active")
    assert(tsRead.value == 39)

  }


}