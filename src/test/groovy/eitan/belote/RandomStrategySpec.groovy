package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Roi
import static eitan.belote.Suit.Coeur
import static eitan.belote.Suit.Trefle

class RandomStrategySpec extends Specification
{
  Strategy strategy

  def setup()
  {
    strategy = new RandomStrategy()
  }

  def "should pass"()
  {
    when:
    Card card = new Card(type: Ace, suit: Trefle)

    then:
    !strategy.envoi(card)
  }

  def "should pick a card among the valids"()
  {
    given:
    def validCards = [new Card(type: Ace, suit: Trefle), new Card(type: Roi, suit: Coeur)] as Set

    when:
    Card chosen = strategy.chooseCard(validCards, null)

    then:
    validCards.contains chosen
  }

}
