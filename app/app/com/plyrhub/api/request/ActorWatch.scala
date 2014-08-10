/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.plyrhub.api.request

import akka.actor.{Terminated, ActorRef, Actor}

class ActorWatch extends Actor{

  var counter = 0

  override def receive = {

    case WatchThis(a) => {
      println("Watching...." + a.toString)
      context.watch(a)
    }

    case Terminated(a) => {
      counter += 1
      println("Terminated.." + (counter) + "..." + a.toString())
    }

  }
}


case class WatchThis(a:ActorRef)