package eitan.belote

import akka.actor.ActorRef
import akka.actor.ActorSystem
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

import java.security.Principal

import static eitan.belote.SpringExtension.SpringExtProvider

@Controller
public class BeloteController {

    @Autowired
    ActorSystem system

    ActorRef stompActor

    @Autowired
    RemoteStrategy remoteStrategy

    Partie partie

    @MessageMapping("/newPartie")
    void newPartie(Principal user) throws Exception {

        if (stompActor == null) {
            stompActor = system.actorOf(
                SpringExtProvider.get(system).props("StompActor"), "stompActor")

            remoteStrategy.actorRef = stompActor
        }

        def players = [user.name, 'Rony', 'Johnny', 'Corinne'].collect { name ->
            def player = new Player(name: name, actorRef: stompActor)
            if (name == user.name)
            {
                player.strategy = remoteStrategy
            }
            player
        }

        partie = new Partie(
            team1: new Team(first: players[0], second: players[1]),
            team2: new Team(first: players[2], second: players[3]),
            actorRef: stompActor
        )

        partie.begin()
    }

    @MessageMapping("/respond")
    void respond(BeloteEvent event) {
        def game = partie.currentGame

        if ("envoi" == event.name)
        {
            if (event.args.size() > 0)
            {
                def suit = Suit.fromName(event.args[0])
                game.envoi(suit)
            }
            else
            {
                game.envoi()
            }
        }
        else if ("pass" == event.name)
        {
            game.pass()
        }
        else if ("pass2" == event.name)
        {
            game.passDeuxFois()
        }
        else if ("playerChooses" == event.name)
        {
            String cardName = event.args[0]
            Card card = Card.fromName(cardName)
            game.playerChooses(card)
        }
        else {
            console.error("Received unknown/unsupported event name: "+event.name+" from client.")
        }
    }

}
