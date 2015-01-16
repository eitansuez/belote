
var Card = Base.extend({
    initialize: function(name, cardback, desiredHeight) {
        this.name = name;

        this.backSym = cardback;
        this.back = cardback.place();

        var raster = new Raster(name);
        raster.scale(desiredHeight / raster.height);
        this.faceSym = new Symbol(raster);
        this.face = this.faceSym.place();

        this.faceUp = false;
        this.show();
    },
    show: function() {
        this.upFace().visible = true;
        this.downFace().visible = false;
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
    position: function() {
        return this.face.position;
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
        // reset timeElapsed
        // set flipActive to true
    }

});

var card;
var flipActive = false;

$(function() {
    var desiredHeight = 150;

    var cardback = new Raster('cardback');
    cardback.scale(desiredHeight / cardback.height);
    card = new Card('Roi_de_Coeur', new Symbol(cardback), desiredHeight);
    card.placeAt(new Point(100, 100));

    var fn = function() {
        card.hide();
        timeElapsed = 0;
        flipActive = true;
    };
    card.back.on('click', fn);
    card.face.on('click', fn);
});

var duration = 1; // seconds
var timeElapsed = 0;

function onFrame(event) {
    timeElapsed += event.delta;
    var progress = timeElapsed / duration;

    if (flipActive)
    {
        card.animateFlipFrame(progress);
        flipActive = progress < 1.0;
        if (!flipActive) {
            card.doneFlipping();
        }
    }
}

