package eitan.belote;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class BeloteController {

    @MessageMapping("/newGame")
    @SendTo("/topic/belote")
    void newGame() throws Exception {

        def eitan = new Player(name: "Eitan", strategy: new CliStrategy())
        def johnny = new Player(name: "Johnny")
        def corinne = new Player(name: "Corinne")
        def rony = new Player(name: "Rony")

        def partie = new Partie(
            team1: new Team(first: eitan, second: rony),
            team2: new Team(first: johnny, second: corinne),
            ui: new StompUI()
        )

        partie.begin()
        def game = partie.nextGame()
        game.begin()
    }
}
