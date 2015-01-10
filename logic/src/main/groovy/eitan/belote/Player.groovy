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
  boolean haveBeloteRebelote = false

  void setStrategy(Strategy s)
  {
    this.strategy = s
    s.player = this
  }

  def receiveCard(Card card, Suit atout = null)
  {
    def order = addCard(card, atout)
    emit('receiveCard', [this, card, order])
  }

  private addCard(Card card, Suit atout)
  {
    hand << card
    def orderBy = new OrderBy([
        { it.suit == atout ? 1 : 0 },
        { it.suit.ordinal() },
        { it.points(atout) },
        { it.type.ordinal() }
    ])
    def newHand = hand.sort(false, orderBy)

    def order = newHand.collect { hand.indexOf(it) }
    hand = newHand
    order
  }

  def receiveCards(List<Card> cards, Suit atout = null)
  {
    cards.each { card ->
      receiveCard(card, atout)
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

  def playCard(Card card, Suit atout, Delay delay = Delay.Standard)
  {
    assert hand.contains(card)

    String bRText = beloteRebeloteClaim(card, atout)

    if (hand.remove(card)) {
      emit('playCard', [this, card, bRText], delay)
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
      (card.suit == atout) && (card.higherThan(highestAtout, atout))
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

  void setBeloteRebelote(boolean haveIt)
  {
    haveBeloteRebelote = haveIt
  }

  String beloteRebeloteClaim(Card card, Suit atout)
  {
    if (!haveBeloteRebelote) return ''
    if ( (card.type == Roi || card.type == Dame) && card.suit == atout) {
      return (hasBeloteRebelote(atout)) ? 'Belote' : 'Rebelote'
    }
    return ''
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
