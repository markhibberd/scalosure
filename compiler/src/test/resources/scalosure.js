goog.provide('scalosure.Some');
goog.provide('scalosure.Option');
goog.provide('scalosure.None');

scalosure.inherits = function(childCtor, parentCtor) {
	/** @constructor */
	function tempCtor() {};
	tempCtor.prototype = parentCtor.prototype;
	childCtor.superClass = parentCtor.prototype;
	childCtor.prototype = new tempCtor();
	childCtor.prototype.constructor = childCtor;
};

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

ScalosureObject = function() {};

scalosure.init = function() {

	String.prototype.startsWith = function(prefix) {
		return (this.indexOf(prefix) === 0);
	};

	String.prototype.endsWith = function(suffix) {
		return this.indexOf(suffix, this.length - suffix.length) !== -1;
	};

	String.prototype.captitalize = function() { };

	String.prototype.compare = function() { };

	String.prototype.format = function() {

		var demuxes_ = {};

		demuxes_['f'] = function(value, flags, width, dotp, precision, type, offset, wholeString) {

			var replacement = value.toString();

			if (!(isNaN(precision) || precision == '')) {
				replacement = value.toFixed(precision);
			}

			// Generates sign string that will be attached to the replacement.
			var sign;
			if (value < 0) {
				sign = '-';
			} else if (flags.indexOf('+') >= 0) {
				sign = '+';
			} else if (flags.indexOf(' ') >= 0) {
				sign = ' ';
			} else {
				sign = '';
			}

			if (value >= 0) {
				replacement = sign + replacement;
			}

			// If no padding is neccessary we're done.
			if (isNaN(width) || replacement.length >= width) {
				return replacement;
			}

			// We need a clean signless replacement to start with
			replacement = isNaN(precision) ?  Math.abs(value).toString() : Math.abs(value).toFixed(precision);

			var padCount = width - replacement.length - sign.length;

			// Find out which side to pad, and if it's left side, then which character to
			// pad, and set the sign on the left and padding in the middle.
			if (flags.indexOf('-', 0) >= 0) {
				replacement = sign + replacement + repeat(' ', padCount);
			} else {
				// Decides which character to pad.
				var paddingChar = (flags.indexOf('0', 0) >= 0) ? '0' : ' ';
				replacement = sign + repeat(paddingChar, padCount) + replacement;
			}

			return replacement;
		};

		demuxes_['d'] = function(value, flags, width, dotp, precision, type, offset, wholeString) {

			value = parseInt(value, 10);
			precision = 0;

			return demuxes_['f'](value, flags, width, dotp, precision, type, offset, wholeString);
		};

		demuxes_['s'] = function(value, flags, width, dotp, precision, type, offset, wholeString) {

			var replacement = value;
			// If no padding is necessary we're done.
			if (isNaN(width) || replacement.length >= width) {
				return replacement;
			}

			// Otherwise we should find out where to put spaces.
			if (flags.indexOf('-', 0) > -1) {
				replacement = replacement + repeat(' ', width - replacement.length);
			} else {
				replacement = repeat(' ', width - replacement.length) + replacement;
			}
			return replacement;
		};

		demuxes_['i'] = demuxes_['d'];
		demuxes_['u'] = demuxes_['d'];

		function repeat(string, length) {
			return new Array(length + 1).join(string);
		};

		// Convert the arguments to an array (MDC recommended way).
		var args = Array.prototype.slice.call(arguments);

		// Try to get the template.
		var template = this.toString();
		if (typeof template == 'undefined') {
			throw Error('[scalosure.String.format] Template required');
		}

		// This re is used for matching, it also defines what is supported.
		var formatRe = /%([0\-\ \+]*)(\d+)?(\.(\d+))?([%sfdiu])/g;

		function replacerDemuxer(match, flags, width, dotp, precision, type, offset, wholeString) {

			// The % is too simple and doesn't take an argument.
			if (type == '%') {
				return '%';
			}

			// Try to get the actual value from parent function.
			var value = args.shift();

			// If we didn't get any arguments, fail.
			if (typeof value == 'undefined') {
				throw Error('[scalosure.String.format] Not enough arguments');
			}

			// Patch the value argument to the beginning of our type specific call.
			arguments[0] = value;

			return demuxes_[type].apply(null, arguments);
		}

		return template.replace(formatRe, replacerDemuxer);
	};

	String.prototype.lines = function() { };

	String.prototype.stripPrefix = function(prefix) {
		return (this.toString().startsWith(prefix)) ? 
			this.toString().substring(prefix.length) : this.toString();
	};

	String.prototype.stripSuffix = function(suffix) {
		return (this.toString().endsWith(suffix)) ?
		    this.toString().substring(0, this.toString().length - suffix.length) : this.toString();
	};

	String.prototype.toBoolean = function() { };

	String.prototype.toNumber = function() { };

	Object.prototype.foreach = function(fn) {
		var self = this;
		for (var key in self){
			if (self.hasOwnProperty(key)) {
				fn({_1:key, _2:self[key]});
			}
		}
	};

	Object.prototype.$apply = function(key) {
		return this[key];
	};

	Object.prototype.update = function(key, value) {
		return this[key] = value;
	};

	Object.prototype.isInstanceOf = function(type) {
		if(this instanceof Object) {
			return this instanceof type;
		} else {
			return typeof this == type;
		}
	};
};

scalosure.matchit = function(thing, thingInfo) {

	var abuilder = [];
	var rst = true;

	var extract = function(matchee, matcher) {

		var type = matcher.type;

		if(type == String || type == Number || type == Boolean) {

			if(matcher.cond != undefined && (matchee != matcher.cond)) {
				rst = false;
			} else {
				if(matcher.bind) abuilder.push(matchee);
			}

		} else {

			// matchee is an object of some type
			if(matchee.isInstanceOf(type)) {

				if(matcher.bind) abuilder.push(matchee);

				// unapply extracts the object into an Option of type tuple or primative(String,Number,Boolean), 
				// which is then applied to a function.
				if(type.unapply != undefined) {

					var extracted = type.unapply(matchee);

					extracted.foreach(function(m) {

						// extracted object is a tuple, so process it's fields
						var fieldTypes = matcher.children;

						for(var i = 0; i < fieldTypes.length; i++) {

							var field = m;

							if(typeof m == 'object') field = m['_'+(i+1)];

							extract(field, fieldTypes[i]);
						}
					});
				} else {
					if(matcher.bind) abuilder.push(matchee);
				}
			} 
		}


		return rst;
	}

	extract(thing, thingInfo)

	if(rst) {
		return abuilder;
	} else {
		return false;
	}
};

scalosure.bitbucket = null;
