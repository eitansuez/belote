var cards = {};
var suits = {};
var players = {};
var cardsLayer, groupsLayer;
var cardSeparation, selectDelta;
var handAspectRatio = 2.5;
var played = [];
var thisPlayerName;

var table, groups;
var gameScoreArea, partieScoreArea;

var cmds = {
    receiveCard : function(playerName, cardName) {
        var useBackface = !isPlayerMe(playerName);
        var group = players[playerName];
        var card = cardFor(cardName);
        placeCards([card], group, useBackface);
    },
    turnUpCard : function(cardName) {
        turnUpCard(cardFor(cardName));
    },
    playerDecision : function(playerName, envoi, suitName) {
        var passesText = (suitName ? " passes at " : " passes again.");
        var text = playerName + (envoi ? " goes for " : passesText);
        if (suitName) {
            text += suitName;
        }

        var playerIndex = groups.indexOf(players[playerName]);
        if (envoi) {
            placeSuit(suits[suitName.toLowerCase()], playerIndex);
        }

        groups[playerIndex].bubble.say(text);
    },
    offer : function(playerName, suitName) {
        var firstRound = (typeof suitName !== 'undefined');
        if (firstRound)
        {
            $("#prompt1").css("visibility", "visible");
        }
        else
        {
            $("#prompt2").css("visibility", "visible");
        }
    },
    play: function(playerName, cardNames) {
        var cards = _.map(cardNames, function(cardName) {
            return cardFor(cardName);
        });
        chooseCard(cards);
    },
    playCard: function(playerName, cardName) {
        var card = cardFor(cardName);
        if (!isPlayerMe(playerName))
        {
            card.cardback.visible = false;
        }
        playCard(card);
    },
    gameUpdate: function(team1, team1Score, team2, team2Score) {
        gameScoreArea.updateScores(team1Score, team2Score);
    },
    roundEnds: function(winner, points) {
        console.log(winner+" takes with "+points+" points");
        clearTable(players[winner]);
    },
    gameStarts : function(gameNumber) {
        gameScoreArea.clearScores();
        clearGame();
        console.log("game #"+gameNumber+" starting")
    },
    gameEnds : function(winningTeam, team1, score1, team2, score2) {
        if (winningTeam) // forfeit has no winning team
        {
            console.log(winningTeam + " wins");
            gameScoreArea.updateScores(score1, score2);
        }
        else
        {
            console.log("game ends");
        }
    },
    partieUpdate: function(team1, team1Score, team2, team2Score) {
        partieScoreArea.updateScores(team1Score, team2Score);
    },
    partieEnds: function(winningTeam) {
        bootbox.alert("Partie is over.  Winner is "+winningTeam);  // TODO: for now
    },
    partieStarts: function(team1, team2, playerNames) {
        gameScoreArea.setTeams(team1, team2);
        partieScoreArea.setTeams(team1, team2);
        paintPlayerNames(playerNames);
        clearGame();
    }
};

function clearGame() {
    resetSuits();
    resetDeck();
    for (var i=0; i<groups.length; i++)
    {
        groups[i].nextPosition = null;
    }
}

var client;

