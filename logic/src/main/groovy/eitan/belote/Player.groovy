package eitan.belote

import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j

import static eitan.belote.CardType.Dame
import static eitan.belote.CardType.Roi

@EqualsAndHashCode(includes="name")
@Slf4j
class Player implements Emitter
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
    emit('receiveCard', [this, card])
  }

  def receiveCards(List<Card> cards)
  {
    cards.each { card ->
      receiveCard(card)
    }
  }

  void offer(Game game, Card candidate) {
    strategy.offer(game, candidate)
  }
  void offer(Game game) {
    strategy.offer(game)
  }
  void play(Game game, Round round) {
    def validCards = validCards(round)
    strategy.play(game, validCards, round)
  }

  def playCard(Card card)
  {
    assert hand.contains(card)

    if (hand.remove(card)) {
      emit('playCard', [this, card])
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
        card.suit == round.game.atout
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

    if (haveToCut && haveAtout(round.game.atout))
    {
      if (round.containsAtout())
      {
        Set<Card> higherAtouts = findHigherAtouts(round)
        if (higherAtouts) {
          return higherAtouts
        }
      }

      Set<Card> allAtouts = hand.findAll { card ->
        card.suit == round.game.atout
      }
      if (allAtouts) {
        return allAtouts
      }

    }

    hand
  }

  private Set<Card> findHigherAtouts(Round round)
  {
    Suit atout = round.game.atout
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

  // events
  void gameDone()
  {
    hand.clear()
  }

  @Override
  String toString() {
    name
  }


}
