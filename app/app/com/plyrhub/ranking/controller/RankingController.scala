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
import com.plyrhub.ranking.service.RankingCreator
import com.plyrhub.ranking.service.protocol.{RankingAlreadyExist, RankingCreated, RankingCreationMsg}
import play.api.mvc.{Controller, Result}

object RankingController extends Controller with Loggable {

  /*
    Creates a new ranking
   */
  val createOrUpdateParams = Seq(RANKING_ID, BODY)

  def createOrUpdate(rnk: String) =

    AuthAction.async {

      implicit request =>

        val successBlock: PartialFunction[ServiceSuccess, Result] = {
          case RankingCreated(rn: String) => API_SIMPLE_CREATED
          case RankingAlreadyExist(rn: String) => ApiRqParamError(Seq(ParamError(rn, "plyrhub.ranking.already.exists")))
        }

        DefaultAction()
          .withParams(createOrUpdateParams)
          .withPathValues(RANKING_ID.seedMap(rnk))
          .withSuccessBlock(successBlock)
          .launch[RankingCreationMsg, RankingCreator]

    }

}
