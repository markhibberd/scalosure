goog.provide('scalosure.Option');
goog.provide('scalosure.None');
goog.provide('scalosure.Some');

goog.require('scalosure');

/** @constructor */
scalosure.Option = function() {};

scalosure.Option.prototype.isEmpty = function() {
	return true;
};

scalosure.Option.prototype.get = function() {
	return null;
};

scalosure.Option.prototype.filter = function(fn) {
	return (this.isEmpty() || fn(this.get())) ? this : new scalosure.None();
};

scalosure.Option.prototype.exists = function(fn) {
	return (!this.isEmpty() && fn(this.get()));
};

scalosure.Option.prototype.foreach = function(fn) {
	if(!this.isEmpty()) fn(this.get());
};

scalosure.Option.prototype.flatMap = function(fn) {
	return (this.isEmpty()) ? new scalosure.None() : fn(this.get());
};

scalosure.Option.prototype.map = function(fn) {
	return (this.isEmpty()) ? scalosure.None.$apply() : scalosure.Some.$apply(fn(this.get()));
};

scalosure.Option.prototype.getOrElse = function(value) {
	return (this.isEmpty()) ? value : this.get();
};

/** @constructor */
scalosure.None = function() {
	scalosure.Option.call(this);
};
scalosure.inherits(scalosure.None, scalosure.Option);

/** @constructor */
scalosure.Some = function(x) {
	scalosure.Option.call(this);
	this.x = x;
};
scalosure.inherits(scalosure.Some, scalosure.Option);

scalosure.Some.prototype.get = function() {
	return this.x;
};

scalosure.Some.prototype.isEmpty = function() {
	return false;
};

scalosure.Some.$apply = function(x) {
	return new scalosure.Some(x);
};