var ScoreArea = Group.extend({
    _class: 'ScoreArea',
    _score1: null,
    _score2: null,
    _team1: null,
    _team2: null,

    initialize: function ScoreArea() {
        Group.apply(this, arguments);

        var fontSize = 10;
        var padding = 15, rowHeight = 20;
        var point = new Point(this.topLeft.x + padding, this.topLeft.y + padding + 5);
        this.addChild(new PointText({
            point: point,
            content: this.title,
            fontSize: fontSize
        }));
        this._team1 = new PointText({
            point: point + [0, rowHeight],
            content: "Nous: ",
            fontSize: fontSize
        });
        this.addChild(this._team1);
        this._score1 = new PointText({
            point: point + [this.size.width - 30, rowHeight],
            content: "0",
            justification: "right",
            fontSize: fontSize
        });
        this.addChild(this._score1);
        this._team2 = new PointText({
            point: point + [0, 2 * rowHeight],
            content: "Eux: ",
            fontSize: fontSize
        });
        this.addChild(this._team2);
        this._score2 = new PointText({
            point: point + [this.size.width - 30, 2 * rowHeight],
            content: "0",
            justification: "right",
            fontSize: fontSize
        });
        this.addChild(this._score2);

        var bg = new Path.Rectangle(this.topLeft + [5, 5], this.size - [10, 10] , 5);
        this.addChild(bg);
        bg.style = {
            fillColor: new Color(1, 0.95, 0.64, 0.8),
            strokeColor: 'black',
            strokeWidth: 1,
            shadowColor: new Color(0, 0, 0, 0.3),
            shadowBlur: 12,
            shadowOffset: new Point(10, 10)
        };
        bg.sendToBack();

    },

    updateScores: function(team1Score, team2Score) {
        this._score1.content = "" + team1Score;
        this._score2.content = "" + team2Score;
    },
    clearScores: function() {
        this.updateScores(0, 0);
    },
    setTeams: function(team1, team2) {
        this._team1.setContent(team1+":");
        this._team2.setContent(team2+":");
    }

});

var Bubble = Group.extend({
    _class: 'Bubble',

    text: '',
    setText: function(text) {
        this.text = text;
        this.textField().content = this._splitText();
    },
    orientation: 0,

    textField: function() {
        return this.getItem({className: 'PointText'});
    },

    initialize: function Bubble() {
        Group.apply(this, arguments);

        var point = new Point(10, 30);
        var size = new Size(200, 55);

        var rect = new Rectangle(point, size);
        var body = new Path.Rectangle(rect, 5);

        var chupchik  = new Path.RegularPolygon(point, 3, 10);
        var chupchikOffset = 8;
        chupchik.scale(1, -1.5);
        chupchik.shear(-0.6, 0);

        chupchik.rotate(90*this.orientation, body.bottomLeft);
        if (this.orientation == 0)
        {
            chupchik.translate(0.25*size.width, size.height + chupchikOffset+2);
        }
        else if (this.orientation == 1)
        {
            chupchik.translate(-chupchikOffset-5, 0.5*size.height);
        }
        else if (this.orientation == 2)
        {
            chupchik.translate(0.75 * size.width, -chupchikOffset);
        }
        else if (this.orientation == 3)
        {
            chupchik.translate(size.width+chupchikOffset-3, 0.5*size.height);
        }

        body.remove();
        chupchik.remove();

        var bubble = body.unite(chupchik);
        bubble.style = {
            fillColor: new Color(1, 0.95, 0.64, 0.8),
            strokeColor: 'black',
            strokeWidth: 1,
            shadowColor: new Color(0, 0, 0, 0.3),
            shadowBlur: 12,
            shadowOffset: new Point(10, 10)
        };
        this.pivot = chupchik.position;

        var textField = new PointText({point: this.bounds.topLeft + [20, 50]});
        textField.content = this._splitText();
        textField.fillColor = 'black';
        textField.bringToFront();

        this.addChild(bubble);
        this.addChild(textField);

        this.hide();
    },

    _splitText: function() {
        var lineSizeInChars = 35;
        var lines = [];
        for (var i = 0; i <= (this.text.length / lineSizeInChars); i++) {
            lines[i] = this.text.substr( lineSizeInChars * i, Math.min(this.text.length - (i * lineSizeInChars), lineSizeInChars) );
        }
        return lines.join('\n');
    },

    show: function() {
        for (var i=0; i<this.children.length; i++) {
            this.children[i].visible = true;
        }
        this.visible = true;
    },

    hide: function() {
        this.visible = false;
        for (var i=0; i<this.children.length; i++) {
            this.children[i].visible = false;
        }
    },

    say: function(text) {
        this.setText(text);
        this.show();
        var self = this;
        setTimeout(function() {
            self.hide();
        }, 2000);
    }

});


