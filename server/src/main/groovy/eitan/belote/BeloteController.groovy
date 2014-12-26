package eitan.belote

import akka.actor.ActorRef
import akka.actor.ActorSystem
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller
import static eitan.belote.SpringExtension.SpringExtProvider

@Controller
@Slf4j
public class BeloteController {

    @Autowired
    ActorSystem system

    ActorRef stompActor

    @Autowired
    RemoteStrategy remoteStrategy

    Game game

    @MessageMapping("/newGame")
    void newGame() throws Exception {

        if (stompActor == null) {
            stompActor = system.actorOf(
                SpringExtProvider.get(system).props("StompActor"), "stompActor")
        }

        def eitan = new Player(name: "Eitan", actorRef: stompActor, strategy: remoteStrategy)
        remoteStrategy.actorRef = stompActor
        def johnny = new Player(name: "Johnny", actorRef: stompActor)
        def corinne = new Player(name: "Corinne", actorRef: stompActor)
        def rony = new Player(name: "Rony", actorRef: stompActor)

        def partie = new Partie(
            team1: new Team(first: eitan, second: rony),
            team2: new Team(first: johnny, second: corinne),
            actorRef: stompActor
        )

        partie.begin()
        game = partie.nextGame()
        game.begin()
    }

    @MessageMapping("/respond")
    void respond(BeloteEvent event) {
        if ("envoi" == event.name)
        {
            game.envoi()
        }
        else if ("pass" == event.name)
        {
            game.pass()
        }
        else if ("playerChooses" == event.name)
        {
            String cardName = event.args[0]
            log.info("received card name from client: "+cardName)
            Card card = Card.fromName(cardName)
            game.playerChooses(card)
        }
        else {
            console.error("Received unknown/unsupported event name: "+event.name+" from client.")
        }
    }

}
