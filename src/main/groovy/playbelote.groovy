import eitan.belote.*
import static eitan.belote.Suit.Trefle

def eitan = new Player(name: "Eitan", strategy: new CliStrategy())
def johnny = new Player(name: "Johnny")
def corinne = new Player(name: "Corinne")
def rony = new Player(name: "Rony")

Partie partie = new Partie(
    team1: new Team(first: eitan, second: rony),
    team2: new Team(first: johnny, second: corinne)
)
partie.begin()

Game game = partie.nextGame()
game.begin()

game.envoi(Trefle, eitan)

game.playRandomly()
game.finalizeScore()