$(function() {
    var a = Math.min(view.size.width, view.size.height);
    setupTable(a);
    var c = a / (2 + handAspectRatio);
    var b = handAspectRatio * c;

    loadSuits();
    scaleSuits(b);

    groups = setupAreas(c, b);
    setupScoreAreas(c, b);

    loadCards();
    scaleCards(c);
    setupCardbacks(c);
    setupBubbles();

    var card = randomCard();
    cardSeparation = card.bounds.width / 2;
    selectDelta = [0, card.bounds.height / 5];

    htmlInit();
    groupsLayer.activate();
    resetDeck();
    connectToServer();
});

function htmlInit() {
    $("#side-panel").css('left', (a + 10)+"px");
    $(".prompt").width(a);
    $("#envoi-btn").on('click', function() {
        $("#prompt1").css("visibility", "hidden");
        sendResponse("envoi");
    });
    $("#pass-btn").on('click', function() {
        $("#prompt1").css("visibility", "hidden");
        sendResponse("pass");
    });
    $("#pass2-btn").on('click', function() {
        $("#prompt2").css("visibility", "hidden");
        sendResponse("pass2");
    });
    $("button.envoi").on('click', function() {
        $("#prompt2").css("visibility", "hidden");
        sendResponse("envoi", [$(this).data("atout")]);
    });
}

function isPlayerMe(playerName) {
    return playerName == thisPlayerName;
}


function connectToServer() {
    var ws = new SockJS('/belote');
    client = Stomp.over(ws);
    client.debug = null;

    client.connect({}, function(frame) {
        console.log('connected');

        thisPlayerName = frame.headers['user-name'];

        var handleCmd = function(message) {
            var body = JSON.parse(message.body);
            var cmd = cmds[body.cmd];
            if (!cmd) {
                console.error("unimplemented command: "+body.cmd);
            } else {
                cmd.apply(null, body.args);
            }
        };

        client.subscribe("/topic/belote", handleCmd);
        client.subscribe("/user/queue/belote", handleCmd);

        $("#disconnect-btn").on('click', function() {
            client.disconnect(function() {
                console.log("disconnected");
            });
        });

        $("#newPartie-btn").on('click', function() {
            client.send('/app/newPartie');
        });

    }, function(error) {
        console.log('error: '+error.headers.message);
    });
}

function sendResponse(cmd, args) {
    var msg = {
      name: cmd,
      args: args || []
    };
    client.send('/app/respond', {}, JSON.stringify(msg));
}

function cardFor(serverSideCardName) {
    var card_name = serverSideCardName.replace(/ /g, '_');
    return cards[card_name];
}



function chooseCard(validCards) {
    _.each(validCards, function(card) {
        card.candidate = true;
        card.position -= selectDelta;
        armCard(card);
    });
}

function armCard(card) {
    card.on('click', function() {
        deselect(candidateCards(card.parent), true);
        playCard(card);
        sendResponse("playerChooses", [ card.name ]);
    });
}

function candidateCards(group) {
    return group.getItems({className: 'Raster', candidate: true});
}

function deselect(cards) {
    _.each(cards, function(card) {
        card.off('click');
        card.candidate = false;
        card.position += selectDelta;
    });
}

function playCard(card) {
    var group = card.parent;
    var verticalOffset = ( groups[0].hand.bounds.height + card.bounds.height ) / 2 + (card.bounds.height / 4);
    var offset = new Size(0, verticalOffset);
    var vector = vectorize(offset);
    var position = group.hand.position - vector.rotate(90*groups.indexOf(group));
    moveCardToPosition(card, position);
    played.push(card);
}

function placeCards(cards, group, backface) {
    _.each(cards, function(card) {
        placeCardInGroup(card, group, backface);
    });
}

