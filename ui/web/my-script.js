var cards = {};
var cardSeparation;
var handAspectRatio = 3;
var table;

function onResize(event) {
}

$(function() {
    var backGround = new Path.Rectangle(view.bounds);
    backGround.fillColor = 'green';

    loadCards();

    var a = Math.min(view.bounds.width, view.bounds.height);
    table = new Rectangle(new Point(0, 0), new Size(a, a));
    var c = a / (2 + handAspectRatio);
    var b = handAspectRatio * c;

    var areas = setupAreas(c, b);

    var scale = c / cards['7_clubs'].height;
    scaleCards(scale);
    cardSeparation = new Size(cards['7_clubs'].bounds.width / 2, 0);

    var groups = [];
    for (var i=0; i<4; i++) {
        groups.push(new Group());
    }

    placeCards([
        cards['7_clubs'],
        cards['king_hearts'],
        cards['ace_diamonds'],
        cards['jack_clubs'],
        cards['9_spades']
    ], areas[0], groups, 0);

    //chooseCard([cards['7_clubs'], cards['jack_clubs']]);

    placeCards([
        cards['10_clubs'],
        cards['10_hearts'],
        cards['10_diamonds'],
        cards['10_spades'],
        cards['jack_hearts']
    ], areas[0], groups, 1);

    placeCards([
        cards['ace_hearts'],
        cards['8_diamonds'],
        cards['9_clubs'],
        cards['queen_clubs'],
        cards['jack_spades']
    ], areas[0], groups, 2);

    placeCards([
        cards['ace_clubs'],
        cards['8_hearts'],
        cards['7_spades'],
        cards['jack_diamonds'],
        cards['king_spades']
    ], areas[0], groups, 3);

    turnUpCard(cards['queen_hearts']);

});

function randomCard() {
    var index = parseInt(Math.random()*32);
    cards[index];
}

function chooseCard(validCards) {
    $.each(validCards, function(index, card) {
        //card.selected = true;
        card.position -= [0, card.bounds.height / 5];
    });
}

function setupAreas(c, b) {
    var paths = [];

    var path = new Path.Rectangle(
        new Rectangle(
            new Point(c, c + b),
            new Size(b, c))
    );
    //path.strokeColor = 'black';
    paths.push(path);

    for (var i=0; i<3; i++)
    {
        path = path.clone();
        path.rotate(90, table.center);
        paths.push(path);
    }

    return paths;
}

function loadCards() {
    $("#images_section img").each(function() {
        var img = $(this);
        var id = img.attr("id");
        var card = new Raster(id);
        card.visible = false;
        cards[id] = card;
    });
}
function scaleCards(scale) {
    for (var card in cards) {
        cards[card].scale(scale);
    }
}

////

function placeCards(hand, path, groups, index) {
    var card = hand[0];
    placeCard(card, path.bounds.leftCenter + [card.bounds.width/2, 0]);
    groups[index].addChild(card);

    for (var i=1; i<hand.length; i++) {
        placeCard(hand[i], hand[i-1].position + cardSeparation);
        groups[index].addChild(hand[i]);
    }

    groups[index].rotate(-90*index, table.center);
}

function turnUpCard(card) {
    placeCard(card, table.center);
}

function placeCard(card, position) {
    card.position = position;
    card.visible = true;
    card.bringToFront();
    return card;
}

