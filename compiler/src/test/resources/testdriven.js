goog.provide('Foo');
goog.provide('C3');
goog.provide('C2');
goog.provide('Base');
goog.provide('C1');
goog.provide('C4');
goog.provide('o');
goog.require('scalosure.Some');
/** @constructor*/
Base = function() {
var self = this;
};
/** @constructor*/
C2 = function(v1) {
var self = this;
Base.call(self);
self.v1 = v1;
};
goog.inherits(C2, Base);
C2.prototype.copy$default$1 = function() {
var self = this;
return self.v1;
};
C2.prototype.v1 = null;
/** @constructor*/
C3 = function(v1,v2) {
var self = this;
Base.call(self);
self.v1 = v1;
self.v2 = v2;
};
goog.inherits(C3, Base);
C3.prototype.copy$default$2 = function() {
var self = this;
return self.v2;
};
C3.prototype.copy$default$1 = function() {
var self = this;
return self.v1;
};
C3.prototype.v1 = null;
C3.prototype.v2 = null;
/** @constructor*/
C4 = function(c1,c2) {
var self = this;
Base.call(self);
self.c1 = c1;
self.c2 = c2;
};
goog.inherits(C4, Base);
C4.prototype.copy$default$2 = function() {
var self = this;
return self.c2;
};
C4.prototype.copy$default$1 = function() {
var self = this;
return self.c1;
};
C4.prototype.c1 = null;
C4.prototype.c2 = null;
/** @constructor*/
C1 = function(c4,title) {
var self = this;
Base.call(self);
self.c4 = c4;
self.title = title;
};
goog.inherits(C1, Base);
C1.prototype.copy$default$2 = function() {
var self = this;
return self.title;
};
C1.prototype.copy$default$1 = function() {
var self = this;
return self.c4;
};
C1.prototype.c4 = null;
C1.prototype.title = null;
/** @constructor*/
Foo = function(v1) {
var self = this;
self.v1 = v1;
};
Foo.prototype.copy$default$1 = function() {
var self = this;
return self.v1;
};
Foo.prototype.v1 = null;
o.start = function() {
var self = this;
var x = '10';
return function() {
var matched;
if(typeof x == 'string') {
return function(s) {return ('was: ' + s)}(x)
}}();
};
Foo.unapply = function(x$0) {
var self = this;
return (x$0 == null) ? function() {
return scala.None;
}() : function() {
return scalosure.Some.$apply(x$0.v1);
}();
};
Foo.$apply = function(v1) {
var self = this;
return new Foo(v1);
};
C1.unapply = function(x$0) {
var self = this;
return (x$0 == null) ? function() {
return scala.None;
}() : function() {
return scalosure.Some.$apply({'_1':x$0.c4,'_2':x$0.title});
}();
};
C1.$apply = function(c4,title) {
var self = this;
return new C1(c4,title);
};
C4.unapply = function(x$0) {
var self = this;
return (x$0 == null) ? function() {
return scala.None;
}() : function() {
return scalosure.Some.$apply({'_1':x$0.c1,'_2':x$0.c2});
}();
};
C4.$apply = function(c1,c2) {
var self = this;
return new C4(c1,c2);
};
C3.unapply = function(x$0) {
var self = this;
return (x$0 == null) ? function() {
return scala.None;
}() : function() {
return scalosure.Some.$apply({'_1':x$0.v1,'_2':x$0.v2});
}();
};
C3.$apply = function(v1,v2) {
var self = this;
return new C3(v1,v2);
};
C2.unapply = function(x$0) {
var self = this;
return (x$0 == null) ? function() {
return scala.None;
}() : function() {
return scalosure.Some.$apply(x$0.v1);
}();
};
C2.$apply = function(v1) {
var self = this;
return new C2(v1);
};
