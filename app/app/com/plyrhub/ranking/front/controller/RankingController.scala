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
import com.plyrhub.api.request.ApiDefaults.ActionDefaults._
import com.plyrhub.api.request.ApiHttpResults._
import com.plyrhub.api.request.{Q, Param}
import com.plyrhub.core.log.Loggable
import com.plyrhub.core.protocol.ServiceSuccess
import com.plyrhub.ranking.front.conf.RankingConfig
import com.plyrhub.ranking.model.MemberInRanking
import com.plyrhub.ranking.service.RankingRetriever.{RankingsQueryResult, RankingQueryMsg}
import com.plyrhub.ranking.service.{RankingRetriever, RankingCreator}
import com.plyrhub.ranking.service.RankingCreator._
import play.api.mvc.{Controller, Result}

object RankingController extends Controller with Loggable {

  /*
    Creates a new ranking
   */
  val createParams = Seq(RANKING_ID, BODY)

  def create(rnk: String) =

    AuthAction.async {

      implicit request =>

        val successBlock: PartialFunction[ServiceSuccess, Result] = {
          case RankingCreated(rn: String) => API_SIMPLE_CREATED
          case RankingAlreadyExist(rn: String) => ApiRqParamError(Seq(ParamError(rn, "plyrhub.ranking.already.exists")))
        }

        DefaultAction()
          .withParams(createParams)
          .withPathValues(RANKING_ID.seedMap(rnk))
          .withSuccessBlock(successBlock)
          .launch[RankingCreationMsg, RankingCreator]

    }

  /*
    Retrieves members in a ranking
   */

  val queryParams = Seq(
    RANKING_ID,
    FROM_TOP(RankingConfig.ParametersQSConstraints.fromTop),
    FROM_BOTTOM(RankingConfig.ParametersQSConstraints.fromBottom),
    PLATFORM,
    Param[Q, String]("member", "")
  )

  def query(ranking: String) =

    AuthAction.async {
      implicit request =>

        val successBlock: PartialFunction[ServiceSuccess, Result] = {
          case RankingsQueryResult(membersInRanking: List[MemberInRanking]) =>
            API_SIMPLE_CREATED
        }

        DefaultAction()
          .withParams(queryParams)
          .withSuccessBlock(successBlock)
          .withPathValues(RANKING_ID.seedMap(ranking))
          .launch[RankingQueryMsg, RankingRetriever]
    }

}
