package net.cs.core.objs

import org.apache.commons.lang3.builder.HashCodeBuilder

case class CardUser(name: String, creditScore: Int, salary: Int) {

  def canEqual(a: Any) = a.isInstanceOf[CardUser]

  override def equals(obj: Any): Boolean =
    obj match {
      case c: CardUser => {
        c.canEqual(this) &&
          this.name == c.name &&
          this.creditScore == c.creditScore &&
          this.salary == c.salary
      }
      case _ => false
    }

  override def hashCode(): Int = new HashCodeBuilder()
    .append(name)
    .append(creditScore)
    .append(salary)
    .toHashCode
}
