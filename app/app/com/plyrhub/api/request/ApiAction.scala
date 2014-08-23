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

import akka.actor.Actor
import com.plyrhub.api.request.ApiOperationDefaults._
import com.plyrhub.api.utils.HttpResults._
import com.plyrhub.core.context._
import com.plyrhub.core.log.Loggable
import com.plyrhub.core.model.Lang
import com.plyrhub.core.protocol.ServiceMessage
import play.api.i18n.{Lang => PlayLang}
import play.api.libs.json._
import play.api.mvc._

import scala.concurrent.Future
import scala.reflect.ClassTag

object ApiAction extends Loggable {

  object CustomTypes {
    type apiActionRequestType = UserRequest[AnyContent]
    type paramsType = Seq[Param[ParamOrigin, Any, Presence]]
    type pathValuesType = apiActionRequestType => Map[String, Any]
  }

  import com.plyrhub.api.request.ApiAction.CustomTypes._

  class DefaultAction(val request: apiActionRequestType, val lang:PlayLang, val params: paramsType = Seq(), val pathValues: pathValuesType = request => Map(), val successBlock: successBlockType = onSuccessDefault) {

    def withParams(newParams: paramsType) = new DefaultAction(request, lang, newParams, pathValues, successBlock)

    def withPathValues(newPathValues: pathValuesType) = new DefaultAction(request, lang, params, newPathValues, successBlock)

    def withSuccessBlock(newSuccessBlock: successBlockType) = new DefaultAction(request, lang, params, pathValues, newSuccessBlock)

    def launch[P <: ServiceMessage, S <: Actor](implicit paramsReads: Reads[P] = null, classTagS: ClassTag[S], classTagP: ClassTag[P]) =
      defaultApiAction[P, S](params)(pathValues)(successBlock)(request)(lang)

  }

  object DefaultAction {

    def apply[P <: ServiceMessage, S <: Actor]()(implicit request: apiActionRequestType, lang:PlayLang) = new DefaultAction(request, lang)

    def apply[P <: ServiceMessage, S <: Actor](params: paramsType, pathValues: pathValuesType)(implicit request: apiActionRequestType, lang:PlayLang, paramsReads: Reads[P] = null, classTagS: ClassTag[S], classTagP: ClassTag[P]) = new DefaultAction(request, lang, params, pathValues, onSuccessDefault).launch[P, S]

  }


  def defaultApiAction[P <: ServiceMessage, S <: Actor]
  (params: paramsType)
  (pathValues: pathValuesType)
  (successBlock: successBlockType)
  (request: apiActionRequestType)
  (lang: PlayLang)
  (implicit paramsReads: Reads[P] = null, classTagS: ClassTag[S], classTagP: ClassTag[P]) = {

    // Extract Parameters
    implicit val rqRequest = request
    implicit val rqLang = lang
    implicit val rqParams = params
    implicit val rqValues = pathValues(request)

    // Process request failure or success
    extractParameters[P].fold(errorOnParameters, processRequest[S](_, buildOperationContext, successBlock))

  }

  def errorOnParameters(parms: Seq[ParamError])(implicit lang:PlayLang): Future[Result] = {
    Future.successful(ApiRqParamError(parms))
  }

  def processRequest[S <: Actor]
  (message: ServiceMessage, octx: OperationContext, successBlock: successBlockType)
  (implicit classTagS: ClassTag[S]): Future[Result] = {

    ApiOperation()
      .onSuccess(successBlock)
      .launch[S](octx, message)

  }

  def buildOperationContext(implicit request: apiActionRequestType, lang:PlayLang) = {

    // TODO: put his better
    // by now assume it is a UserRequest
    val urq = UserRequest.convert(request).get

    OperationContext(urq.owner, urq.user, Lang(lang.code, lang.country))
  }

  val apiConverterErrorMap = Map(
    "error.path.missing" -> "plyrhub.p.converter.missing.value",
    "error.maxLength" -> "plyrhub.p.converter.maxLength",
    "error.minLength" -> "plyrhub.p.converter.minLength"
  )

  def extractParameters[P](implicit request: apiActionRequestType, pathValue: Map[String, Any], params: Seq[Param[ParamOrigin, Any, Presence]], paramsReads: Reads[P] = null, classTagP: ClassTag[P]): Either[Seq[ParamError], P] = {

    // Extract values from header, path, qs and body
    val (undefined, values) = params.map(p => p.name -> p.extract)
      .filter { case (k, o) => o != JsNull}
      .partition {
      case (k, o: JsUndefined) => true
      case _ => false
    }


    // Convert to extractor case class

    Option(paramsReads) match {
      case None => Right(implicitly[ClassTag[P]].runtimeClass.newInstance().asInstanceOf[P])
      case _ => {
        val fullParamsJson = JsObject(values)
        fullParamsJson.validate[P].fold(
          // Something was wrong
          errors => {
            val jsonErrors = errors.map {
              case (path, errs) => ParamError(path.toString().drop(1), {
                val e = errs.map(_.message).head
                if (e.startsWith("plyrhub"))
                  e
                else {
                  apiConverterErrorMap.getOrElse(e, {
                    log.error(s"Error message non identified: $e")
                    "plyrhub.generic.error"
                  })
                }
              })
            }

            Left((getUndefinedErrors(undefined).toSet ++ jsonErrors.toSet).toSeq)
          },
          /// Seems to be Ok but still we are not sure
          fullParameter => {
            if (undefined.isEmpty)
              Right(fullParameter)
            else
              Left(getUndefinedErrors(undefined))
          }
        )
      }
    }
  }

  def getUndefinedErrors(undefined: Seq[(String, JsValue)]): Seq[ParamError] = {

    def getErrorFromUndefined(v: JsValue) = {
      (v: @unchecked) match {
        case u: JsUndefined => u.error
      }
    }

    undefined.map {
      case (name, value) => ParamError(name, getErrorFromUndefined(value))
    }
  }
}






