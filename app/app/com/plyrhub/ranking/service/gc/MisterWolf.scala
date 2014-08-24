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

package com.plyrhub.ranking.service.gc

import akka.actor.{Props, Actor, ActorLogging}
import com.plyrhub.core.context.OperationContext
import com.plyrhub.core.protocol.StartOperation
import com.plyrhub.ranking.service.protocol.MisterWolfProtocol.{FixMemberRegistration, FixRankingCreation}

class MisterWolf extends Actor with ActorLogging {

  override def receive = {

    case StartOperation(ctx: OperationContext, message: FixRankingCreation) =>
      log.error(s"Fixing RankingCreation for: ${ctx.id}, ${message.ranking}")

    case StartOperation(ctx: OperationContext, message: FixMemberRegistration) =>
      log.error(s"Fixing MemberRegistration for: ${ctx.id}, ${message.member}")

  }

}

object MisterWolf {
  def props(): Props = Props(new MisterWolf)
}