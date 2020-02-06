package net.cs.core.objs
import org.apache.commons.lang3.builder.HashCodeBuilder

object Cards {

  sealed trait Card

  case class CSCard(apr: Double, cardName: String, eligibility: Double) extends Card

  case class ScoredCard(card: String, apr: Double, approvalRating: Double) extends Card

  case class CreditCard(provider: String, name: String, apr: Double, cardScore: Double) extends Card {
    def canEqual(a: Any) = a.isInstanceOf[CreditCard]

    override def equals(obj: Any): Boolean =
      obj match {
        case c: CreditCard => {
          c.canEqual(this) &&
            this.provider == c.provider &&
            this.name == c.name &&
            this.apr == c.apr &&
            this.cardScore == c.cardScore
        }
        case _ => false
      }

    override def hashCode(): Int = new HashCodeBuilder()
      .append(provider)
      .append(name)
      .append(apr)
      .append(cardScore)
      .toHashCode
  }
}