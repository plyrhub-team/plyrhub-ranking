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

package com.plyrhub.ranking.controller

import com.plyrhub.api.request.ApiAction.DefaultAction
import com.plyrhub.api.utils.ApiDefaults.ActionDefaults._
import com.plyrhub.api.utils.HttpResults._
import com.plyrhub.core.log.Loggable
import com.plyrhub.core.protocol.ServiceSuccess
import com.plyrhub.ranking.service.{MemberRegistrator, RankingCreator}
import com.plyrhub.ranking.service.protocol._
import play.api.mvc.{Controller, Result}

object MemberController extends Controller with Loggable {

  /*
    Registrates a new Member
   */
  val registrateMemberParams = Seq(MEMBER_ID, BODY)

  def registrateMember(member: String) =

    AuthAction.async {

      implicit request =>

        val successBlock: PartialFunction[ServiceSuccess, Result] = {

          case MemberRegistered(member:String) => API_SIMPLE_CREATED
          case MemberAlreadyExist(member:String) => ApiRqParamError(Seq(ParamError(member, "plyrhub.member.already.exists")))
          case MemberNonValidRankings(member:String, nonValidRankings:Seq[String]) => ApiRqParamError(Seq(ParamError(member, "plyrhub.member.non.valid.rankings")))
        }

        DefaultAction()
          .withParams(registrateMemberParams)
          .withPathValues(MEMBER_ID.seedMap(member))
          .withSuccessBlock(successBlock)
          .launch[MemberRegistrationMsg, MemberRegistrator]

    }

}
