var cards = {};
var cardsLayer;
var cardSeparation, selectDelta;
var handAspectRatio = 3;
var played = [];

var table, groups, bubbles;
var gameScoreArea, partieScoreArea;

var cmds = {
    receiveCard : function(playerName, cardName) {
        placeCards([cardFor(cardName)], groups, players[playerName]);
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

        bubbles[players[playerName]].say(text);
    },
    offer : function(playerName, suitName) {
        var firstRound = (typeof suitName !== 'undefined');
        if (firstRound)
        {
            var prompt = "Would you like to envoi a "+suitName+"?";
            var envoi = window.confirm(prompt);  // TODO: remove scaffold
            if (envoi)
            {
                sendResponse("envoi");
            }
            else
            {
                sendResponse("pass");
            }
        }
        else
        {
            var promptCaption = "Second round, envoi? (pass/pique/coeur/carreau/trefle)";
            var response = window.prompt(promptCaption, "");
            if (response === "pass" || (response == null))
            {
                sendResponse("pass2");
            } else
            {
                var suitName = response.toLowerCase();
                sendResponse("envoi", [suitName]);
            }
        }
    },
    play: function(playerName, cardNames) {
        var cards = _.map(cardNames, function(cardName) {
            return cardFor(cardName);
        });
        chooseCard(cards);
    },
    playCard: function(playerName, cardName) {
        playCard(cardFor(cardName));
    },
    gameUpdate: function(team1, team1Score, team2, team2Score) {
        gameScoreArea.updateScores(team1Score, team2Score);
    },
    roundEnds: function(winner, points) {
        //console.log(winner+" takes with "+points+" points");
        clearTable();
    },
    gameEnds : function(winningTeam) {
        if (winningTeam) // forfeit has no winning team
        {
            window.alert(winningTeam + " wins");
        }
        gameScoreArea.clearScores();
        resetDeck();
        removeCards();
    },
    partieUpdate: function(team1, team1Score, team2, team2Score) {
        partieScoreArea.updateScores(team1Score, team2Score);
    },
    partieEnds: function(winningTeam) {
        window.alert("Partie is over.  Winner is "+winningTeam);  // TODO: for now
    }
};
// for now hard-code:
var players = { Eitan: 0, Johnny: 1, Rony: 2, Corinne: 3 };

var client;


var ScoreArea = Group.extend({
    _class: 'ScoreArea',
    _score1: 0,
    _score2: 0,
    title: '',
    topLeft: new Point(0, 0),

    initialize: function ScoreArea() {
        Group.apply(this, arguments);

        var padding = 15, rowHeight = 20;
        var point = new Point(this.topLeft.x + padding, this.topLeft.y + padding);
        this.addChild(new PointText({
            point: point,
            content: this.title
        }));
        this.addChild(new PointText({
            point: point + [0, 1 * rowHeight],
            content: "Nous: "
        }));
        this._score1 = new PointText({
            point: point + [60, 1 * rowHeight],
            content: "0",
            justification: "right"
        });
        this.addChild(this._score1);
        this.addChild(new PointText({
            point: point + [0, 2 * rowHeight],
            content: "Eux: "
        }));
        this._score2 = new PointText({
            point: point + [60, 2 * rowHeight],
            content: "0",
            justification: "right"
        });
        this.addChild(this._score2);
    },

    updateScores: function(team1Score, team2Score) {
        this._score1.content = "" + team1Score;
        this._score2.content = "" + team2Score;
    },
    clearScores: function() {
        this.updateScores(0, 0);
    }

});

