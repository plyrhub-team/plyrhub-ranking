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

package com.plyrhub.api.conf

import com.plyrhub.core.Plyrhub
import play.Plugin
import play.api.Application
import play.api.libs.concurrent.Akka

import play.api.Play.current

class PlyrhubBootstrap(app: Application) extends Plugin {

  override def onStart() = {

    startupAkkaSystemForPlyrhub

  }

  def startupAkkaSystemForPlyrhub = {
    Plyrhub.installRuntime(Akka.system)
  }

}
