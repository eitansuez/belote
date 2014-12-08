package eitan.belote

import spock.lang.Specification

import static eitan.belote.Suite.Carreau
import static eitan.belote.Suite.Coeur
import static eitan.belote.Suite.Pique
import static eitan.belote.Suite.Trefle

class RoundSpec extends Specification
{
  Player eitan, rony, johnny, corinne

  def setup()
  {
    eitan = new Player(name: "Eitan")
    johnny = new Player(name: "Johnny")
    corinne = new Player(name: "Corinne")
    rony = new Player(name: "Rony")
  }

  def "mono-suite round, largest card wins"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Sept, suite: Coeur)): corinne,
        (new Card(type: CardType.Valet, suite: Coeur)): rony]
    def round = new Round(cards: cards, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 23
    round.winner == eitan
  }

  def "all atout round, largest card wins"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Sept, suite: Coeur)): corinne,
        (new Card(type: CardType.Valet, suite: Coeur)): rony]
    def round = new Round(cards: cards, atout: Coeur)

    when:
    round.resolve()

    then:
    round.points == 41
    round.winner == rony
  }

  def "cut with large atout"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Neuf, suite: Trefle)): corinne,
        (new Card(type: CardType.Dame, suite: Coeur)): rony]
    def round = new Round(cards: cards, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 38
    round.winner == corinne
  }

  def "cut with small atout"() {
    given:
    def cards = [
        (new Card(type: CardType.Ace, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Coeur)): johnny,
        (new Card(type: CardType.Sept, suite: Trefle)): corinne,
        (new Card(type: CardType.Dame, suite: Coeur)): rony]
    def round = new Round(cards: cards, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 24
    round.winner == corinne
  }

  def "ask for a suite that no one has"() {
    given:
    def cards = [
        (new Card(type: CardType.Sept, suite: Coeur)): eitan,
        (new Card(type: CardType.Dix, suite: Pique)): johnny,
        (new Card(type: CardType.Roi, suite: Carreau)): corinne,
        (new Card(type: CardType.Huit, suite: Carreau)): rony]
    def round = new Round(cards: cards, atout: Trefle)

    when:
    round.resolve()

    then:
    round.points == 14
    round.winner == eitan
  }

}
