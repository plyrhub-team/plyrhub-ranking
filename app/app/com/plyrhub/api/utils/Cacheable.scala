/*
 * Copyright (C) 2014  Enrique Aguilar Esnaola
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation (version 3 of the
 *     License).
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.plyrhub.api.utils

import com.plyrhub.core.log.Loggable
import play.api.Play.current
import play.api.cache.Cache

trait Cacheable[T] extends Loggable {

  protected val cacheKey: String

  def set(value: Map[String, T]): Unit = {
    Cache.set(cacheKey, value)
  }

  def get(valueKey: String): Option[T] = {

    Cache.getAs[Map[String, T]](cacheKey).flatMap(_.get(valueKey))

  }

}
