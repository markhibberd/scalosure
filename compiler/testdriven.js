goog.provide('T1');
goog.provide('o1');
goog.provide('O');
goog.provide('T');
goog.provide('o');

/** @constructor*/
T1 = function() {};
T1.prototype.other = function() {
var self = this;
};

/** @constructor*/
T = function() {};
T.prototype.v1 = 'foo';
T.prototype.m4 = function() {
var self = this;
console.log(self.name);
};
/** @constructor*/
O = function(name) {
var self = this;
self.name = name;
};
O.prototype.m2 = function() {
var self = this;
};
o1 = new O("o1");
o1.m4 = T.prototype.m4;
o1.v1 = T.prototype.v1;
o1.other = T1.prototype.other;
o1.m3 = function() {
var self = this;
};
o.start = function() {
var self = this;
o1.m4();
return 'done';
};
