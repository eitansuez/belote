package eitan.belote

import groovy.util.logging.Slf4j

@Slf4j
class Dealer
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
    log.info("turning over candidate card ${candidate}")
    candidate = deck.takeCard()
    candidate
  }

  def dealRemaining(List<Player> players, Player committer)
  {
    log.info("Dealing remaining cards..")
    assert deck.size() == 11

    committer.receiveCard(candidate)
    candidate = null

    players.each { player ->
      int count = (player == committer) ? 2 : 3
      dealToPlayer(player, deck.takeCards(count))
    }
  }

  private void dealToPlayer(Player player, List<Card> cards)
  {
    player.receiveCards(cards)
  }


}
