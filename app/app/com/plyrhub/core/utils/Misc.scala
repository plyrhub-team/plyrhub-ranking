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

package com.plyrhub.core.utils

import java.util.UUID

object Misc {

  def uniqueID = UUID.randomUUID().toString

}


/*

    // Taken from: http://instagram-engineering.tumblr.com/post/10853187575/sharding-ids-at-instagram
    // TODO -- improve the implementation
    private long calculateId(String key, long nextVal){

        long currentMillis = new Date().getTime() - epochInitAt;

        long result = currentMillis << 23;                      // 41 bits for time
        result = result | (startValues.get(key) << 10);         // 13 bits for "theService"
        result = result | (nextVal % 1024);                     // 10 bits for "theNextVal"

        return result;
    }


 */