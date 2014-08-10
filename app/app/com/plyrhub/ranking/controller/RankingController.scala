package com.plyrhub.ranking.controller

import com.plyrhub.api.request.ApiAction._
import com.plyrhub.api.request._
import com.plyrhub.api.utils.ApiDefaults.ActionDefaults._
import com.plyrhub.api.utils.HttpResults._
import com.plyrhub.core.log.Loggable
import com.plyrhub.core.protocol.ServiceSuccess
import com.plyrhub.ranking.model.RankingName
import com.plyrhub.ranking.service.RankingCreator
import com.plyrhub.ranking.service.protocol.{CreateRanking, CreateRanking2, RankingCreated}
import play.api.mvc._

object RankingController extends Controller with Loggable {

  val paramsView3 = Seq(RANKING, FROM_TOP, FROM_BOTTOM, STATE_TRUE, Param[B, B]("name"))

    def view4(ranking: String) =
      AuthAction.async {

        implicit request =>

          DefaultAction().launch[CreateRanking2, RankingCreator]
      }

  def view3(ranking: String) =
    AuthAction.async {

      implicit request =>

        val theSuccess:PartialFunction[ServiceSuccess, Result] = {

        case RankingCreated(rn:RankingName) =>
          println("en v3")
          API_SUCCESS(Some(rn), None)

      }

         DefaultAction()
          .withParams(paramsView3)
          .withPathValues(request => Map("ranking" -> ranking))
          .withSuccessBlock(theSuccess)
          .launch[CreateRanking, RankingCreator]

    }

  def view5(ranking: String) =
    AuthAction.async {

      implicit request =>

        DefaultAction()
          .withParams(paramsView3)
          .withPathValues(request => Map("ranking" -> ranking))
          .launch[CreateRanking, RankingCreator]

    }

}

