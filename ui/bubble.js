
$(function() {

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

    var bubble = new Bubble({ text: 'Player "Johnny" passes at Trefle', orientation: 0 });
    bubble.position = [200, 80];
    bubble.show();
    var bubble2 = new Bubble({ text: 'Testing 123..', orientation: 1 });
    bubble2.position = [400, 80];
    bubble2.show();
    var bubble3 = new Bubble({ text: 'Hello "Johnny"', orientation: 2 });
    bubble3.position = [200, 150];
    bubble3.show();
    var bubble4 = new Bubble({ text: 'Player "Johnny" passes at Trefle', orientation: 3 });
    bubble4.position = [550, 180];
    bubble4.show();

    setTimeout(function() {
        bubble4.say("Hello Mister Banjo");
        bubble4.position += [100,100];
        setTimeout(function() {
            bubble4.say("Hello Mister Banjo");
        }, 1000);
    }, 1000);

    $("#my-btn").on('click', function() {
       bubble4.say("Howdy");
    });
});


function onFrame(event) {

}
