import akka.actor.ActorRef
import akka.actor.ActorSystem
import akka.actor.Props
import eitan.belote.*

ActorSystem system = ActorSystem.create("BeloteWithAkka")
ActorRef actorRef = system.actorOf(Props.create(TextUI.class))

def eitan = new Player(name: "Eitan", strategy: new CliStrategy(), actorRef: actorRef)
def johnny = new Player(name: "Johnny", actorRef: actorRef)
def corinne = new Player(name: "Corinne", actorRef: actorRef)
def rony = new Player(name: "Rony", actorRef: actorRef)

Partie partie = new Partie(
    team1: new Team(first: eitan, second: rony),
    team2: new Team(first: johnny, second: corinne),
    actorRef: actorRef
)

partie.begin()
while (!partie.done())
{
  partie.nextGame().begin()
}

println "Partie is over."




