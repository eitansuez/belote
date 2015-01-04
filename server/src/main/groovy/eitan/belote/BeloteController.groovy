package eitan.belote

import akka.actor.ActorRef
import akka.actor.ActorSystem
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

import java.security.Principal

import static eitan.belote.SpringExtension.SpringExtProvider

@Controller
@Slf4j
public class BeloteController {

    @Autowired ActorSystem system
    ActorRef stompActor
    Partie partie
    def users = [] as Set
    def builder = [team1: [first: 'Bot1', second: 'Bot2'],
                   team2: [first: 'Bot3', second: 'Bot4']]


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
            log.error("Received unknown/unsupported event name: "+event.name+" from client.")
        }
    }


    @MessageMapping("/startPartie")
    void startPartie() throws Exception {

        if (stompActor == null) {
            stompActor = system.actorOf(
                SpringExtProvider.get(system).props("StompActor"), "stompActor")
        }

        def players = [builder.team1.first, builder.team1.second, builder.team2.first, builder.team2.second].collect { name ->
            def player = new Player(name: name, actorRef: stompActor)
            if (users.contains(name))
            {
                player.strategy = new RemoteStrategy(actorRef: stompActor)
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

    @MessageMapping("/enterPartie")
    def enterPartie(Principal user)
    {
        users << user.name
        builder
    }

    @MessageMapping("/joinPartie")
    @SendTo("/topic/enterPartie")
    def joinPartie(Principal user, Map<String, String> msg)
    {
        builder[msg.team][msg.position] = user.name
        builder
    }


}
