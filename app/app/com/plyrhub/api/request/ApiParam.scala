/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License Version 3
 *     as published by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plyrhub.api.request


import com.plyrhub.core.log.Loggable
import play.api.libs.json._
import play.api.mvc.{AnyContent, Request}

import scala.reflect.runtime.universe._

/*
  this is not the most performant nor elegant code but the idea behind is that
  everything is an input parameter doesn't matter where it come from
  (path, queryStringm header or body) ... perhaps some kind of READS
  able to read form multiple sources would be great ...
  TODO: in next iteration
 */

sealed trait Presence

trait Req extends Presence

trait Opt extends Presence

sealed trait ParamOrigin

trait H extends ParamOrigin

trait P extends ParamOrigin

trait Q extends ParamOrigin

trait B extends ParamOrigin

trait ParamResult {
  type Result
}

trait PString extends ParamResult {
  type Result = String
}

trait PSInt extends ParamResult {
  type Result = Int
}

trait PSBody extends ParamResult {
  type Result = B
}

object Extractors {

  lazy val typeHeader = typeOf[H]
  lazy val typePath = typeOf[P]
  lazy val typeQueryString = typeOf[Q]
  lazy val typeBody = typeOf[B]

  def fromHeader(name: String, request: Request[AnyContent]): Option[Any] = {
    request.getQueryString(name)
  }

  def fromPath(name: String, pathMap: Map[String, Any]) = {
    pathMap.get(name)
  }

  def fromQueryString(name: String, request: Request[AnyContent]): Option[Any] = {
    request.getQueryString(name)
  }

  def fromBody(request: Request[AnyContent]): Option[Any] = {
    request.body.asJson
  }

}

object Converters {

  lazy val typeString = typeOf[String]
  lazy val typeInt = typeOf[Int]
  lazy val typeBoolean = typeOf[Boolean]
  lazy val typeBody = typeOf[B]

  type ConverterType = Any => JsValue

  val StringConverter: ConverterType = v => JsString(v.asInstanceOf[String])

  val IntConverter: ConverterType = v => {
    try {
      JsNumber(v.asInstanceOf[String].toInt)
    } catch {
      case e: Exception => JsUndefined("plyrhub.p.converter.non.valid.value")
    }
  }

  val BooleanConverter: ConverterType = v => {
    try {
      JsBoolean(v.asInstanceOf[String].toLowerCase.toBoolean)
    } catch {
      case e: Exception => JsUndefined("plyrhub.p.converter.non.valid.value")
    }
  }

  val BodyConverter: ConverterType = v => {
    try {
      v.asInstanceOf[JsObject]
    } catch {
      case e: Exception => JsUndefined("plyrhub.p.converter.non.valid.body.value")
    }
  }

  val UndefinedConverter: ConverterType = v => JsUndefined("plyrhub.p.converter.non.valid.type")
}

final class Param[+O <: ParamOrigin : TypeTag, +R: TypeTag, +M <: Presence : TypeTag] private (val name: String, val default: Option[R]) extends Loggable {

  lazy val typeOrigin = typeOf[O]
  lazy val typeResult = typeOf[R]
  lazy val typePresence = typeOf[M]

  def extract(implicit request: Request[AnyContent], pathMap: Map[String, Any]) = {

    // TODO: review this and make it work with implicits
    // unable to make implicits available in context with the convert function inside the map-operation
    // probably related to the non homogeneous Seq of Param

    import com.plyrhub.api.request.Extractors._

    (
      typeOrigin match {

        case t if t =:= typeHeader => fromHeader(name, request)
        case t if t =:= typePath => fromPath(name, pathMap)
        case t if t =:= typeQueryString => fromQueryString(name, request)
        case t if t =:= typeBody => fromBody(request)
      }
      ).map(convert()(_)).getOrElse(managePresence())

  }

  def convert() = {

    import com.plyrhub.api.request.Converters._

    typeResult match {
      case t if t =:= typeString => StringConverter
      case t if t =:= typeInt => IntConverter
      case t if t =:= typeBoolean => BooleanConverter
      case t if t =:= typeBody => BodyConverter
      case _ => UndefinedConverter
    }
  }

  def managePresence() = {
    if (typePresence =:= typeOf[Req])
      JsUndefined("plyrhub.p.converter.mandatory.value.not.provided")
    else {
      default.map(d => convert()(d.toString))
        .orElse {
        log.error(s"Optional parameter with NO default value: $name")
        Some(JsNull)
      }.get
    }
  }

  import com.plyrhub.api.request.ApiAction.CustomTypes._
  def seedMap(value:Any)(implicit request:apiActionRequestType):pathValuesType = request => Map(name -> value)
  def seedValue(value:Any) = name -> value

}

object Param {

  //def apply[O <: ParamOrigin : TypeTag, R: TypeTag, M <: Presence : TypeTag](name: String) = new Param3[O, R, M](name, None)
  def apply[O <: ParamOrigin : TypeTag, R: TypeTag](name: String, default: R) = new Param[O, R, Opt](name, Some(default))

  def apply[O <: ParamOrigin : TypeTag, R: TypeTag](name: String) = new Param[O, R, Req](name, None)

}


