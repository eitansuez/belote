package eitan.belote

import spock.lang.Specification

import static eitan.belote.Suite.Carreau
import static eitan.belote.Suite.Coeur
import static eitan.belote.Suite.Pique
import static eitan.belote.Suite.Trefle

class HandSpec extends Specification
{
  Player eitan, rony, johnny, corinne

  def setup()
  {
    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")
  }

  def "mono-suite hand, largest card wins"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Sept, suite: Coeur)): corinne,
        (new Card(type: CardType.Valet, suite: Coeur)): rony]
    def hand = new Hand(cards: cards, atout: Trefle)

    when:
    hand.resolve()

    then:
    hand.points == 23
    hand.winner == eitan
  }

  def "all atout hand, largest card wins"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Sept, suite: Coeur)): corinne,
        (new Card(type: CardType.Valet, suite: Coeur)): rony]
    def hand = new Hand(cards: cards, atout: Coeur)

    when:
    hand.resolve()

    then:
    hand.points == 41
    hand.winner == rony
  }

  def "cut with large atout"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Neuf, suite: Trefle)): corinne,
        (new Card(type: CardType.Dame, suite: Coeur)): rony]
    def hand = new Hand(cards: cards, atout: Trefle)

    when:
    hand.resolve()

    then:
    hand.points == 38
    hand.winner == corinne
  }

  def "cut with small atout"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Sept, suite: Trefle)): corinne,
        (new Card(type: CardType.Dame, suite: Coeur)): rony]
    def hand = new Hand(cards: cards, atout: Trefle)

    when:
    hand.resolve()

    then:
    hand.points == 24
    hand.winner == corinne
  }

  def "ask for a suite that no one has"() {
    given:
    def cards = [
        (new Card(type: CardType.Sept, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Pique)): johnny,
        (new Card(type: CardType.Roi, suite: Carreau)): corinne,
        (new Card(type: CardType.Huit, suite: Carreau)): rony]
    def hand = new Hand(cards: cards, atout: Trefle)

    when:
    hand.resolve()

    then:
    hand.points == 14
    hand.winner == eitan
  }

}
