package eitan.belote

import spock.lang.Specification

class SuiteSpec extends Specification
{
  def "check toString"()
  {
    expect:
    Suite.Trefle.toString() == "Suite: Trefle"
  }

}
