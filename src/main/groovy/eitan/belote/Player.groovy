package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class Player
{
  String name
  List<Card> hand = []
  Team team
  Strategy strategy = new RandomStrategy(player: this)

  void setStrategy(Strategy s)
  {
    this.strategy = s
    s.player = this
  }

  def receiveCard(Card card)
  {
    hand << card
  }

  def receiveCards(List<Card> cards)
  {
    cards.each { card ->
      receiveCard(card)
    }
  }

  @Override
  String toString() {
    name
  }

  Card chooseCard(Round round)
  {
    // TODO:  strategy will likely also need to have access to past rounds
    def validCards = validCards(round)
    def card = strategy.chooseCard(validCards, round)
    assert validCards.contains(card)
    card
  }

  boolean envoi(Card candidate) {
    strategy.envoi(candidate)
  }
  Suit envoi() {
    strategy.envoi()
  }

  def playCard(Card card)
  {
    assert hand.contains(card)

    if (hand.remove(card)) {
      return card
    }
  }

  void showHand() {
    log.info("${this}'s hand:")
    hand.eachWithIndex { Card card, index ->
      def displayIndex = index + 1
      log.info("\t[${displayIndex}] ${card}")
    }
  }

  boolean isMyPartner(Player player)
  {
    player.team == team
  }

  Set<Card> validCards(Round round)
  {
    if (round.empty)
    {
      return hand
    }

    if (round.requestedAtout())
    {
      Set<Card> higherAtouts = findHigherAtouts(round)
      if (higherAtouts) {
        return higherAtouts
      }

      Set<Card> allAtouts = hand.findAll { card ->
        card.suit == round.atout
      }
      if (allAtouts) {
        return allAtouts
      }
      return hand
    }

    Set<Card> matchingSuit = hand.findAll { card -> card.suit == round.requestedSuit() }
    if (matchingSuit) {
      return matchingSuit
    }

    boolean haveToCut = (! isMyPartner(round.master()) )

    if (haveToCut && haveAtout(round.atout))
    {
      if (round.containsAtout())
      {
        Set<Card> higherAtouts = findHigherAtouts(round)
        if (higherAtouts) {
          return higherAtouts
        }
      }

      Set<Card> allAtouts = hand.findAll { card ->
        card.suit == round.atout
      }
      if (allAtouts) {
        return allAtouts
      }

    }

    hand
  }

  private Set<Card> findHigherAtouts(Round round)
  {
    Suit atout = round.atout
    Card highestAtout = round.highest(round.atouts())
    hand.findAll { card ->
      (card.suit == atout) && (card.points(atout) > highestAtout.points(atout))
    }
  }

  private boolean haveAtout(Suit atout)
  {
    hand.find { card -> card.suit == atout }
  }
}
