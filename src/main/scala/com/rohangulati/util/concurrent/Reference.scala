package com.rohangulati.util.concurrent

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.IntUnaryOperator

/**
  * Holds a reference of a single value which can be acquired multiple time and should be released
  * This class is meant to be used with object pools or shared variable where it is required to
  * create the object once, reuse it multiple times and then release the reference
  * @tparam T
  */
trait Reference[T] {

  /**
    * Returns the reference value held by this object
    * @return the referenced value
    */
  def acquire(): T

  /**
    * Releases the reference of the held object
    */
  def release()
}

