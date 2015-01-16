var table;
var deckCards = {};
var suits = {};
var players = {};
var hands = [];
var played = [];
var gameScoreArea, partieScoreArea;
var cardsLayer, handsLayer;
var cardSeparation, selectDelta, cardBounds;
var handAspectRatio = 2.2;
var client;
var thisPlayerName;


var CardAnimation = Base.extend({
    initialize: function(card) {
        this.card = card;
        this.active = false;
        this.flipActive = false;
        this.duration = 0.7;  // seconds
    },
    animate: function(destination, rotation, flip, doneFn) {
        this.destination = destination;

        this.rotation = rotation;
        if (this.rotationSpecified()) {
            this.rotation %= 180;
        }

        // TODO: replace with xor
        if (this.flip == true) {
            if (flip == true) {
                this.flip = false;
            } else {
                this.flip = true;
            }
        } else {
            this.flip = flip;
        }

        this.doneFn = doneFn;
        this.face = this.card.upFace();

        this.vector = this.destination - this.face.position;
        this.initialPosition = this.face.position;
        this.timeElapsed = 0;
        this.k = this.vector.length / (this.duration * this.duration);

        this.active = true;
    },
    rotationSpecified: function() {
        return (typeof this.rotation !== 'undefined') && this.rotation != null;
    },
    onFrame: function(event) {
        this.timeElapsed += event.delta;
        var progress = this.timeElapsed / this.duration;

        if (this.flipActive)
        {
            this.card.animateFlipFrame(progress);
            this.flipActive = progress < 1.0;
            if (!this.flipActive) {
                this.card.doneFlipping();
            }
        }

        if (!this.active) {
            return;
        }

        var distance = this.vector.length - this.k * Math.pow(this.duration - this.timeElapsed, 2);
        var dist_vector = new Point({length: distance, angle: this.vector.angle});

        if (this.rotationSpecified()) {
            var delta_angle = this.rotation - this.face.rotation;
            var angle = progress * delta_angle;
        }

        if (this.timeElapsed < this.duration) {
            this.face.position = this.initialPosition + dist_vector;
            if (this.rotationSpecified()) {
                this.face.rotate(angle);
            }
        }
        else {
            this.active = false;

            this.card.placeAt(this.destination);
            if (this.rotationSpecified()) {
                this.card.face.rotation = this.rotation;
                this.card.back.rotation = this.rotation;
            }
            if (this.flip) {
                this.card.flip();
                this.flip = false;
            }
            if (typeof this.doneFn !== 'undefined') {
                this.doneFn.call(undefined, this.card);
            }
        }
    }
});

