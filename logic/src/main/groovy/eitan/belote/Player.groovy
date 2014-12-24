package eitan.belote

import groovy.util.logging.Slf4j

import static eitan.belote.CardType.Dame
import static eitan.belote.CardType.Roi

@Slf4j
class Player implements Emitter
{
  String name
  List<Card> hand = []
  Team team
  Strategy strategy = new RandomStrategy(player: this)


  void gameDone()
  {
    hand.clear()
  }

  void setStrategy(Strategy s)
  {
    this.strategy = s
    s.player = this
  }

  def receiveCard(Card card)
  {
    hand << card
    emit("receiveCard", [this, card])
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
    boolean response = strategy.envoi(candidate)

    emit("playerDecision", [this, response, candidate.suit])

    response
  }

  Suit envoi() {
    def suit = strategy.envoi()

    emit("playerDecision", [this, (suit != null), suit])

    suit
  }

  def playCard(Card card)
  {
    assert hand.contains(card)

    if (hand.remove(card)) {
      emit("playCard", [this, card])
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

  boolean hasBeloteRebelote(Suit atout)
  {
    hand.contains(new Card(type: Roi, suit: atout)) &&
        hand.contains(new Card(type: Dame, suit: atout))
  }
}
