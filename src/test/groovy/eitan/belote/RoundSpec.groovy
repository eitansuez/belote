package eitan.belote

import spock.lang.Specification

import static eitan.belote.Suite.Carreau
import static eitan.belote.Suite.Coeur
import static eitan.belote.Suite.Pique
import static eitan.belote.Suite.Trefle

class RoundSpec extends Specification
{
  Player eitan, rony, johnny, corinne
  def players

  def setup()
  {
    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")

    players = [eitan, johnny, corinne, rony]
  }

  def "mono-suite round, largest card wins"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suite: Coeur),
        new Card(type: CardType.Dix, suite: Coeur),
        new Card(type: CardType.Sept, suite: Coeur),
        new Card(type: CardType.Valet, suite: Coeur)
    ]
    def round = new Round(cards: cards, players: players, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 23
    round.winner == eitan
  }

  def "all atout round, largest card wins"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suite: Coeur),
        new Card(type: CardType.Dix, suite: Coeur),
        new Card(type: CardType.Sept, suite: Coeur),
        new Card(type: CardType.Valet, suite: Coeur)
    ]
    def round = new Round(cards: cards, players: players, atout: Coeur)

    when:
    round.resolve()

    then:
    round.points == 41
    round.winner == rony
  }

  def "cut with large atout"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suite: Coeur),
        new Card(type: CardType.Dix, suite: Coeur),
        new Card(type: CardType.Neuf, suite: Trefle),
        new Card(type: CardType.Dame, suite: Coeur)
    ]
    def round = new Round(cards: cards, players: players, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 38
    round.winner == corinne
  }

  def "cut with small atout"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suite: Coeur),
        new Card(type: CardType.Dix, suite: Coeur),
        new Card(type: CardType.Sept, suite: Trefle),
        new Card(type: CardType.Dame, suite: Coeur)
    ]
    def round = new Round(cards: cards, players: players, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 24
    round.winner == corinne
  }

  def "ask for a suite that no one has"() {
    given:
    def cards = [
        new Card(type: CardType.Sept, suite: Coeur),
        new Card(type: CardType.Dix, suite: Pique),
        new Card(type: CardType.Roi, suite: Carreau),
        new Card(type: CardType.Huit, suite: Carreau)
    ]
    def round = new Round(cards: cards, players: players, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 14
    round.winner == eitan
  }

}
