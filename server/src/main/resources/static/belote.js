var table;
var cards = {};
var suits = {};
var players = {};
var hands = [];
var gameScoreArea, partieScoreArea;

var cardsLayer, handsLayer;

var cardSeparation, selectDelta, cardBounds;
var handAspectRatio = 2.2;

var played = [];

var client;
var thisPlayerName;


var Hand = Base.extend({
    initialize: function(orientation, path) {
        this.orientation = orientation;
        this.path = path.clone();

        this.group = new Group(this.path);
        this.group.hand = this;

        this.playerNameField = new PointText({
            point: this.path.bounds.center + new Point(0, this.path.bounds.height/2 - 3),
            content: '',
            justification: 'center',
            fillColor: new Color(1, 1, 1),
            fontWeight: 'bold',
            fontSize: 12
        });
        this.group.addChild(this.playerNameField);
        this.group.rotate(this.angle(), table.bounds.center);
        this.cards = [];
    },
    rotate: function(point) {
        return point.rotate(this.angle());
    },
    angle: function() {
        return 90 * this.orientation;
    },
    receiveCard: function(card, playerName, order) {

        this.cards.push(card);

        var newCards = [];
        for (var i=0; i<order.length; i++) {
            newCards.push(this.cards[order[i]]);
        }
        this.cards = newCards;

        var self = this;

        _.each(this.cards, function(card, index) {
            var position = self.cardPosition(index);

            card.moveTo(position, self.angle(), function(card) {
                if (isPlayerMe(playerName)) {
                    card.flip(true);
                }
                self.addCard(card);
                if (self.isLastCard(card)) {
                    self.zOrderCards();
                }
            });

        });
    },
    isLastCard: function(card) {
        return (this.cards[this.cards.length-1].name == card.name);
    },
    zOrderCards: function() {
        for (var i=1; i<this.cards.length; i++) {
            var prevCard = this.cards[i-1];
            var thisCard = this.cards[i];
            thisCard.face.moveAbove(prevCard.face);
            thisCard.back.moveAbove(prevCard.back);
        }
    },
    cardPosition: function(index) {
        var verticalOffset = (hands[0].path.bounds.height - cardBounds.height) / 2;
        var horizontalOffset = ( hands[0].path.bounds.width - cardBounds.width ) / 2;
        var startingPosition = this.addVectorToPosition(this.path.position, new Size(-horizontalOffset, -verticalOffset));

        return this.addVectorToPosition(startingPosition, new Size(cardSeparation * index, 0));
    },
    cardPlayPosition: function() {
        var verticalOffset = ( hands[0].path.bounds.height + cardBounds.height ) / 2 + (cardBounds.height / 8);
        return this.addVectorToPosition(this.path.position, new Size(0, -verticalOffset));
    },
    vectorize: function(size) {
        return (table.bounds.topLeft + size) - table.bounds.topLeft;
    },
    addVectorToPosition: function(position, offset) {
        var vector = this.vectorize(offset);
        var transformedVector = this.rotate(vector);
        return position + transformedVector;
    },
    clear: function() {
        this.cards = [];
    },
    setPlayerName: function(name) {
        this.playerNameField.content = name;
    },
    candidateCards: function() {
        return this.group.getItems({className: 'Raster', candidate: true});
    },
    deselect: function() {
        var self = this;
        _.each(this.candidateCards(), function(cardFace) {
            cardFace.off('click');
            cardFace.candidate = false;
            cardFace.position = self.addVectorToPosition(cardFace.position, new Size(0, selectDelta));
        });
    },
    addCard: function(card) {
        if (card.face.parent != this.group) {
            this.group.addChild(card.face);
        }
        if (card.back.parent != this.group) {
            this.group.addChild(card.back);
        }
    },
    chooseCard: function(validCards) {
        var self = this;
        _.each(validCards, function(card) {
            card.face.candidate = true;
            card.face.position = self.addVectorToPosition(card.face.position, new Size(0, -selectDelta));
            card.arm();
        });
    },
    placeSuit: function(suit) {
        var point = table.bounds.center + [0, table.bounds.width / 5];
        suit.position = point.rotate(this.angle(), table.bounds.center);
        suit.visible = true;
        suit.bringToFront();
    },
    setupBubble: function() {
        var bubble = new Bubble({ orientation: this.orientation });
        bubble.position = this.path.position;
        this.bubble = bubble;
    }
});

