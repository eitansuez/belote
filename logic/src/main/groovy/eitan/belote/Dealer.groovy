package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class Dealer implements Emitter
{
  Deck deck = new Deck()

  def deal(List<Player> players)
  {
    assert players?.size() == 4

    players.each { player ->
      dealToPlayer(player, deck.takeCards(3))
    }

    players.each { player ->
      dealToPlayer(player, deck.takeCards(2))
    }
  }

  Card candidate

  Card turnUpCandidateCard() {
    candidate = deck.takeCard()
    emit('turnUpCard', [candidate])
    candidate
  }

  def dealRemaining(List<Player> players, Player committer, Suit atout)
  {
    log.info("Dealing remaining cards..")
    assert deck.size() == 11

    committer.receiveCard(candidate, atout)
    candidate = null

    players.each { player ->
      int count = (player == committer) ? 2 : 3
      dealToPlayer(player, deck.takeCards(count), atout)
    }
  }

  private void dealToPlayer(Player player, List<Card> cards, Suit atout = null)
  {
    player.receiveCards(cards, atout)
  }


}