var Bubble = CompoundPath.extend({
    _class: 'Bubble',
    _text: 'Change me',
    _orientation: 0,

    _serializeFields: {
        text: null,
        orientation: null
    },
    getText: function() {
        return this._text;
    },
    setText: function(text) {
        this._text = '' + text;
        this._pointText.content = this._splitText();
    },
    getOrientation: function() {
        return this._orientation;
    },
    setOrientation: function(value) {
        this._orientation = value;
    },

    _pointText: new PointText({visible: false}),
    _body: null,
    _bubble: null,

    initialize: function Bubble() {
        CompoundPath.apply(this, arguments);

        var point = new Point(10, 30);
        var size = new Size(200, 55);

        var rect = new Rectangle(point, size);
        this._body = new Path.Rectangle(rect, 5);
        this._body.visible = false;

        var chupchik  = new Path.RegularPolygon(point, 3, 10);
        var chupchikOffset = 8;
        chupchik.scale(1, -1.5);
        chupchik.shear(-0.6, 0);

        chupchik.rotate(90*this._orientation, this._body.bottomLeft);
        if (this._orientation == 0)
        {
            chupchik.translate(0.25*size.width, size.height + chupchikOffset+2);
        }
        else if (this._orientation == 1)
        {
            chupchik.translate(-chupchikOffset-5, 0.5*size.height);
        }
        else if (this._orientation == 2)
        {
            chupchik.translate(0.75 * size.width, -chupchikOffset);
        }
        else if (this._orientation == 3)
        {
            chupchik.translate(size.width+chupchikOffset-3, 0.5*size.height);
        }

        chupchik.visible = false;

        this._bubble = this._body.unite(chupchik);
        this.style = {
            fillColor: new Color(1, 0.95, 0.64, 0.8),
            strokeColor: 'black',
            strokeWidth: 1,
            shadowColor: new Color(0, 0, 0, 0.3),
            shadowBlur: 12,
            shadowOffset: new Point(10, 10)
        };
        this.pivot = chupchik.position;

        this.addChild(this._bubble);

        this._pointText = new PointText(this.bounds.topLeft + [10, 40]);
        this._pointText.content = this._splitText();
        this._pointText.fillColor = 'black';
        this._pointText.bringToFront();

        this.eitanShow(false);
    },

    eitanShow: function(visible) {
        this.visible = visible;
        this._pointText.visible = visible;
    },

    _splitText: function() {
        var lineSizeInChars = 35;
        var lines = [];
        for (var i = 0; i <= (this._text.length / lineSizeInChars); i++) {
            lines[i] = this._text.substr( lineSizeInChars * i, Math.min(this._text.length - (i * lineSizeInChars), lineSizeInChars) );
        }
        return lines.join('\n');
    },

    _changed: function() {
        if (this._pointText) {
            var topLeft = this.bounds.topLeft + [10, 15] + [this._pointText.bounds.width/2, 0];
            if (this._orientation == 2) {
                // scootch down on account of chupchik
                topLeft += [0, 20];
            } else if (this._orientation == 1) {
                // scootch right on account of chupchik
                topLeft += [20, 0];
            }
            this._pointText.position = topLeft;
        }
    },

    say: function(text) {
        this.setText(text);
        this.bringToFront();
        this._pointText.bringToFront();
        this.eitanShow(true);
        var self = this;
        setTimeout(function() {
            self.eitanShow(false);
        }, 2000);
    }

});




$(function() {
    loadCards();

    var a = Math.min(view.bounds.width, view.bounds.height);
    setupTable(a);
    var c = a / (2 + handAspectRatio);
    var b = handAspectRatio * c;
    groups = setupAreas(c, b);
    bubbles = setupBubbles();

    setupScoreAreas(c, b);

    var card = randomCard();
    var scale = c / card.height;
    scaleCards(scale);
    cardSeparation = [card.bounds.width / 2, 0];
    selectDelta = [0, card.bounds.height / 5];

    $("#button-area").css('left', (a + 10)+"px");

    connectToServer();
});


