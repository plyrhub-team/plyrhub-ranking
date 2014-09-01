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

package com.plyrhub.odd

import org.junit._
import Assert._
import org.junit.runner.RunWith
import play.api.libs.json.{JsValue, JsString, JsNumber, JsObject}

import scala.reflect.ClassTag
import scala.reflect.runtime.universe._

class OddTest {

  @Test
  def oneTest() = {

    case class P(c: Int, t: String)

    val m = Map("count" -> "12", "pgtk" -> "TOKEN", "status" -> "true")

    // Incluir un 'defaultValue
    def e[T:TypeTag](k:String):JsValue = m.get(k).map(v => b[T](v)).get

    def b[T:TypeTag](v:String) = typeOf[T] match {
      case  t if t =:= typeOf[Int] => JsNumber(v.toInt)
      case _ => JsString(v)
    }

    val j = JsObject(
      Seq(
        "pag" -> JsObject(Seq(
          "count" -> e[Int]("count"),
          "pgtk" -> e[String]("pgtk")
        )
        )
      )
    )

    // Reads!!!!

    // Check which ones are not in the selected set 'partition
    // Check the number in there are more -> do not continue 'size

    // Aplicar Reads!!!!

    assert(1==1)

  }

  @Test
  def twoTest() = {

    case class P(c: Int, t: String)

    val m = Map("count" -> "12", "pgtk" -> "TOKEN", "status" -> "true")

    // Incluir un 'defaultValue
    def e[T:TypeTag](k:String):JsValue = m.get(k).map(v => b[T](v)).get

    def b[T:TypeTag](v:String) = typeOf[T] match {
      case  t if t =:= typeOf[Int] => JsNumber(v.toInt)
      case _ => JsString(v)
    }

    val xxx = ("fromBottom", JsNumber(9))
    val xxx2 = "fromBottom" -> JsNumber(9)

    val xxx1 = "pag" -> JsObject(Seq(
      "count" -> e[Int]("count"),
      "pgtk" -> e[String]("pgtk")
    ))

    val elem = Seq(xxx, xxx1)

    val j = JsObject(
      Seq(
        xxx1,
        "fromTop" -> JsNumber(8),
        xxx
      )
    )

    val jj = JsObject(elem.map{case (k,v) => k -> v})

    assert(1==1)

  }

  object a {
    val x = 9
  }

  @Test
  def threeTest() = {

    val f = () => a

    import f.apply

    println("")




  }

}
