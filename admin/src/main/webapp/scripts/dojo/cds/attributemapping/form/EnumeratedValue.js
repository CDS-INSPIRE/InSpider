define ([
	'dojo/_base/declare',
	'dojo/_base/lang',
	'./Value',
	'dijit/form/Select'
], function (declare, lang, Value, Select) {
	return declare ([ Value ], {
		_values: null,
		
		constructor: function (value, values) {
			this._values = values;
		},
		
		_labelGetter: function () {
			var value = this.get ('value');
			
			return value in this._values ? this._values[value] : '';
		},
		
		getEditorWidget: function (/*Function?*/changeHandler) {
			var options = [ ],
				self = this;
			
			for (var i in this._values) {
				options.push ({
					label: this._values[i],
					value: i
				});
			}
			
			var widget = new Select ({
				options: options,
				value: this.value
			});
			
			this.watch ('value', function (attr, old, newValue) {
				widget.set ('value', newValue);
			});
			widget.watch ('value', lang.hitch (this, function (attr, old, newValue) {
				self.set ('value', newValue);
				if (changeHandler) {
					changeHandler (newValue);
				}
			}));
			
			// Use the default value from the widget if there was no previous value:
			if (!this.value) {
				setTimeout (lang.hitch (this, function () {
					this.set ('value', widget.get ('value'));
					if (changeHandler) {
						changeHandler (this.value);
					}
				}), 0);
			}
			
			return widget;
		}
	});
});