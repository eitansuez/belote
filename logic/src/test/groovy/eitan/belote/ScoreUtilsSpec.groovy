package eitan.belote

import spock.lang.Specification

import static eitan.belote.ScoreUtils.roundScore

class ScoreUtilsSpec extends Specification
{
  def "should round score for total #total to #rounded"(total, rounded) {
    expect:
    roundScore(total) == rounded

    where:
    total | rounded
       65 |  70
       81 |  80
      162 | 160
        4 |   0
        6 |  10
  }
}
