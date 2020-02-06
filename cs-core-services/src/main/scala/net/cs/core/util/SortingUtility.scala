package net.cs.core.util

import net.cs.core.objs.Cards.CreditCard

object SortingUtility {

  type Sorter[T] = (T, T) => Boolean

  trait Sort[T] {
    def sort(seq: Seq[T])(implicit sorter: Sorter[T]): Seq[T]
  }


  object SortableInstances {

   /* implicit val doubleInstance = new Sort[Double] {
      override def sort(seq: List[Double])(implicit sorter: Sorter[Double]): List[Double] = seq.sortWith(sorter)
    }
*/
    implicit val creditCardInstance = new Sort[CreditCard] {
      override def sort(seq: Seq[CreditCard])(implicit sorter: Sorter[CreditCard]): Seq[CreditCard] = seq.sortWith(sorter)
    }
  }

  object SortableInterface {
    //def sortArray[T](value: List[T])(implicit sorter: Sorter[T], sortable: Sort[T]) = sortable.sort(value)

    implicit class SortOps[T](value: Seq[T])(implicit sorter: Sorter[T], sortable: Sort[T]) {
      def sortSeq: Seq[T] = sortable.sort(value)
    }
  }

}