var Card = Base.extend({
    initialize: function(name, cardback, desiredHeight) {
        this.name = name;
        var raster = new Raster(name);
        this.face = raster.scale(desiredHeight / raster.height);
        this.back = cardback.place();
    },
    flip: function(faceUp) {
        var faceToShow = faceUp ? this.face : this.back;
        var faceToHide = faceUp ? this.back : this.face;
        faceToShow.visible = true;
        faceToHide.visible = false;
    },
    placeAt: function(position) {
        this.face.position = position;
        this.back.position = position;
    },
    rotate: function(angle) {
        this.face.rotate(angle);
        this.back.rotate(angle);
    },
    moveTo: function (destination, finalRotation, doneFn) {
        var duration = 0.5; // seconds
        var timeRemaining = duration;

        var rotationSpecified = (typeof finalRotation !== 'undefined') || finalRotation != null;
        if (rotationSpecified) {
            finalRotation %= 180;
        }

        var face = (this.face.visible ? this.face : this.back);
        var self = this;

        face.onFrame = function(event) {
            var vector = destination - face.position;

            var progress = event.delta / timeRemaining;
            var distance = progress * vector.length;
            var trans = new Point({length: distance, angle: vector.angle});

            if (rotationSpecified) {
                var delta_angle = finalRotation - face.rotation;
                var angle = progress * delta_angle;
            }

            timeRemaining -= event.delta;

            if (timeRemaining < 0) {
                self.placeAt(destination);

                if (rotationSpecified) {
                    self.face.rotation = finalRotation;
                    self.back.rotation = finalRotation;
                }

                face.onFrame = null;
                if (doneFn)
                {
                    doneFn.call(undefined, self);
                }
            }
            else {
                face.translate(trans);
                if (rotationSpecified) {
                    face.rotate(angle);
                }
            }
        };
    },
    clear: function() {
        this.face.rotation = 0;
        this.face.remove();
        cardsLayer.addChild(this.face);
        this.face.visible = false;

        this.back.rotation = 0;
        this.back.remove();
        cardsLayer.addChild(this.back);
        this.back.visible = false;
    },
    play: function() {
        var hand = this.face.parent.hand;
        this.flip(true);
        this.moveTo(hand.cardPlayPosition());
        played.push(this);
    },
    arm: function() {
        var self = this;
        this.face.on('click', function() {
            var hand = self.face.parent.hand;
            hand.deselect();
            self.play();
            sendResponse("playerChooses", [ self.name ]);
        });
    },
    turnUp: function() {
        this.flip(true);
        this.placeAt(table.bounds.center + new Size(this.face.bounds.width/2 + 10, 0));
    }
});


var cmds = {
    receiveCard : function(playerName, cardName, order) {
        var hand = players[playerName];
        hand.receiveCard(cards[cardName], playerName, order);
    },
    turnUpCard : function(cardName) {
        cards[cardName].turnUp();
    },
    playerDecision : function(playerName, envoi, suitName) {
        var passesText = (suitName ? " pass at " : " pass again.");
        var text = "I" + (envoi ? " go for " : passesText);
        if (suitName) {
            text += suitName;
        }

        var hand = players[playerName];
        if (envoi) {
            hand.placeSuit(suits[suitName.toLowerCase()]);
        }

        hand.bubble.say(text);
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
        var validCards = _.map(cardNames, function(cardName) {
            return cards[cardName];
        });
        players[playerName].chooseCard(validCards);
    },
    playCard: function(playerName, cardName) {
        cards[cardName].play();
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
        setupPlayers(playerNames);
        clearGame();
    }
};

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
    htmlInit(a);
    handsLayer.activate();
    resetDeck();
    connectToServer();
});

