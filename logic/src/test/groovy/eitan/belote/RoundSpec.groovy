package eitan.belote

import spock.lang.Specification

import static Suit.Carreau
import static Suit.Coeur
import static Suit.Pique
import static Suit.Trefle

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

  Game gameWithAtout(Suit atout)
  {
    def game = new Game(atout: atout)
    game.initScores()
    game
  }

  def "can tell round size"()
  {
    when:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Sept, suit: Coeur),
        new Card(type: CardType.Valet, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    then:
    round.size() == 4
  }

  def "master card for a round is first card when round size is 1"()
  {
    when:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: [eitan], game: gameWithAtout(Trefle))

    then:
    round.size() == 1

    and:
    round.masterCard() == cards.first()
    round.master() == eitan
  }

  def "master card in a round should be strongest when of same suit"()
  {
    when:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Sept, suit: Coeur),
        new Card(type: CardType.Valet, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    then:
    round.masterCard() == cards.first()
    round.master() == eitan
  }

  def "master card in a round should be strongest when of same suit, which happens to be atout"()
  {
    when:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Sept, suit: Coeur),
        new Card(type: CardType.Valet, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Coeur))

    then:
    round.masterCard() == cards.last()
    round.master() == rony
  }

  def "master card in a round where cut is highest cutter"()
  {
    when:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Pique),
        new Card(type: CardType.Sept, suit: Pique),
        new Card(type: CardType.Valet, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Pique))

    then:
    round.masterCard() == cards[1]
    round.master() == johnny
  }

  def "8 wins over 7 even though they're both 0 points"()
  {
    when:
    def cards = [
        new Card(type: CardType.Sept, suit: Pique),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Huit, suit: Pique),
        new Card(type: CardType.Valet, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    then:
    round.masterCard() == new Card(type: CardType.Huit, suit: Pique)
    round.master() == corinne
  }

  def "9 wins over 8 and 7 even though they're all 0 points"()
  {
    when:
    def cards = [
        new Card(type: CardType.Sept, suit: Pique),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Huit, suit: Pique),
        new Card(type: CardType.Neuf, suit: Pique)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    then:
    round.masterCard() == cards[3]
    round.master() == players[3]
  }

  def "mono-suit round, largest card wins"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Sept, suit: Coeur),
        new Card(type: CardType.Valet, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    when:
    round.resolve()

    then:
    round.points == 23
    round.winner == eitan
  }

  def "all atout round, largest card wins"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Sept, suit: Coeur),
        new Card(type: CardType.Valet, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Coeur))

    when:
    round.resolve()

    then:
    round.points == 41
    round.winner == rony
  }

  def "cut with large atout"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Neuf, suit: Trefle),
        new Card(type: CardType.Dame, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    when:
    round.resolve()

    then:
    round.points == 38
    round.winner == corinne
  }

  def "cut with small atout"() {
    given:
    def cards = [
        new Card(type: CardType.Ace, suit: Coeur),
        new Card(type: CardType.Dix, suit: Coeur),
        new Card(type: CardType.Sept, suit: Trefle),
        new Card(type: CardType.Dame, suit: Coeur)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    when:
    round.resolve()

    then:
    round.points == 24
    round.winner == corinne
  }

  def "ask for a suit that no one has"() {
    given:
    def cards = [
        new Card(type: CardType.Sept, suit: Coeur),
        new Card(type: CardType.Dix, suit: Pique),
        new Card(type: CardType.Roi, suit: Carreau),
        new Card(type: CardType.Huit, suit: Carreau)
    ]
    def round = new Round(cards: cards, players: players, game: gameWithAtout(Trefle))

    when:
    round.resolve()

    then:
    round.points == 14
    round.winner == eitan
  }

}
