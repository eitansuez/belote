package eitan.belote

class CliStrategy implements Strategy
{
  Player player

  @Override
  Card chooseCard(Set<Card> validCards, Round round)
  {
    //
    // prompt in the console:
    // -- here's the round, context: atout xx etc..
    // -- here are your cards
    // [1] ace de trefle
    // [2] dame de coeur (valid to play)
    // [3] ...

    player.showHand()

    // --prompt>
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in))

    println 'choose one valid card.. (enter a number): '
    def input = br.readLine()
    def cardIndex = input.toInteger() - 1

    def card = player.hand[cardIndex-1]
    println "chose to play.. ${card}"
    card
  }
}
