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

package com.plyrhub.odd

import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._

import org.junit.Test

class Futures {

  @Test
  def t1() = {


    val f1 = Future{
      67
    }

    val f2 = for {
      i <- f1
      if i>70
    } yield i



    val f = for {
      a ← Future{10 / 2} // 10 / 2 = 5
      b ← Future{a + 1} // 5 + 1 = 6
      c ← Future{a - 1} // 5 - 1 = 4
      if c > 25 // Future.filter
    } yield b * c // 6 * 4 = 24

    // Note that the execution of futures a, b, and c
    // are not done in parallel.

    val result = Await.result(f, 1 second)

    println("kkkk..." + result)
  }

}
