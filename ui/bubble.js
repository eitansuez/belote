
$(function() {

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

            chupchik.rotate(-90*this._orientation, this._body.bottomLeft);
            if (this._orientation == 0)
            {
                chupchik.translate(0.25*size.width, size.height + chupchikOffset+2);
            }
            else if (this._orientation == 1)
            {
                chupchik.translate(size.width+chupchikOffset-3, 0.5*size.height);
            }
            else if (this._orientation == 2)
            {
                chupchik.translate(0.75 * size.width, -chupchikOffset);
            }
            else if (this._orientation == 3)
            {
                chupchik.translate(-chupchikOffset-5, 0.5*size.height);
            }

            chupchik.visible = false;

            var bubble = this._body.unite(chupchik);
            this.style = {
                fillColor: '#fff4a3',
                strokeColor: 'black',
                strokeWidth: 1,
                shadowColor: '#aaa',
                shadowBlur: 12,
                shadowOffset: new Point(10, 10)
            };

            this.addChild(bubble);

            this._pointText = new PointText(this.bounds.topLeft + [10, 40]);
            //this._pointText.topLeft = this.bounds.topLeft + [10, 40];
            this._pointText.content = this._splitText();
            this._pointText.fillColor = 'black';
            this._pointText.bringToFront();
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
                } else if (this._orientation == 3) {
                    // scootch right on account of chupchik
                    topLeft += [20, 0];
                }
                this._pointText.position = topLeft;
            }
        }

    });

    var bubble = new Bubble({ text: 'Player "Johnny" passes at Trefle', orientation: 0 });
    bubble.position = [200, 50];
    var bubble2 = new Bubble({ text: 'Testing 123..', orientation: 1 });
    bubble2.position = [200, 170];
    var bubble3 = new Bubble({ text: 'Hello "Johnny"', orientation: 2 });
    bubble3.position = [200, 290];
    var bubble4 = new Bubble({ text: 'Player "Johnny" passes at Trefle', orientation: 3 });
    bubble4.position = [200, 410];

});
