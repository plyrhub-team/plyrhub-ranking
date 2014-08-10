
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

package com.plyrhub.api.codes

import play.api.http.Status

class ApiCode(val httpCode: Int, val apiCode: String, val apiMessage: String)

object ApiCode {

  val OK_CODE = ApiCode(Status.OK, "OK", "api.code.ok.success")

  val E400_GENERIC_ERROR_CODE = ApiCode(Status.BAD_REQUEST, "E400-GENERIC-ERROR", "api.code.e400.generic.error")
  val E400_PARAM_ERROR_CODE = ApiCode(Status.BAD_REQUEST, "E400-PARAM-ERROR", "api.code.e400.param.error")

  val E401_UNAUTHORIZED_ACCESS_CODE = ApiCode(Status.UNAUTHORIZED, "E401_UNAUTHORIZED_ACCESS", "api.code.e401.unauthorized.access")

  val E404_COLLECTION_NOT_FOUND_CODE = ApiCode(Status.NOT_FOUND, "E404_COLLECTION_NOT_FOUND", "api.code.e404.collection.not.found")
  val E404_PLATFORM_NOT_FOUND_CODE = ApiCode(Status.NOT_FOUND, "E404_PLATFORM_NOT_FOUND", "api.code.e404.platform.not.found")
  val E404_RANKING_NOT_FOUND_CODE = ApiCode(Status.NOT_FOUND, "E404_RANKING_NOT_FOUND", "api.code.e404.ranking.not.found")
  val E404_MEMBER_NOT_FOUND_CODE = ApiCode(Status.NOT_FOUND, "E404_MEMBER_NOT_FOUND", "api.code.e404.member.not.found")

  val E500_SERVER_ERROR_CODE = ApiCode(Status.INTERNAL_SERVER_ERROR, "E500_SERVER_ERROR", "api.code.e500.server.error")

  // 403 - Forbidden
  // 405 - Method Not Allowed
  // 415 - Unsupported Media Type
  // 416 - Request Range Not Satisfiable

  private[this] val apiCodes = Map(

    OK_CODE.apiCode -> OK_CODE,

    E400_GENERIC_ERROR_CODE.apiCode -> E400_GENERIC_ERROR_CODE,
    E400_PARAM_ERROR_CODE.apiCode -> E400_PARAM_ERROR_CODE,

    E401_UNAUTHORIZED_ACCESS_CODE.apiCode -> E401_UNAUTHORIZED_ACCESS_CODE,

    E404_COLLECTION_NOT_FOUND_CODE.apiCode -> E404_COLLECTION_NOT_FOUND_CODE,
    E404_PLATFORM_NOT_FOUND_CODE.apiCode -> E404_PLATFORM_NOT_FOUND_CODE,
    E404_RANKING_NOT_FOUND_CODE.apiCode -> E404_RANKING_NOT_FOUND_CODE,
    E404_MEMBER_NOT_FOUND_CODE.apiCode -> E404_MEMBER_NOT_FOUND_CODE,

    E500_SERVER_ERROR_CODE.apiCode -> E500_SERVER_ERROR_CODE
  )

  def apply(httpCode: Int, apiCode: String, apiMessage: String): ApiCode = {
    new ApiCode(httpCode, apiCode, apiMessage)
  }

  def getApiCode(apiCode:String):Option[ApiCode] = apiCodes.get(apiCode)

}







