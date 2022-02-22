package com.hpe.registry

import zio.RIO
import zio.clock.Clock

package object query {
  type ClockIO[T] = RIO[Clock, T]
}
