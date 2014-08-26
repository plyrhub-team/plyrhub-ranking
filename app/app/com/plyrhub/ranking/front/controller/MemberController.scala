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

package com.plyrhub.ranking.front.controller

import com.plyrhub.api.request.ApiAction.DefaultAction
import com.plyrhub.api.request.{ApiHttpResults, ApiDefaults}
import ApiDefaults.ActionDefaults._
import ApiHttpResults._
import com.plyrhub.core.log.Loggable
import com.plyrhub.core.protocol.ServiceSuccess
import com.plyrhub.ranking.service.{MemberScorer, MemberRegistrator, RankingCreator}
import com.plyrhub.ranking.service.MemberRegistrator._
import play.api.mvc.{Controller, Result}

object MemberController extends Controller with Loggable {


  val memberParams = Seq(MEMBER_ID, BODY)

  /*
    Registrates a new Member
  */
  def registrateMember(member: String) =

    AuthAction.async {

      implicit request =>

        val successBlock: PartialFunction[ServiceSuccess, Result] = {

          case MemberRegistered(member: String) => API_SIMPLE_CREATED
          case MemberAlreadyExist(member: String) => ApiRqParamError(Seq(ParamError(member, "plyrhub.member.already.exists")))
          case MemberNonValidRankings(member: String, nonValidRankings: Seq[String]) => {
            ApiRqParamError(Seq(ParamError(member, "plyrhub.member.non.valid.rankings", Some(nonValidRankings.mkString(",")))))
          }
        }

        DefaultAction()
          .withParams(memberParams)
          .withPathValues(MEMBER_ID.seedMap(member))
          .withSuccessBlock(successBlock)
          .launch[MemberRegistrationMsg, MemberRegistrator]

    }

  /*
    Increments score on the selected rankings
    The score is a Delta to add to the existing value
      - note: the score can be negative
   */
  def score(member: String) =

    AuthAction.async {

      implicit request =>

        val successBlock: PartialFunction[ServiceSuccess, Result] = {

          case MemberRegistered(member: String) => API_SIMPLE_CREATED
          case MemberAlreadyExist(member: String) => ApiRqParamError(Seq(ParamError(member, "plyrhub.member.already.exists")))
          case MemberNonValidRankings(member: String, nonValidRankings: Seq[String]) => {
            ApiRqParamError(Seq(ParamError(member, "plyrhub.member.non.valid.rankings", Some(nonValidRankings.mkString(",")))))
          }
        }

        DefaultAction()
          .withParams(memberParams)
          .withPathValues(MEMBER_ID.seedMap(member))
          .withSuccessBlock(successBlock)
          .launch[MemberScoreMsg, MemberScorer]

    }

}
