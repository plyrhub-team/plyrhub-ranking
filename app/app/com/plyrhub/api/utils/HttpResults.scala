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

package com.plyrhub.api.utils

import com.plyrhub.api.codes.ApiCode
import com.plyrhub.api.model.Response._
import com.plyrhub.api.model.{Data, Pagination, Response, Result}
import com.plyrhub.core.log.Loggable
import play.api.libs.json.{Writes, Json}
import play.api.mvc.Results._

object HttpResults extends Loggable {

  val API_SIMPLE_SUCCESS = Ok("")
  val API_SIMPLE_CREATED = Created("")

  def API_SUCCESS[T](data: Option[T], pagination: Option[Pagination] = None)(implicit writes: Writes[T]) =
    ApiSuccessResponse[T](Ok, ApiCode.OK_CODE, data, pagination)

  def API_CREATED[T](data: Option[T], pagination: Option[Pagination] = None)(implicit writes: Writes[T]) =
    ApiSuccessResponse[T](Created, ApiCode.CREATED_CODE, data, pagination)


  private[this] def ApiSuccessResponse[T](playStatus: Status, code: ApiCode, data: Option[T], pagination: Option[Pagination])(implicit writes: Writes[T]) = {

    data.fold({
      playStatus("")
    })(d => {
      val resp = Response(Result(code.httpCode), Some(Data(Json.toJson(d))), pagination)
      playStatus(Json.toJson(resp).toString())
    }
      )
  }

  class ParamError private(val name: String, val msg: String) {

    override def hashCode() = name.hashCode

    override def equals(other: scala.Any) = other match {
      case that: ParamError => this.name == that.name
      case _ => false
    }

  }

  object ParamError {
    def apply(name: String, msg: String) = new ParamError(name, msg)
  }

  private[this] val apiGlobalErrorMap = Map(
    "Invalid Json" -> "plyrhub.non.valid.body"
  )

  val API_RQ_PARAM_ERROR = (errors: Seq[ParamError]) => {
    ApiErrorResponse(BadRequest, ApiCode.E400_PARAM_ERROR_CODE, errors.map(e => s"${e.name} ... ${e.msg}"))
  }

  val API_GLOBAL_ERROR = (errors: Seq[String]) => {

    ApiErrorResponse(BadRequest, ApiCode.E400_PARAM_ERROR_CODE,
      errors.map(e => apiGlobalErrorMap.getOrElse(e, {
        log.error(s"Error message non identified: $e")
        "plyrhub.generic.error"
      })))

  }

  val API_UNAUTHORIZED_ERROR = (errors: Seq[String]) => {
    ApiErrorResponse(Unauthorized, ApiCode.E401_UNAUTHORIZED_ACCESS_CODE, errors)
  }

  val API_GENERIC_ERROR = (errors: Seq[String]) => {
    ApiErrorResponse(InternalServerError, ApiCode.E500_SERVER_ERROR_CODE, errors)
  }

  private[this] def ApiErrorResponse(playStatus: Status, code: ApiCode, errors: Seq[String]) = {
    val r = Result(code.httpCode, Some(code.apiCode), Some(errors))
    val resp = Response(r)
    playStatus(Json.toJson(resp).toString())
  }

}