function turnUpCard(card) {
    placeCardToPosition(card, table.bounds.center + new Size(card.bounds.width/2 + 10, 0));
}

function rotate(group, point)
{
    var index = groups.indexOf(group);
    return point.rotate(90*index);
}

function vectorize(size)
{
    return (table.bounds.topLeft + size) - table.bounds.topLeft;
}

function nextPosition(group, card) {
    var angle = 90 * groups.indexOf(group);
    if (group.nextPosition)
    {
        var offset = vectorize(new Size(cardSeparation, 0));
        group.nextPosition += offset.rotate(angle);
    }
    else
    {
        var verticalOffset = (groups[0].hand.bounds.height - card.bounds.height) / 2;
        var horizontalOffset = (groups[0].hand.bounds.width - card.bounds.width ) / 2;
        var offset = vectorize(new Size(horizontalOffset, verticalOffset));

        group.nextPosition = group.hand.position - offset.rotate(angle);
    }
    return group.nextPosition;
}

function placeCardInGroup(card, group, backface) {
    var cardToPlace = backface ? card.cardback : card;
    var otherSide = backface ? card : card.cardback;
    var position = nextPosition(group, cardToPlace);

    cardToPlace.rotate(90*groups.indexOf(group));
    otherSide.rotate(90*groups.indexOf(group));

    moveCardToPosition(card, position, backface, function(card) {
        group.addChild(cardToPlace);
        group.addChild(otherSide);
    });
}

function placeCardToPosition(card, position, backface)
{
    var cardToPlace = backface ? card.cardback : card;
    var otherSide = backface ? card : card.cardback;

    cardToPlace.visible = true;
    otherSide.visible = false;
    cardToPlace.bringToFront();
    cardToPlace.position = position;
    otherSide.position = position;
}

function moveCardToPosition(card, position, backface, doneFn)
{
    var cardToPlace = backface ? card.cardback : card;
    var otherSide = backface ? card : card.cardback;

    cardToPlace.visible = true;
    otherSide.visible = false;
    cardToPlace.bringToFront();
    animateToPosition(cardToPlace, otherSide, position, doneFn);
}

function animateToPosition(card, otherSide, destination, doneFn)
{
    var duration = 0.5; // seconds
    var vector = destination - card.position;

    card.onFrame = function(event) {
        var distance = event.delta/duration * vector.length;
        var trans = new Point({length: distance, angle: vector.angle});
        card.translate(trans);
        var distToDestination = (card.position - destination).length;
        if (distToDestination < 5) {
            card.position = destination;
            otherSide.position = destination;
            card.onFrame = null;
            if (doneFn)
            {
                doneFn.call(undefined, card);
            }
        }
    };

}

function onFrame(event) {
}


function clearTable(winningGroup) {
    _.each(played, function(card) {

        var verticalOffset = ( groups[0].hand.bounds.height + card.bounds.height ) / 2 + (card.bounds.height / 4);
        var offset = new Size(0, verticalOffset);
        var vector = vectorize(offset);
        var position = winningGroup.hand.position - vector.rotate(90*groups.indexOf(winningGroup));

        moveCardToPosition(card, position, false, function(card) {
            var group = card.parent;
            var index = groups.indexOf(group);
            if (index >= 0)
            {
                card.rotate(-90*index);  // reset rotation
                card.cardback.rotate(-90*index);
            }
            card.remove();
            card.cardback.remove();
            cardsLayer.addChild(card);
            cardsLayer.addChild(card.cardback);

            card.visible = false;
            card.cardback.visible = false;
        });
    });

    played = [];
}

function resetDeck() {
    var delta = new Size(0, 0);
    for (var cardName in cards) {
        var card = cards[cardName];
        card.visible = false;
        var spot = table.bounds.center - new Size(card.bounds.width/2 + 10, 0) - delta;
        placeCardToPosition(card, spot, true);
        delta += new Size(0.25, 0.25);
    }
}