var Card = Base.extend({
    initialize: function(name, cardback, desiredHeight) {
        this.name = name;

        var raster = new Raster(name);
        raster.scale(desiredHeight / raster.height);
        this.faceSym = new Symbol(raster);
        this.face = this.faceSym.place();

        this.backSym = cardback;
        this.back = this.backSym.place();

        this.animation = new CardAnimation(this);
        var self = this;
        this.face.onFrame = function(event) {
            self.animation.onFrame(event);
        };

        this.faceUp = false;
        this.show();
    },
    show: function() {
        this.upFace().visible = true;
        this.downFace().visible = false;
    },
    showFace: function(up) {
        this.faceUp = up;
        this.show();
    },
    flip: function() {
        this.faceUp = !this.faceUp;
        this.show();
    },
    upFace: function() {
        return this.faceUp ? this.face : this.back;
    },
    downFace: function() {
        return this.faceUp ? this.back : this.face;
    },
    upSym: function() {
        return this.faceUp ? this.faceSym : this.backSym;
    },
    downSym: function() {
        return this.faceUp ? this.backSym : this.faceSym;
    },
    hide: function() {
        this.face.visible = false;
        this.back.visible = false;
    },
    position: function() {
        return this.face.position;
    },
    placeAt: function(position) {
        this.face.position = position;
        this.back.position = position;
    },
    rotate: function(angle) {
        this.face.rotate(angle);
        this.back.rotate(angle);
    },
    moveTo: function (destination, rotation, flip, doneFn) {
        this.animation.animate(destination, rotation, flip, doneFn);
    },
    clear: function() {
        this.face.rotation = 0;
        this.face.remove();
        cardsLayer.addChild(this.face);

        this.back.rotation = 0;
        this.back.remove();
        cardsLayer.addChild(this.back);
    },
    play: function() {
        var hand = this.face.parent.hand;
        this.moveTo(hand.cardPlayPosition(), null, !isPlayerMe(hand.playerNameField.content));
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
    animateFlipFrame: function(progress) {
        var phase = progress * Math.PI;
        var scale = Math.abs(Math.cos(phase));
        var symbol = progress < 0.5 ? this.upSym() : this.downSym();
        if (this.placedItem) {
            this.placedItem.remove();
        }
        this.placedItem = symbol.place(this.position());
        this.placedItem.scale(scale, 1);
    },
    doneFlipping: function() {
        this.placedItem.remove();
        this.flip();
    },
    startFlipping: function() {
        this.hide();
        this.animation.timeElapsed = 0;
        this.animation.flipActive = true;
    }
});

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
    sortCards: function(order) {
        var newCards = [];
        for (var i=0; i<order.length; i++) {
            newCards.push(this.cards[order[i]]);
        }
        return newCards;
    },
    receiveCard: function(receivedCard, playerName, order) {
        this.cards.push(receivedCard);
        this.cards = this.sortCards(order);

        var self = this;
        _.each(this.cards, function(card, index) {
            self.addCard(card);
            var flipIt = (card.name == receivedCard.name) && isPlayerMe(playerName) && (card.faceUp == false);
            card.moveTo(self.cardPosition(index), self.angle(), flipIt, function(card) {
                if (self.isLastCard(card)) {
                    self.zOrderCards();
                }
            });
        });
    },
    isLastCard: function(card) {
        return _.last(this.cards).name == card.name;
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
        var verticalOffset = ( hands[0].path.bounds.height + ( 1.25 * cardBounds.height ) ) / 2;
        return this.addVectorToPosition(this.path.position, new Size(0, -verticalOffset));
    },
    pilePosition: function() {
        var offset = ( hands[0].path.bounds.width + ( 1.5 * cardBounds.width ) ) / 2;
        return this.addVectorToPosition(this.path.position, new Size(-offset, 0));
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
        return this.group.getItems({className: 'PlacedSymbol', candidate: true});
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
            card.placeAt(self.addVectorToPosition(card.position(), new Size(0, -selectDelta)));
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

var cmds = {
    receiveCard : function(playerName, cardName, order) {
        var hand = players[playerName];
        hand.receiveCard(deckCards[cardName], playerName, order);
    },
    turnUpCard : function(cardName) {
        deckCards[cardName].placeAt(turnedUpCardPosition());
        deckCards[cardName].showFace(true);
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
            return deckCards[cardName];
        });
        players[playerName].chooseCard(validCards);
    },
    playCard: function(playerName, cardName, beloteRebeloteText) {
        players[playerName].bubble.say(beloteRebeloteText);
        deckCards[cardName].play();
    },
    gameUpdate: function(team1, team1Score, team2, team2Score) {
        gameScoreArea.updateScores(team1Score, team2Score);
    },
    roundEnds: function(winner, points) {
        console.log(winner+" takes with "+points+" points");
        clearRound(players[winner]);
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
        if (! _.isEmpty(text)) {
            this.show();
            var self = this;
            setTimeout(function () {
                self.hide();
            }, 2000);
        }
    }

});


$(function() {
    var a = Math.min(view.size.width, view.size.height);
    setupTable(a);
    htmlInit(a);
    handsLayer.activate();
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
    //client.debug = null;

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


function clearRound(winningHand) {
    var position = winningHand.cardPlayPosition();
    var pilePosition = winningHand.pilePosition();
    var angle = winningHand.angle();
    _.each(played, function(playedCard) {
        playedCard.moveTo(position, angle, false, function(card) {
            card.clear();
            card.moveTo(pilePosition, angle, true);
        });
    });

    played = [];
}

function clearGame() {
    resetSuits();
    _.each(hands, function(hand) {
        hand.clear();
    });
    resetDeck();
}

function deckPosition() {
    return table.bounds.center - new Size(cardBounds.width/2 + 10, 0);
}

function turnedUpCardPosition() {
    return table.bounds.center + new Size(cardBounds.width/2 + 10, 0);
}

function resetDeck(place) {
    var delta = new Size(0.25, 0.25);
    var spot = deckPosition();
    _.each(deckCards, function(card) {
        card.showFace(false);
        if (place) {
            card.placeAt(spot);
            card.clear();
        } else {
            card.moveTo(spot, 0, false, function(card) {
                card.clear();
            });
        }
        spot -= delta;
    });
}


// initial rendering setup..

function loadCards(desiredHeight) {
    cardsLayer = new Layer({name: 'cards'});

    var cardback = new Raster('cardback');
    cardback.scale(desiredHeight / cardback.height);
    var symbol = new Symbol(cardback);

    $("#card_images").find("img").each(function() {
        var name = $(this).attr("id");
        deckCards[name] = new Card(name, symbol, desiredHeight);
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
    var key = Object.keys(deckCards)[index];
    return deckCards[key];
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
    loadCards(0.8*c);

    setupScoreAreas(c, b);
    setupBubbles();

    var card = randomCard();
    cardBounds = card.face.bounds;
    cardSeparation = cardBounds.width / 2.5;
    selectDelta = cardBounds.height / 5;

    resetDeck(true);
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
    new Layer({name: 'scoreAreas'});

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

