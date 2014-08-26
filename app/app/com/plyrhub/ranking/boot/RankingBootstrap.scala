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

package com.plyrhub.ranking.boot

import com.plyrhub.core.PlyrhubRT
import play.Plugin
import play.api.Application
import play.api.Play.current
import play.api.libs.concurrent.Akka

class RankingBootstrap(app: Application) extends Plugin {

  override def onStart() = {

    startupAkkaSystemForPlyrhub

  }

  def startupAkkaSystemForPlyrhub = {
    PlyrhubRT.installRuntime(Akka.system)
  }

}
