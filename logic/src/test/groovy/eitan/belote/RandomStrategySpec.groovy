package eitan.belote

import spock.lang.Specification

import static eitan.belote.CardType.Ace
import static eitan.belote.CardType.Roi
import static eitan.belote.Suit.Coeur
import static eitan.belote.Suit.Trefle

class RandomStrategySpec extends Specification
{
  Strategy strategy
  Game game

  def setup()
  {
    strategy = new RandomStrategy()
    game = Mock()
  }

  def "should pass"()
  {
    given:
    Card candidate = new Card(type: Ace, suit: Trefle)

    when:
    strategy.offer(game, candidate)

    then:
    1 * game.pass()
  }

  def "should pick a card among the valids"()
  {
    given:
    def validCards = [new Card(type: Ace, suit: Trefle), new Card(type: Roi, suit: Coeur)] as Set

    when:
    strategy.play(game, validCards, null)

    then:
    1 * game.playerChooses { card -> validCards.contains(card) }
  }

}
