package eitan.belote

import spock.lang.Specification

class SuiteSpec extends Specification
{
  def "toString should return suite name"()
  {
    expect:
    Suite.Trefle.toString() == "Trefle"
  }

}
