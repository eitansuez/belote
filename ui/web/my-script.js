var cards = {};
var cardSeparation;
var handAspectRatio = 3;
var selectDelta;
var table;

function onResize(event) {
}

$(function() {

    loadCards();

    var a = Math.min(view.bounds.width, view.bounds.height);
    table = new Path.Rectangle({
        topLeft: new Point(0, 0),
        size: new Size(a, a)
    });
    table.fillColor = {
        gradient: {
            stops: ['#8af28a', '#038406']
        },
        origin: table.bounds.topCenter,
        destination: table.bounds.bottomCenter
    };

    var c = a / (2 + handAspectRatio);
    var b = handAspectRatio * c;

    var groups = setupAreas(c, b);

    var scale = c / cards['7_clubs'].height;
    scaleCards(scale);
    var card = randomCard();
    cardSeparation = [card.bounds.width / 2, 0];
    selectDelta = [0, card.bounds.height / 5];

    placeCards([
        cards['7_clubs'],
        cards['king_hearts'],
        cards['ace_diamonds'],
        cards['jack_clubs'],
        cards['9_spades']
    ], groups, 0);
    chooseCard([cards['7_clubs'], cards['jack_clubs']]);

    placeCards([
        cards['10_clubs'],
        cards['10_hearts'],
        cards['10_diamonds'],
        cards['10_spades'],
        cards['jack_hearts']
    ], groups, 1);
    chooseCard([cards['10_clubs'], cards['10_spades']]);

    placeCards([
        cards['ace_hearts'],
        cards['8_diamonds'],
        cards['9_clubs'],
        cards['queen_clubs'],
        cards['jack_spades']
    ], groups, 2);
    chooseCard([cards['8_diamonds'], cards['queen_clubs']]);

    placeCards([
        cards['ace_clubs'],
        cards['8_hearts'],
        cards['7_spades'],
        cards['jack_diamonds'],
        cards['king_spades']
    ], groups, 3);
    chooseCard([cards['king_spades'], cards['7_spades']]);

    turnUpCard(cards['queen_hearts']);
    var counter = 0;
    cards['queen_hearts'].on('click', function() {
        placeCard(cards['queen_hearts'], 0, groups[counter]);
        counter = (counter + 1) % 4;
    });
});

function randomCard() {
    var index = parseInt(Math.random()*32);
    var key = Object.keys(cards)[index];
    return cards[key];
}

function chooseCard(validCards) {
    $.each(validCards, function(index, card) {
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
    $.each(cards, function(index, card) {
        card.off('click');
        card.selected = false;
        if (reposition) {
            card.position += selectDelta;
        }
    });
}

function playCard(card) {
    var group = card.parent;
    var index = group.data.index;
    group.rotate(90*index, table.bounds.center);

    var position = group.position + [0, -(card.bounds.height+5)];
    placeCard(card, position);

    group.rotate(-90*index, table.bounds.center);
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
    }

    return groups;
}

function loadCards() {
    var cardsLayer = new Layer();
    cardsLayer.name = 'cards';

    $("#images_section img").each(function() {
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

////

function placeCards(hand, groups, index) {

    var group = groups[index];

    for (var i=0; i<hand.length; i++) {
        placeCard(hand[i], null, group);
    }

    group.rotate(-90*index, table.bounds.center);
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