function htmlInit(a) {
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

        client.subscribe("/topic/enterPartie", function(msg) {
            var partie = JSON.parse(msg.body);
            $("#t1p1").val(partie.team1.first);
            $("#t1p2").val(partie.team1.second);
            $("#t2p1").val(partie.team2.first);
            $("#t2p2").val(partie.team2.second);
        });
        $("#t1p1").on('click', function() {
            client.send("/app/joinPartie", {}, JSON.stringify({team: 'team1', position: 'first'}));
        });
        $("#t1p2").on('click', function() {
            client.send("/app/joinPartie", {}, JSON.stringify({team: 'team1', position: 'second'}));
        });
        $("#t2p1").on('click', function() {
            client.send("/app/joinPartie", {}, JSON.stringify({team: 'team2', position: 'first'}));
        });
        $("#t2p2").on('click', function() {
            client.send("/app/joinPartie", {}, JSON.stringify({team: 'team2', position: 'second'}));
        });
        client.send("/app/enterPartie");

        $("#startPartie-btn").on('click', function() {
           client.send("/app/startPartie");
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

function onFrame(event) {
}


function clearTable(winningHand) {
    _.each(played, function(card) {

        var position = winningHand.cardPlayPosition();

        card.flip(true);
        card.moveTo(position, winningHand.angle(), function(card) {
            card.clear();
        });
    });

    played = [];
}

function clearGame() {
    resetSuits();
    _.each(hands, function(hand) {
        hand.clear();
    });
    _.each(cards, function(card) {
        card.clear();
    });
    resetDeck();
}


function resetDeck() {
    var delta = new Size(0, 0);
    _.each(cards, function(card) {
        var spot = table.bounds.center - new Size(card.face.bounds.width/2 + 10, 0) - delta;
        card.flip(false);
        card.placeAt(spot);
        delta += new Size(0.25, 0.25);
    });
}


// initial rendering setup..

function loadCards(desiredHeight) {
    cardsLayer = new Layer({name: 'cards'});

    var cardback = new Raster('cardback');
    cardback.scale(desiredHeight / cardback.height);
    cardback.remove();
    var symbol = new Symbol(cardback);

    $("#card_images").find("img").each(function() {
        var id = $(this).attr("id");
        cards[id] = new Card(id, symbol, desiredHeight);
    });
}

function loadSuits(desiredHeight) {
    new Layer({name: 'suits'});

    $("#suit_images").find("img").each(function() {
        var img = $(this);
        var id = img.attr("id");
        var suit = new Raster(id);
        suit.scale(desiredHeight / suit.bounds.height);
        suit.name = id;
        suit.opacity = 0.5;
        suit.visible = false;
        suits[id] = suit;
    });
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
    table.position = new Point(a/2, a/2);
    table.scale(a / table.bounds.height);

    var c = a / (2 + handAspectRatio);
    var b = handAspectRatio * c;

    loadSuits(b/2);

    setupHands(c, b);
    setupScoreAreas(c, b);

    loadCards(0.8*c);

    setupBubbles();

    var card = randomCard();
    cardBounds = card.face.bounds;
    cardSeparation = cardBounds.width / 2.5;
    selectDelta = cardBounds.height / 5;
}

function setupHands(c, b) {
    handsLayer = new Layer({name: 'hands'});

    var path = new Path.Rectangle(
        new Rectangle(
            new Point(c, c + b),
            new Size(b, c))
    );

    for (var i=0; i<4; i++)
    {
        hands.push(new Hand(i, path));
    }

    path.remove();
    return hands;
}

function setupBubbles() {
    new Layer({name: 'bubbles'});

    for (var i=0; i<4; i++) {
        hands[i].setupBubble();
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


function setupPlayers(playerNames)
{
    for (var i=0; i<hands.length; i++)
    {
        var playerName = playerNames[i];
        hands[i].setPlayerName(playerName);
        players[playerName] = hands[i];
    }
}

