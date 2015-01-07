package eitan.belote

import spock.lang.Specification

import static eitan.belote.Suit.Trefle

class DealerSpec extends Specification
{
  Dealer dealer
  Player eitan, rony, johnny, corinne

  def setup()
  {
    dealer = new Dealer()

    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")
  }

  def "dealer should have a deck"()
  {
    expect:
    dealer.deck != null
    dealer.deck.full()
  }

  def "should deal 20 cards in first phase"()
  {
    when:
    dealer.deal([eitan, rony, johnny, corinne])

    then:
    dealer.deck.size() == 12
  }

  def "should deal remainder of cards after selection phase"()
  {
    given:
    dealer.deal([eitan, rony, johnny, corinne])
    Card candidate = dealer.turnUpCandidateCard()

    when:
    dealer.dealRemaining([eitan, rony, johnny, corinne], eitan, Trefle)

    then:
    dealer.deck.empty()
    eitan.hand.contains(candidate)
  }

}
