package eitan.belote

import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import static eitan.belote.SpringExtension.SpringExtProvider

@Controller
public class BeloteController {

    @Autowired
    ActorSystem system

    ActorRef stompActor

    @MessageMapping("/newGame")
    void newGame() throws Exception {

        if (stompActor == null) {
            stompActor = system.actorOf(
                SpringExtProvider.get(system).props("StompActor"), "stompActor")
        }

        def eitan = new Player(name: "Eitan", actorRef: stompActor)
        def johnny = new Player(name: "Johnny", actorRef: stompActor)
        def corinne = new Player(name: "Corinne", actorRef: stompActor)
        def rony = new Player(name: "Rony", actorRef: stompActor)

        def partie = new Partie(
            team1: new Team(first: eitan, second: rony),
            team2: new Team(first: johnny, second: corinne),
            actorRef: stompActor
        )

        partie.begin()
        def game = partie.nextGame()
        game.play()
    }
}
