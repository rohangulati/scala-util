package com.rohangulati.util.concurrent

import java.util.concurrent.atomic.AtomicInteger
import java.util.function.IntUnaryOperator

import com.rohangulati.util.concurrent.CountingReference.DecrementUntil

/**
  * A thread safe implementation where the reference is acquired on the first call to
  * {@link CountingReference#acquire}. This class keeps track of the number of times this reference
  * was acquired and released. When the reference is the number of times it was acquired,
  * {@link CountingReference#releaseFn} function is called. After the reference is released,
  * subsequent calls to {@link CountingReference#release} are no-op
  *
  * @param acquireFn the function to created the reference
  * @param releaseFn the function to release the acquired reference
  * @tparam T
  */
case class CountingReference[T](acquireFn: () => T, releaseFn: T => Unit)
    extends Reference[T] {

  private[this] val DECREMENT = DecrementUntil(0)

  private[this] val counter = new AtomicInteger(0)

  private[this] var reference: Option[T] = None

  override def acquire(): T = {
    if (reference.isEmpty) {
      // call the acquire function only when the reference is empty
      synchronized {
        reference match {
          case None => reference = Some(acquireFn())
        }
      }
    }
    counter.incrementAndGet()
    reference.get
  }

  override def release(): Unit = {
    val newValue = counter.updateAndGet(DECREMENT)
    if (newValue == 0 && reference.nonEmpty) {
      // release the reference when the counter returns to zero
      synchronized {
        reference match {
          case Some(x) =>
            releaseFn(x)
            reference = None
        }
      }
    }
  }

  /**
    * Returns the count of times this reference has been acquired
    * @return
    */
  def count(): Int = counter.get
}

object CountingReference {

  /**
    * An operation that decrements the operands passed until the operands equals the terminal value
    * Returns decremented value until operand reaches the termial value
    * @param terminal the terminal value
    */
  case class DecrementUntil(terminal: Int) extends IntUnaryOperator {
    override def applyAsInt(operand: Int): Int = {
      if (operand <= terminal) terminal else operand - 1
    }
  }
}