function connectToServer() {
    var ws = new SockJS('/newPartie');
    client = Stomp.over(ws);
    client.debug = null;

    client.connect({}, function() {
        console.log('connected');

        client.subscribe("/topic/belote", function(message) {
            var body = JSON.parse(message.body);
            var cmd = cmds[body.cmd];
            if (!cmd) {
                console.error("unimplemented command: "+body.cmd);
            } else {
                cmd.apply(null, body.args);
            }
        });

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



// TODO:  technically when add a text field to the canvas it's in some group and
//  a variable is not necessary to obtain a reference to it:  learn how to properly
//  use paper js and improve this implementation



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
    doInGroupCoordinates(group, function(group) {
        var position = group.hand.position + [0, -(card.bounds.height+5)];
        placeCard(card, position);
    });
    played.push(card);
}

function placeCards(hand, groups, index) {
    var group = groups[index];
    doInGroupCoordinates(group, function() {
        _.each(hand, function(card) {
            placeCard(card, null, group);
        });
    });
}

function doInGroupCoordinates(group, what) {
    var index = group.data.index;
    group.rotate(-90*index, table.bounds.center);
    what.call(null, group);
    group.rotate(90*index, table.bounds.center);
}

function turnUpCard(card) {
    placeCard(card, table.bounds.center);
}

function placeCard(card, position, group) {
    if (group) {
        if (hasCards(group))
        {
            card.position = group.lastChild.position + cardSeparation;
        }
        else
        {
            card.position = group.hand.position - [group.hand.bounds.width/2, 0] + [card.bounds.width/2, 0];
        }
        group.addChild(card);
    } else {
        card.position = position;
    }

    card.visible = true;
    card.bringToFront();
    return card;
}

function hasCards(group) {
    var items = group.getItems({className: 'Raster'});
    return items && items.length > 0;
}

function removeCards() {
    for (card in cards) {
        cards[card].remove();
        cardsLayer.addChild(cards[card]);
    }
}

function clearTable() {
    _.each(played, function(card) {
        card.visible = false;
    });
    played = [];
}

function resetDeck() {
    for (var card in cards) {
        cards[card].visible = false;
    }
}


function onFrame(event) {

}

// initial rendering setup..

function loadCards() {
    cardsLayer = new Layer();
    cardsLayer.name = 'cards';

    $("#images_section").find("img").each(function() {
        var img = $(this);
        var id = img.attr("id");
        var card = new Raster(id);
        card.name = id;
        cards[id] = card;
    });

    resetDeck();
}

function scaleCards(scale) {
    for (var card in cards) {
        cards[card].scale(scale);
    }
}

function randomCard() {
    var index = parseInt(Math.random()*32);
    var key = Object.keys(cards)[index];
    return cards[key];
}

function setupTable(a) {
    table = new Path.Rectangle({
        topLeft: new Point(0, 0),
        size: new Size(a, a)
    });
    table.fillColor = {
        gradient: {
            stops: ['#038406', '#038406', '#8af28a'],
            radial: true
        },
        origin: table.bounds.center,
        destination: table.bounds.rightCenter
    };
}

function setupAreas(c, b) {
    var groups = [];

    var path = new Path.Rectangle(
        new Rectangle(
            new Point(c, c + b),
            new Size(b, c))
    );

    for (var i=0; i<4; i++)
    {
        var group = new Group(path);
        group.style = {
            strokeColor: '#000',
            dashArray: [4, 10],
            strokeWidth: 1,
            strokeCap: 'round'
        };
        group.hand = path;
        group.data.index = i;
        group.transformContent = false;
        groups.push(group);
        path = path.clone();
        group.rotate(90*i, table.bounds.center);
    }

    return groups;
}

function setupBubbles() {
    var bubbles = [];
    for (var i=0; i<4; i++) {
        bubbles[i] = new Bubble({ orientation: i, text: '...' });
        bubbles[i].position = groups[i].position;
    }
    return bubbles;
}

function setupScoreAreas(c, b)
{
    gameScoreArea = new ScoreArea({
        title: "Game Score",
        topLeft: new Point(b+c, 0)
    });

    partieScoreArea = new ScoreArea({
        title: "Partie Score",
        topLeft: new Point(0, 0)
    });
}
