
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

import com.plyrhub.api.utils.Cacheable
import play.api.data.validation.ValidationError
import play.api.libs.json._

import play.api.libs.json.Reads._

import play.api.libs.json.Writes._

import play.api.libs.functional.syntax._

sealed abstract class State(val id: String)

case class StateActive() extends State("active")

case class StateNotActive() extends State("not_active")

object State extends Cacheable[State] {

  override protected val cacheKey: String = "query.state"

  private val stateActive = StateActive()
  private val stateNotActive = StateNotActive()

  private val mapStates = Map(stateActive.id -> stateActive, stateNotActive.id -> stateNotActive)

  def apply(state: String):Option[State] = {

    mapStates.get(Option(state).getOrElse("no-key").trim)
  }

  def unapply(state: State) = {
    Some(state.id)
  }

  // Serialization with converters
  implicit object StateReads extends Reads[State] {
    def reads(json: JsValue) = json match {
      case JsString(s) =>

        State(s) match {
          case Some(state) => JsSuccess(state)
          case _ => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.not.allowed.value.state"))))
        }

      case _ => JsError(Seq(JsPath() -> Seq(ValidationError("plyrhub.error.non.valid.state"))))
    }
  }

  implicit object StateWrites extends Writes[State] {
    def writes(s: State) =
      JsString(s.id)
  }

}