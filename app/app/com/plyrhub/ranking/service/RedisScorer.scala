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

package com.plyrhub.ranking.service

import akka.actor.{Actor, ActorLogging, Props}
import com.plyrhub.ranking.service.RankingRepo._

import scala.concurrent.Future

object RedisScorer{

  case class Score(owner:String, member:String, rankings:Seq[String], score:Int, opId:String)

  def props(): Props = Props(classOf[RedisScorer])

}

class RedisScorer extends Actor with ActorLogging{
  override def receive = ???

  // Redis Scorer
  //  -> score
  //  -> update Mongo (if fail ->) --->> doesn't matter we can reconstruct the ranking for the user (we have the data)
  //                               --->> done through the recollector (looks for the non-confirmed operations and reconstrcut for that user)

/*
  def storePosition(ranking: String, member: String, score: Long): Future[Long] = {

    //import redis.dispatcher
    redis.zAdd(ranking, (member, score))

  }
*/

}
