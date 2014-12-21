package eitan.belote

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.stereotype.Controller

@Controller
public class BeloteController {

    @Autowired
    StompUI stompUI

    @MessageMapping("/newGame")
    void newGame() throws Exception {

        def eitan = new Player(name: "Eitan", strategy: new CliStrategy(), ui: stompUI)
        def johnny = new Player(name: "Johnny", ui: stompUI)
        def corinne = new Player(name: "Corinne", ui: stompUI)
        def rony = new Player(name: "Rony", ui: stompUI)

        def partie = new Partie(
            team1: new Team(first: eitan, second: rony),
            team2: new Team(first: johnny, second: corinne),
            ui: stompUI
        )

        partie.begin()
        def game = partie.nextGame()
        game.play()
    }
}