// initial rendering setup..

function loadCards() {
    cardsLayer = new Layer({name: 'cards'});

    $("#card_images").find("img").each(function() {
        var img = $(this);
        var id = img.attr("id");
        var card = new Raster(id);
        card.name = id;
        card.childType = 'card';
        cards[id] = card;
    });
}

function scaleCards(c) {
    for (var card in cards) {
        var scale = (0.8 * c) / cards[card].height;
        cards[card].scale(scale);
    }
}

function setupCardbacks(c) {
    var cardback = new Raster('cardback');
    var scale = (0.8 * c) / cardback.height;
    cardback.scale(scale);
    cardback.remove();
    var symbol = new Symbol(cardback);

    for (var card in cards) {
        var placedCardback = symbol.place();
        placedCardback.visible = false;
        placedCardback.childType = 'card';
        cards[card].cardback = placedCardback;
    }
}

function loadSuits() {
    new Layer({name: 'suits'});

    $("#suit_images").find("img").each(function() {
        var img = $(this);
        var id = img.attr("id");
        var suit = new Raster(id);
        suit.name = id;
        suit.opacity = 0.5;
        suit.visible = false;
        suits[id] = suit;
    });
}

function scaleSuits(b) {
    var desiredHeight = b / 2;
    var scale = desiredHeight / suits['trefle'].bounds.height ;
    for (var suit in suits) {
        suits[suit].scale(scale);
    }
}

function placeSuit(suit, index)
{
    var point = table.bounds.center + [0, table.bounds.width / 5];
    var position = point.rotate(index * 90, table.bounds.center);
    suit.position = position;
    suit.visible = true;
    suit.bringToFront();
}

function resetSuits() {
    for (var suitName in suits) {
        suits[suitName].visible = false;
    }
}


function randomCard() {
    var index = parseInt(Math.random()*32);
    var key = Object.keys(cards)[index];
    return cards[key];
}

function setupTable(a) {
    new Layer({name: 'table'});

    table = new Raster("tablecloth");
    table.position = [a/2, a/2];
    table.scale(a / table.bounds.height);
}

function setupAreas(c, b) {
    groupsLayer = new Layer({name: 'groups'});

    var groups = [];

    var path = new Path.Rectangle(
        new Rectangle(
            new Point(c, c + b),
            new Size(b, c))
    );

    for (var i=0; i<4; i++)
    {
        var group = new Group(path);
        group.hand = path;

        groups.push(group);

        var playerNameField = new PointText({
            point: path.bounds.center + new Point(0, path.bounds.height/2 - 3),
            content: 'p'+i,
            justification: 'center',
            fillColor: new Color(1, 1, 1),
            fontWeight: 'bold',
            fontSize: 12
        });
        group.addChild(playerNameField);
        group.playerName = playerNameField;
        playerNameField.bringToFront();

        path = path.clone();
    }

    for (var i=0; i<4; i++)
    {
        groups[i].rotate(90*i, table.bounds.center);
    }

    return groups;
}

function paintPlayerNames(playerNames)
{
    for (var i=0; i<groups.length; i++)
    {
        var playerName = playerNames[i];
        groups[i].playerName.content = playerName;
        players[playerName] = groups[i];
    }
}

function setupBubbles() {
    new Layer({name: 'bubbles'});

    for (var i=0; i<4; i++) {
        var bubble = new Bubble({ orientation: i, text: '...' });
        bubble.position = groups[i].position;
        groups[i].bubble = bubble;
    }
}

function setupScoreAreas(c, b)
{
    gameScoreArea = new ScoreArea({
        title: "Game Score",
        topLeft: new Point(b+c, 0),
        size: new Size(c, c)
    });

    partieScoreArea = new ScoreArea({
        title: "Partie Score",
        topLeft: new Point(0, 0),
        size: new Size(c, c)
    });
}
