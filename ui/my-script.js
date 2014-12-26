var cards = {};
var cardSeparation, selectDelta;
var handAspectRatio = 3;
var table, groups;

$(function() {

    loadCards();

    var a = Math.min(view.bounds.width, view.bounds.height);
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
        destination: table.bounds.rightcenter
    };

    var c = a / (2 + handAspectRatio);
    var b = handAspectRatio * c;

    groups = setupAreas(c, b);
    setupScore(c, b);

    var scale = c / cards['Sept_de_Trefle'].height;
    scaleCards(scale);
    var card = randomCard();
    cardSeparation = [card.bounds.width / 2, 0];
    selectDelta = [0, card.bounds.height / 5];
});

var score1, score2;
function setupScore(c, b)
{
    var point = new Point(b+c + 15, 15);
    new PointText({
        point: point,
        content: "Nous: "
    });
    new PointText({
        point: point + [0, 20],
        content: "Eux: "
    });
    score1 = new PointText({
        point: point + [60, 0],
        content: "0",
        justification: "right"
    });
    score2 = new PointText({
        point: point + [60, 20],
        content: "0",
        justification: "right"
    });
}
function updateScore(team1Score, team2Score)
{
    score1.content = "" + team1Score;
    score2.content = "" + team2Score;
}

function randomCard() {
    var index = parseInt(Math.random()*32);
    var key = Object.keys(cards)[index];
    return cards[key];
}

function chooseCard(validCards) {
    _.each(validCards, function(card) {
        card.selected = true;
        card.position -= selectDelta;
        armCard(card);
    });
}

function armCard(card) {
    card.on('click', function() {
        playCard(card);
        deselect([card]);
        deselect(chosenCards(card.parent), true);
    });
}

function chosenCards(group) {
    return group.getItems({className: 'Raster', selected: true});
}

function deselect(cards, reposition) {
    _.each(cards, function(card) {
        card.off('click');
        card.selected = false;
        if (reposition) {
            card.position += selectDelta;
        }
    });
}

function doInGroupCoordinates(group, what) {
    var index = group.data.index;
    group.rotate(90*index, table.bounds.center);
    what.call(null, group);
    group.rotate(-90*index, table.bounds.center);
}

function playCard(card) {
    var group = card.parent;
    doInGroupCoordinates(group, function(group) {
        var position = group.position + [0, -(card.bounds.height+5)];
        placeCard(card, position);
    });
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
        group.data.index = i;
        group.transformContent = false;
        groups.push(group);
        path = path.clone();
        group.rotate(-90*i, table.bounds.center);
    }

    return groups;
}

function loadCards() {
    var cardsLayer = new Layer();
    cardsLayer.name = 'cards';

    $("#images_section").find("img").each(function() {
        var img = $(this);
        var id = img.attr("id");
        var card = new Raster(id);
        card.visible = false;
        card.name = id;
        cards[id] = card;
    });
}

function scaleCards(scale) {
    for (var card in cards) {
        cards[card].scale(scale);
    }
}

function placeCards(hand, groups, index) {

    var group = groups[index];
    doInGroupCoordinates(group, function() {
        _.each(hand, function(card) {
            placeCard(card, null, group);
        });
    });

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
            card.position = group.position - [group.bounds.width/2, 0] + [card.bounds.width/2, 0];
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

