goog.provide('scalosure.collection.immutable.List');

/** @constructor */
scalosure.collection.immutable.List = function(underlying) {
	this.underlying = underlying;
};

scalosure.collection.immutable.List.prototype.foreach = function(fn) {

	var self = this;

	for(var i = 0; i < self.underlying.length; i++) {
		fn(self.underlying[i]);
	}
};

scalosure.collection.immutable.List.prototype.map = function(fn) {

	var self = this;
	var newarray = [];

	for(var i = 0; i < self.underlying.length; i++) {
		newarray.push(fn(self.underlying[i]));
	}

	return new scalosure.collection.immutable.List(newarray);
};

scalosure.collection.immutable.List.prototype.isEmpty = function() {
	return (this.underlying.length == 0);
};

scalosure.collection.immutable.List.prototype.head = function() {
	return this.underlying.slice(0,1)[0];
};

scalosure.collection.immutable.List.prototype.tail = function() {
	return new scalosure.collection.immutable.List(this.underlying.slice(1));
};

scalosure.collection.immutable.List.prototype.exists = function(p) {

	var these = this;

	while (!these.isEmpty()) {
		if (p(these.head())) return true;
		these = these.tail();
	}

	return false;
};

scalosure.collection.immutable.List.prototype.headOption = function() {
	return (this.isEmpty()) ? new scalosure.None() : new scalosure.Some(this.head());
};

scalosure.collection.immutable.List.prototype.$apply = function(index) {
	return this.underlying[index];
};

scalosure.collection.immutable.List.prototype.filter = function(fn) {

	var self = this;
	var newarray = [];

	for(var i = 0; i < self.underlying.length; i++) {
		if(fn(self.underlying[i])) { newarray.push(self.underlying[i]); }
	}

	return new scalosure.collection.immutable.List(newarray);
};

scalosure.collection.immutable.List.$apply = function(xs) {
	return new scalosure.collection.immutable.List(xs);
};
