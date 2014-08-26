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

package com.plyrhub.ranking.front.conf

import com.plyrhub.api.codes.ApiParams_ZZ
import com.plyrhub.core.log.Loggable
import com.plyrhub.api.model._
import com.plyrhub.api.model.StateNotActive
import com.plyrhub.api.utils.Configurer
import play.api.cache.Cache
import play.api.Play.current

object Starter extends Loggable {

  def warmup() = {

    val configurers = Seq(StateConfigurer, DeltaConfigurer)

    log.debug("Loading configurers...")
    Configurer.configure(configurers)

  }

}

object StateConfigurer extends Configurer with Loggable {

  override def configure() = {

    // TODO: configure this through Archaius
    val active = StateActive()
    val not_active = StateNotActive()
    val mapStatus = Map(active.id -> active, not_active.id -> not_active)

    log.debug("Loading Status Cache")
    State.set(mapStatus)

  }
}

object DeltaConfigurer extends Configurer with Loggable {

  override def configure() = {

    // TODO: configure this through Archaius
    val fromTop = Delta(ApiParams_ZZ.fromTop, 0, 100, 10)
    val fromBottom = Delta(ApiParams_ZZ.fromBottom, 0, 100, 10)

    log.debug("Loading Delta (fromXXX) Cache")
    Delta.set(Map(fromTop.id->fromTop, fromBottom.id -> fromBottom))

  }
}
